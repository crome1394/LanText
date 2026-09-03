package app.lantext.net

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import app.lantext.LanTextApp
import app.lantext.notify.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GatewayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var holdingWifiRequest = false
    private val keepWifiCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = Unit
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val notification = Notifications.gateway(this, app().gateway.snapshot.value)
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(Notifications.GATEWAY_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(Notifications.GATEWAY_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                scope.launch { app().settings.setEnabled(false) }
                stopSelf()
                return START_NOT_STICKY
            }
        }
        try {
            app().gateway.startServerIfEligible()
        } catch (t: Throwable) {
            android.util.Log.e("LanText", "Gateway start failed", t)
        }
        if (observeJob == null) {
            observeJob = scope.launch {
                app().gateway.snapshot.collectLatest { snap ->
                    Notifications.updateGateway(this@GatewayService, snap)
                    updateLocks(snap.listening)
                    if (!snap.enabled) {
                        stopSelf()
                    }
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        observeJob?.cancel()
        app().gateway.stopServer()
        updateLocks(false)
        scope.cancel()
        super.onDestroy()
    }

    private fun updateLocks(listening: Boolean) {
        val wifi = getSystemService(WifiManager::class.java)
        val cm = getSystemService(ConnectivityManager::class.java)
        if (listening) {
            if (wifiLock == null) {
                @Suppress("DEPRECATION")
                wifiLock = wifi.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "lantext:wifi").apply {
                    setReferenceCounted(false)
                }
            }
            if (wifiLock?.isHeld != true) wifiLock?.acquire()
            if (!holdingWifiRequest) {
                try {
                    cm.requestNetwork(
                        NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .build(),
                        keepWifiCallback,
                    )
                    holdingWifiRequest = true
                } catch (t: Throwable) {
                    android.util.Log.w("LanText", "Could not hold Wi-Fi network", t)
                }
            }
        } else {
            wifiLock?.let { if (it.isHeld) it.release() }
            if (holdingWifiRequest) {
                try {
                    cm.unregisterNetworkCallback(keepWifiCallback)
                } catch (_: Exception) {
                }
                holdingWifiRequest = false
            }
        }
    }

    private fun app() = application as LanTextApp

    companion object {
        const val ACTION_STOP = "app.lantext.STOP"
    }
}
