package app.lantext.net

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import app.lantext.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WifiMonitor(
    private val context: Context,
    private val settings: SettingsRepository,
    private val gateway: GatewayController,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private val callback: ConnectivityManager.NetworkCallback =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LocationAwareCallback()
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = onWifiAvailable(network)
                override fun onLost(network: Network) = onWifiLost(network)
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    onCaps(caps)
                }
                override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties) {
                    WifiGate.rememberLink(network, lp)
                    reevaluate()
                }
            }
        }

    private var wifiStateReceiver: BroadcastReceiver? = null
    private var forgetSsidRunnable: Runnable? = null
    private var retrySsidRunnable: Runnable? = null
    private var retryStep = 0

    @RequiresApi(Build.VERSION_CODES.S)
    private inner class LocationAwareCallback : ConnectivityManager.NetworkCallback(
        FLAG_INCLUDE_LOCATION_INFO,
    ) {
        override fun onAvailable(network: Network) = onWifiAvailable(network)
        override fun onLost(network: Network) = onWifiLost(network)
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            onCaps(caps)
        }
        override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties) {
            WifiGate.rememberLink(network, lp)
            reevaluate()
        }
    }

    fun start() {
        register()
        registerWifiStateReceiver()
    }

    fun reregister() {
        try {
            cm.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
        }
        register()
    }

    private fun register() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        cm.registerNetworkCallback(request, callback)
        readAndRememberSsid()
        reevaluate()
        if (WifiGate.lastSsid == null && WifiGate.isOnWifi(context)) {
            scheduleSsidRetry()
        }
    }

    private fun registerWifiStateReceiver() {
        if (wifiStateReceiver != null) return
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiManager.WIFI_STATE_CHANGED_ACTION,
                    WifiManager.NETWORK_STATE_CHANGED_ACTION,
                    Intent.ACTION_AIRPLANE_MODE_CHANGED,
                    -> {
                        readAndRememberSsid()
                        reevaluate()
                        if (WifiGate.lastSsid == null) scheduleSsidRetry()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
        )
        wifiStateReceiver = receiver
    }

    private fun onCaps(caps: NetworkCapabilities) {
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val fromCallback = WifiGate.normalizeSsid((caps.transportInfo as? WifiInfo)?.ssid)
            @Suppress("DEPRECATION")
            val fromManager = WifiGate.normalizeSsid(
                context.getSystemService(WifiManager::class.java)?.connectionInfo?.ssid,
            )
            val ssid = fromCallback ?: fromManager
            if (ssid != null) {
                WifiGate.remember(ssid)
                scope.launch { settings.rememberSsid(ssid) }
                cancelSsidRetry()
            } else if (WifiGate.lastSsid == null) {
                scheduleSsidRetry()
            }
        }
        reevaluate()
    }

    private fun onWifiAvailable(network: Network) {
        cancelForgetSsid()
        cm.getLinkProperties(network)?.let { WifiGate.rememberLink(network, it) }
        readAndRememberSsid()
        reevaluate()
        if (WifiGate.lastSsid == null) scheduleSsidRetry()
    }

    private fun onWifiLost(network: Network) {
        WifiGate.forgetLink(network)
        reevaluate()
        scheduleForgetSsid()
    }

    private fun readAndRememberSsid() {
        val ssid = WifiGate.readVisibleSsid(context) ?: return
        WifiGate.remember(ssid)
        scope.launch { settings.rememberSsid(ssid) }
        cancelSsidRetry()
    }

    private fun scheduleForgetSsid() {
        forgetSsidRunnable?.let { mainHandler.removeCallbacks(it) }
        val r = Runnable {
            if (!WifiGate.isOnWifi(context) && WifiGate.wifiIpv4(context) == null) {
                WifiGate.remember(null)
                reevaluate()
            }
        }
        forgetSsidRunnable = r
        mainHandler.postDelayed(r, FORGET_SSID_DELAY_MS)
    }

    private fun cancelForgetSsid() {
        forgetSsidRunnable?.let { mainHandler.removeCallbacks(it) }
        forgetSsidRunnable = null
    }

    private fun scheduleSsidRetry() {
        if (retrySsidRunnable != null) return
        retryStep = 0
        val r = object : Runnable {
            override fun run() {
                readAndRememberSsid()
                reevaluate()
                if (WifiGate.lastSsid == null &&
                    (WifiGate.isOnWifi(context) || WifiGate.wifiIpv4(context) != null) &&
                    retryStep < SSID_RETRY_DELAYS_MS.size
                ) {
                    mainHandler.postDelayed(this, SSID_RETRY_DELAYS_MS[retryStep])
                    retryStep++
                } else {
                    retrySsidRunnable = null
                }
            }
        }
        retrySsidRunnable = r
        mainHandler.postDelayed(r, SSID_RETRY_DELAYS_MS[0])
        retryStep = 1
    }

    private fun cancelSsidRetry() {
        retrySsidRunnable?.let { mainHandler.removeCallbacks(it) }
        retrySsidRunnable = null
        retryStep = 0
    }

    private fun reevaluate() {
        gateway.onNetworkChanged()
    }

    companion object {
        private const val FORGET_SSID_DELAY_MS = 5_000L
        private val SSID_RETRY_DELAYS_MS = longArrayOf(500L, 2_000L, 5_000L, 15_000L)
    }
}
