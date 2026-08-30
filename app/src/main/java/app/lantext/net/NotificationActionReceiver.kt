package app.lantext.net

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.lantext.LanTextApp
import app.lantext.notify.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? LanTextApp ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (intent?.action) {
                    ACTION_APPROVE -> {
                        val id = intent.getStringExtra(EXTRA_ID) ?: return@launch
                        app.pairing.approve(id)
                        Notifications.cancelPairing(context)
                    }
                    ACTION_DENY -> {
                        val id = intent.getStringExtra(EXTRA_ID) ?: return@launch
                        app.pairing.deny(id)
                        Notifications.cancelPairing(context)
                    }
                    GatewayService.ACTION_STOP -> {
                        app.settings.setEnabled(false)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_APPROVE = "app.lantext.PAIR_APPROVE"
        const val ACTION_DENY = "app.lantext.PAIR_DENY"
        const val EXTRA_ID = "pairing_id"
    }
}
