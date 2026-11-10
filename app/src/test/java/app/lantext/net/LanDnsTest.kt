package app.lantext.net

import app.lantext.sms.displayMime
import app.lantext.sms.imageContentType
import app.lantext.sms.mmsTimestampMillis
import app.lantext.sms.partText
import app.lantext.util.PhoneNumbers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanDnsTest {
    @Test
    fun answersOnlyQuestionsForTheStableName() {
        val query = ByteArray(12) + LanDns.encodeName(LanDns.HOST) + byteArrayOf(0, 1, 0, 1)
        query[5] = 1
        assertTrue(LanDns.queryWantsHost(query))
        val other = ByteArray(12) + LanDns.encodeName("printer.local") + byteArrayOf(0, 1, 0, 1)
        other[5] = 1
        assertFalse(LanDns.queryWantsHost(other))
        val response = query.copyOf()
        response[2] = 0x84.toByte()
        assertFalse(LanDns.queryWantsHost(response))
    }

    @Test
    fun answerCarriesTheCurrentAddress() {
        val bytes = LanDns.answerA(0x12, 0x34, "10.74.10.93")
        assertEquals(0x12, bytes[0].toInt() and 0xff)
        assertEquals(0x34, bytes[1].toInt() and 0xff)
        assertEquals(10, bytes[bytes.size - 4].toInt() and 0xff)
        assertEquals(74, bytes[bytes.size - 3].toInt() and 0xff)
        assertEquals(10, bytes[bytes.size - 2].toInt() and 0xff)
        assertEquals(93, bytes[bytes.size - 1].toInt() and 0xff)
    }
}

class MmsDisplayTest {
    @Test
    fun keepsRealMediaTypesAndSniffsNamelessPictures() {
        assertEquals("image/jpeg", displayMime("image/jpg", null))
        assertEquals("image/jpeg", displayMime("application/octet-stream", "photo.JPG"))
        assertEquals("video/mp4", displayMime("", "clip.mp4"))
        assertEquals("image/png", displayMime("application/octet-stream", null, byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)))
        assertEquals("image/gif", imageContentType("GIF89a".toByteArray()))
    }

    @Test
    fun readsTextFromThePartFileWhenTheColumnIsEmpty() {
        assertEquals("hello", partText(" hello ", null))
        assertEquals("from file", partText(null, "from file".toByteArray()))
        assertNull(partText("   ", byteArrayOf(0, 1, 2)))
    }

    @Test
    fun providerDatesInSecondsBecomeMillis() {
        assertEquals(1_700_000_000_000L, mmsTimestampMillis(1_700_000_000L))
        assertEquals(1_700_000_000_000L, mmsTimestampMillis(1_700_000_000_000L))
    }
}

class PhoneLookupTest {
    @Test
    fun nationalAndE164ShareAKey() {
        val fromContact = PhoneNumbers.lookupKeys("+1 555-123-4567", "US")
        val fromCarrier = PhoneNumbers.lookupKeys("5551234567", "US")
        assertTrue(fromContact.intersect(fromCarrier.toSet()).isNotEmpty())
        assertTrue("15551234567".let { PhoneNumbers.lookupKeys(it, "US") }.contains("5551234567") ||
            PhoneNumbers.lookupKeys("15551234567", "US").any { it.endsWith("5551234567") })
    }

    @Test
    fun shortCodeMatchesWithOrWithoutTheCountryCode() {
        assertTrue(PhoneNumbers.lookupKeys("49447", "US").contains("49447"))
        assertTrue(PhoneNumbers.lookupKeys("+1 49447", "US").contains("49447"))
        assertTrue(PhoneNumbers.lookupKeys("1-49447", "US").contains("49447"))
        assertTrue(PhoneNumbers.lookupKeys("26499", "US").contains("26499"))
        assertTrue(PhoneNumbers.lookupKeys("+1 604-369-8359", "US").none { it.length in 3..6 })
        assertTrue(PhoneNumbers.phoneLookupForms("49447", "US").contains("+149447"))
    }
}
