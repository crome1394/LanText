package app.lantext.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MmsAudioTest {
    @Test
    fun wavToPcmReadsMono16kThenResamples() {
        val samples = ShortArray(16000) { (it % 100).toShort() }
        val wav = wavOf(samples, 16000)
        val pcm = MmsAudio.wavToPcm8kMono16(wav)
        assertTrue(pcm.size in 7900..8100)
        assertEquals(0, pcm[0].toInt())
    }

    @Test
    fun gifHostAllowlist() {
        assertTrue(GifSearch.isAllowed("https://upload.wikimedia.org/wikipedia/commons/a/a.gif"))
        org.junit.Assert.assertFalse(GifSearch.isAllowed("https://evil.example/x.gif"))
        org.junit.Assert.assertFalse(GifSearch.isAllowed("http://upload.wikimedia.org/wikipedia/commons/x.gif"))
    }

    private fun wavOf(samples: ShortArray, rate: Int): ByteArray {
        val n = samples.size * 2
        val buf = ByteBuffer.allocate(44 + n).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray())
        buf.putInt(36 + n)
        buf.put("WAVE".toByteArray())
        buf.put("fmt ".toByteArray())
        buf.putInt(16)
        buf.putShort(1)
        buf.putShort(1)
        buf.putInt(rate)
        buf.putInt(rate * 2)
        buf.putShort(2)
        buf.putShort(16)
        buf.put("data".toByteArray())
        buf.putInt(n)
        for (s in samples) buf.putShort(s)
        return buf.array()
    }
}
