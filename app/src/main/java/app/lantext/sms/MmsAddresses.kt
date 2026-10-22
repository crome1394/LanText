package app.lantext.sms

import android.content.Context
import app.lantext.util.PhoneNumbers

/**
 * Address form carriers route. OMA wants a global phone number so the PDU
 * writer can append /TYPE=PLMN. An email is left as an email.
 */
fun mmsWireAddress(context: Context, raw: String): String {
    val trimmed = raw.trim().substringBefore("/").trim()
    if (trimmed.isEmpty()) return ""
    if ('@' in trimmed) return trimmed
    val e164 = PhoneNumbers.normalize(trimmed, PhoneNumbers.defaultRegion(context))
    if (e164.isNotBlank()) return e164
    val token = trimmed.filter { it.isLetterOrDigit() }
    return if (token.length in 2..11) token else ""
}
