package app.lantext.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import app.lantext.LanTextApp

class IncomingSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val app = context.applicationContext as? LanTextApp ?: return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: emptyArray()
        val address = parts.firstOrNull()?.displayOriginatingAddress
            ?: parts.firstOrNull()?.originatingAddress
            ?: ""
        val body = parts.joinToString("") { it.displayMessageBody ?: it.messageBody ?: "" }
        if (address.isNotBlank() || body.isNotBlank()) {
            app.sms.emitIncoming(address, body)
        } else {
            app.sms.emitRefresh()
        }
    }
}
