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

    /** Keys that should find the same person whether the carrier stored a national number or E.164. */
    fun lookupKeys(raw: String?, region: String): List<String> {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return emptyList()
        val keys = LinkedHashSet<String>()
        val norm = normalize(value, region)
        if (norm.length >= 7) keys += norm
        val digits = digitsKeepPlus(value)
        if (digits.length >= 7) keys += digits
        val bare = digits.removePrefix("+")
        if (bare.length == 11 && bare.startsWith("1")) keys += bare.drop(1)
        if (bare.length >= 10) keys += bare.takeLast(10)
        shortCodeKey(value, region)?.let { keys += it }
        return keys.toList()
    }

    /**
     * Carrier short codes are the whole number. "+1 49447" and "49447" are the
     * same code. A normal phone number is not reduced to a short suffix.
     */
    fun shortCodeKey(raw: String, region: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val bare = trimmed.filter { it.isDigit() }
        if (bare.isEmpty()) return null
        val formatted = trimmed.startsWith("+") || trimmed.any { it == ' ' || it == '-' || it == '(' || it == ')' }
        val cc = countryCallingCode(region)
        if (formatted && cc.isNotEmpty() && bare.startsWith(cc)) {
            val national = bare.removePrefix(cc)
            if (national.length in 3..6) return national
        }
        return bare.takeIf { it.length in 3..6 }
    }

    /** Forms to try with the system contact lookup, including a short code's country prefix. */
    fun phoneLookupForms(raw: String?, region: String): List<String> {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return emptyList()
        val forms = LinkedHashSet<String>()
        forms += value
        val code = shortCodeKey(value, region) ?: return forms.toList()
        forms += code
        val cc = countryCallingCode(region)
        if (cc.isNotEmpty()) forms += "+$cc$code"
        return forms.toList()
    }

    private fun countryCallingCode(region: String): String {
        val code = util.getCountryCodeForRegion(region.ifBlank { "US" })
        return if (code > 0) code.toString() else ""
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
