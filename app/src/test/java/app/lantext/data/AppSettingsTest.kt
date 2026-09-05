package app.lantext.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {
    @Test
    fun experimentalMediaDefaultsOn() {
        val settings = AppSettings()
        assertTrue(settings.gifEnabled)
        assertTrue(settings.voiceEnabled)
        assertTrue(settings.pinnedThreadIds.isEmpty())
    }

    @Test
    fun usAsciiWouldCorruptEmojiTheUtf8FixPreserves() {
        val bytes = "😂".toByteArray(Charsets.UTF_8)
        assertEquals(4, bytes.size)
        assertEquals("\uFFFD".repeat(4), String(bytes, Charsets.US_ASCII))
        assertEquals("😂", String(bytes, Charsets.UTF_8))
    }
}
