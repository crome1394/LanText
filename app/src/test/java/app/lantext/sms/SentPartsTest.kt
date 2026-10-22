package app.lantext.sms

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SentPartsTest {
    @Test
    fun multipartWaitsUntilEveryPartReports() {
        val key = "sent-parts-ok"
        SentParts.begin(key, 2)
        assertFalse(SentParts.onPart(key, true))
        assertTrue(SentParts.onPart(key, true))
        assertFalse(SentParts.takeFailed(key))
    }

    @Test
    fun oneFailedPartMarksTheMessageFailed() {
        val key = "sent-parts-fail"
        SentParts.begin(key, 2)
        assertFalse(SentParts.onPart(key, false))
        assertTrue(SentParts.onPart(key, true))
        assertTrue(SentParts.takeFailed(key))
    }
}
