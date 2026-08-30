package app.lantext.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.lantext.LanTextApp

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        (context.applicationContext as? LanTextApp)?.sms?.emitRefresh()
    }
}
