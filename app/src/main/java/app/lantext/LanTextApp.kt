package app.lantext

import android.app.Application
import app.lantext.data.PairingManager
import app.lantext.data.SettingsRepository
import app.lantext.net.GatewayController
import app.lantext.net.TlsCertificateStore
import app.lantext.net.WifiMonitor
import app.lantext.notify.Notifications
import app.lantext.sms.ContactsRepository
import app.lantext.sms.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LanTextApp : Application() {
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
