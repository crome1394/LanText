package app.lantext.util

import android.content.Context
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

object PhoneNumbers {
    private val util = PhoneNumberUtil.getInstance()

    fun defaultRegion(context: Context): String {
        val tm = context.getSystemService(TelephonyManager::class.java)
        val iso = tm?.simCountryIso?.ifBlank { tm.networkCountryIso }.orEmpty()
        return iso.uppercase().ifBlank { "US" }
    }

    fun normalize(raw: String?, region: String): String {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return ""
        return try {
            val parsed = util.parse(value, region)
            if (util.isValidNumber(parsed)) {
                util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
            } else {
                digitsKeepPlus(value)
            }
        } catch (_: NumberParseException) {
            digitsKeepPlus(value)
        }
    }

    fun digitsKeepPlus(raw: String): String {
        val plus = raw.trim().startsWith("+")
        val digits = raw.filter { it.isDigit() }
        return if (plus) "+$digits" else digits
    }

    fun display(raw: String?, region: String): String {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return ""
        return try {
            val parsed = util.parse(value, region)
            util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (_: Exception) {
            value
        }
    }
}
