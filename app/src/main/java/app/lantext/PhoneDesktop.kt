package app.lantext

import app.lantext.sms.DesktopSchedule
import app.lantext.sms.DesktopTrash

/** Phone-only lists the computer inbox cannot read from telephony. */
interface PhoneDesktop {
    fun trashed(): List<DesktopTrash>
    fun scheduled(): List<DesktopSchedule>
    fun restoreTrash(threadId: String)
    fun deleteTrash(threadId: String)
    fun cancelScheduled(id: String)
}
