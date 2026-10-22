package app.lantext.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import app.lantext.LanTextApp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val raw = intent?.getStringExtra(EXTRA_URI) ?: return
        val uri = Uri.parse(raw)
        val ok = resultCode == Activity.RESULT_OK
        if (!SentParts.onPart(raw, ok)) return
        val failed = SentParts.takeFailed(raw)
        val values = ContentValues().apply {
            put(
                Telephony.Sms.TYPE,
                if (failed) Telephony.Sms.MESSAGE_TYPE_FAILED else Telephony.Sms.MESSAGE_TYPE_SENT,
            )
        }
        try {
            context.contentResolver.update(uri, values, null, null)
        } catch (_: SecurityException) {
        }
        (context.applicationContext as? LanTextApp)?.sms?.emitRefresh()
    }

    companion object {
        const val ACTION_SENT = "app.lantext.SMS_SENT"
        const val EXTRA_URI = "uri"
        const val EXTRA_INDEX = "index"
        const val EXTRA_COUNT = "count"
    }
}

/** Counts multipart send results so one stored row is marked sent or failed once. */
internal object SentParts {
    private val pending = ConcurrentHashMap<String, AtomicInteger>()
    private val failed = ConcurrentHashMap<String, AtomicBoolean>()

    fun begin(key: String, count: Int) {
        pending[key] = AtomicInteger(count.coerceAtLeast(1))
        failed[key] = AtomicBoolean(false)
    }

    /** True when every part for this message has reported. */
    fun onPart(key: String, ok: Boolean): Boolean {
        if (!ok) failed[key]?.set(true)
        val left = pending[key]?.decrementAndGet() ?: return true
        return left <= 0
    }

    fun takeFailed(key: String): Boolean {
        val value = failed.remove(key)?.get() ?: false
        pending.remove(key)
        return value
    }
}
