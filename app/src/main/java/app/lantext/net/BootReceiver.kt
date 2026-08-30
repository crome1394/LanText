package app.lantext.net

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import app.lantext.LanTextApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val app = context.applicationContext as? LanTextApp ?: return@launch
                val settings = app.settings.current()
                if (WifiGate.shouldListen(context, settings)) {
                    ContextCompat.startForegroundService(context, Intent(context, GatewayService::class.java))
                }
            } finally {
                pending.finish()
            }
        }
    }
}
