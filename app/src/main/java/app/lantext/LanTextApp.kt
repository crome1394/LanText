package app.lantext

import android.app.Application
import app.lantext.data.PairingManager
import app.lantext.data.SettingsRepository
import app.lantext.net.GatewayController
import app.lantext.net.TlsCertificateStore
import app.lantext.net.WifiGate
import app.lantext.net.WifiMonitor
import app.lantext.notify.Notifications
import app.lantext.sms.ContactsRepository
import app.lantext.sms.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LanTextApp : Application() {
    /** Picture messages ask the carrier for a delivery report when this is on. */
    @Volatile
    var mmsDeliveryReports: Boolean = false

    /** Product name shown in this process. Empty keeps the standalone LanText strings. */
    @Volatile
    var productLabel: String = ""

    /** Trash and scheduled texts live in the host app. Null on standalone LanText. */
    @Volatile
    var phoneDesktop: PhoneDesktop? = null

    fun productName(): String {
        val custom = productLabel.trim()
        if (custom.isNotEmpty()) return custom
        return runCatching { applicationInfo.loadLabel(packageManager).toString() }
            .getOrDefault("")
            .trim()
            .ifBlank { "this app" }
    }

    lateinit var settings: SettingsRepository
        private set
    lateinit var pairing: PairingManager
        private set
    lateinit var certs: TlsCertificateStore
        private set
    lateinit var sms: SmsRepository
        private set
    lateinit var contacts: ContactsRepository
        private set
    lateinit var gateway: GatewayController
        private set
    lateinit var wifi: WifiMonitor
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        Notifications.ensureChannels(this)
        settings = SettingsRepository(this)
        pairing = PairingManager(this)
        certs = TlsCertificateStore(this)
        contacts = ContactsRepository(this)
        sms = SmsRepository(this, contacts)
        gateway = GatewayController(this, settings, pairing, certs, sms)
        wifi = WifiMonitor(this, settings, gateway)
        wifi.start()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val saved = settings.current()
            if (!saved.lastNetworkSsid.isNullOrBlank()) {
                WifiGate.remember(saved.lastNetworkSsid, saved.lastNetworkIpv4)
                gateway.onNetworkChanged()
            }
            pairing.pendingPairing.collect { request ->
                if (request != null) Notifications.pairing(this@LanTextApp, request)
                else Notifications.cancelPairing(this@LanTextApp)
            }
        }
    }

    companion object {
        lateinit var instance: LanTextApp
            private set
    }
}

/** Rewrites shipped product names in text this app shows or serves. Leaves the desktop host name alone. */
fun brandProductText(text: String, product: String): String {
    val name = product.trim()
    if (name.isEmpty() || name == "LanText") return text
    return text
        .replace("LanMessaging", name)
        .replace("LanText", name)
        .replace("lMessages", name)
}
