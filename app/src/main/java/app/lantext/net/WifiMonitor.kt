package app.lantext.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.os.Build
import androidx.annotation.RequiresApi
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

    fun start() = register()

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
        reevaluate()
    }

    private fun onCaps(caps: NetworkCapabilities) {
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val fromCallback = WifiGate.normalizeSsid((caps.transportInfo as? WifiInfo)?.ssid)
            @Suppress("DEPRECATION")
            val fromManager = WifiGate.normalizeSsid(
                context.getSystemService(android.net.wifi.WifiManager::class.java)?.connectionInfo?.ssid,
            )
            val ssid = fromCallback ?: fromManager
            if (ssid != null) {
                WifiGate.remember(ssid)
                scope.launch { settings.rememberSsid(ssid) }
            }
        }
        reevaluate()
    }

    private fun onWifiAvailable(network: Network) {
        cm.getLinkProperties(network)?.let { WifiGate.rememberLink(network, it) }
        reevaluate()
    }

    private fun onWifiLost(network: Network) {
        WifiGate.forgetLink(network)
        WifiGate.remember(null)
        reevaluate()
    }

    private fun reevaluate() {
        gateway.onNetworkChanged()
    }
}
