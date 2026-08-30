package app.lantext.net

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import app.lantext.data.AppSettings
import app.lantext.data.GateReason
import app.lantext.data.GatewaySnapshot
import app.lantext.data.PairingManager
import app.lantext.data.SettingsRepository
import app.lantext.sms.SmsRepository
import app.lantext.widget.ToggleWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GatewayController(
    private val context: Context,
    private val settingsRepo: SettingsRepository,
    private val pairing: PairingManager,
    private val certs: TlsCertificateStore,
    private val sms: SmsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _snapshot = MutableStateFlow(GatewaySnapshot())
    val snapshot: StateFlow<GatewaySnapshot> = _snapshot
    val clientCount = MutableStateFlow(0)

    @Volatile var server: GatewayServer? = null
        private set

    init {
        scope.launch {
            combine(settingsRepo.settings, pairing.pairingPin, clientCount) { settings, pin, clients ->
                Triple(settings, pin, clients)
            }.collectLatest { (settings, pin, clients) ->
                refresh(settings, pin, clients)
            }
        }
    }

    fun onNetworkChanged() {
        scope.launch {
            val settings = settingsRepo.current()
            refresh(settings, pairing.pairingPin.value, clientCount.value)
        }
    }

    @Synchronized
    fun refresh(settings: AppSettings = _lastSettings, pin: String? = pairing.pairingPin.value, clients: Int = clientCount.value) {
        _lastSettings = settings
        val reason = WifiGate.evaluate(context, settings)
        val ssid = WifiGate.currentSsid(context)
        val ip = WifiGate.wifiIpv4(context)
        val listening = reason == GateReason.LISTENING && ip != null
        if (listening) {
            ensureService()
        } else {
            context.stopService(Intent(context, GatewayService::class.java))
        }
        val url = if (listening && ip != null) "https://$ip:${settings.listenPort}" else null
        _snapshot.value = GatewaySnapshot(
            enabled = settings.enabled,
            listening = listening && server != null,
            ssid = ssid,
            bindAddress = if (listening) ip else null,
            port = settings.listenPort,
            url = url,
            fingerprintSha256 = certs.fingerprintSha256.ifBlank { null },
            pairingPin = if (listening) pin else null,
            reason = if (listening && ip == null) GateReason.NO_WIFI else reason,
            clientCount = clients,
        )
        ToggleWidget.updateAll(context, _snapshot.value)
    }

    @Synchronized
    fun startServerIfEligible(): Boolean {
        val settings = _lastSettings
        val reason = WifiGate.evaluate(context, settings)
        val ip = WifiGate.wifiIpv4(context) ?: return false
        if (reason != GateReason.LISTENING) {
            stopServer()
            return false
        }
        if (server != null && _snapshot.value.bindAddress == ip) {
            return true
        }
        stopServer()
        certs.keyStore(ip)
        pairing.rotatePin()
        sms.startWatching()
        val created = GatewayServer(
            context = context,
            bindAddress = ip,
            listenPort = settings.listenPort,
            certs = certs,
            pairing = pairing,
            sms = sms,
            contacts = app.lantext.LanTextApp.instance.contacts,
            clientCount = clientCount,
        )
        try {
            created.start()
        } catch (t: Throwable) {
            created.stop()
            sms.stopWatching()
            android.util.Log.e("LanText", "Failed to start HTTPS server", t)
            return false
        }
        server = created
        refresh(settings, pairing.pairingPin.value, clientCount.value)
        return true
    }

    @Synchronized
    fun stopServer() {
        server?.stop()
        server = null
        sms.stopWatching()
    }

    private fun ensureService() {
        val intent = Intent(context, GatewayService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    private var _lastSettings: AppSettings = AppSettings()
}
