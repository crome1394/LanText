package app.lantext.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.lantext.LanTextApp
import app.lantext.MainActivity
import app.lantext.R
import app.lantext.data.GatewaySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToggleWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val snap = snapshot(context)
        ids.forEach { id -> manager.updateAppWidget(id, views(context, snap)) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_TOGGLE) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val app = context.applicationContext as LanTextApp
                val enabled = app.settings.current().enabled
                app.settings.setEnabled(!enabled)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "app.lantext.widget.TOGGLE"

        fun updateAll(context: Context, snap: GatewaySnapshot) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ToggleWidget::class.java))
            if (ids.isEmpty()) return
            val remote = views(context, snap)
            ids.forEach { manager.updateAppWidget(it, remote) }
        }

        private fun snapshot(context: Context): GatewaySnapshot {
            val app = context.applicationContext as? LanTextApp
            return app?.gateway?.snapshot?.value ?: GatewaySnapshot()
        }

        private fun views(context: Context, snap: GatewaySnapshot): RemoteViews {
            val remote = RemoteViews(context.packageName, R.layout.widget_toggle)
            val status = when {
                !snap.enabled -> context.getString(R.string.widget_off)
                snap.listening -> context.getString(R.string.widget_listening, snap.ssid ?: "Wi-Fi")
                else -> context.getString(R.string.widget_waiting)
            }
            remote.setTextViewText(R.id.widget_status, status)
            remote.setTextViewText(
                R.id.widget_toggle,
                if (snap.enabled) context.getString(R.string.widget_turn_off)
                else context.getString(R.string.widget_turn_on),
            )
            val open = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            remote.setOnClickPendingIntent(R.id.widget_open, open)
            val toggle = PendingIntent.getBroadcast(
                context,
                1,
                Intent(context, ToggleWidget::class.java).setAction(ACTION_TOGGLE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            remote.setOnClickPendingIntent(R.id.widget_toggle, toggle)
            return remote
        }
    }
}
