package app.lantext.sms

import android.media.MediaCodec
import android.media.MediaFormat
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

object MmsAudio {
    private val AMR_HEADER = "#!AMR\n".toByteArray(Charsets.US_ASCII)
    private const val SAMPLE_RATE = 8000
    private const val FRAME_SAMPLES = 160
    const val MAX_SECONDS = 45

    fun toAmr(input: ByteArray): ByteArray {
        val pcm = wavToPcm8kMono16(input)
        require(pcm.isNotEmpty()) { "Voice note was empty" }
        val maxSamples = SAMPLE_RATE * MAX_SECONDS
        val clipped = if (pcm.size > maxSamples) pcm.copyOf(maxSamples) else pcm
        return encodeAmrNb(clipped)
    }

    internal fun wavToPcm8kMono16(wav: ByteArray): ShortArray {
        require(wav.size >= 44) { "Voice note was too short" }
        require(String(wav, 0, 4) == "RIFF" && String(wav, 8, 4) == "WAVE") {
            "Voice note was not a WAV recording"
        }
        var offset = 12
        var channels = 1
        var rate = SAMPLE_RATE
        var bits = 16
        var dataStart = -1
        var dataLen = 0
        while (offset + 8 <= wav.size) {
            val id = String(wav, offset, 4)
            val size = ByteBuffer.wrap(wav, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            val payload = offset + 8
            when (id) {
                "fmt " -> {
                    val fmt = ByteBuffer.wrap(wav, payload, size.coerceAtMost(wav.size - payload)).order(ByteOrder.LITTLE_ENDIAN)
                    val format = fmt.short.toInt() and 0xffff
                    require(format == 1) { "Voice note must be PCM" }
                    channels = fmt.short.toInt() and 0xffff
                    rate = fmt.int
                    fmt.int
                    fmt.short
                    bits = fmt.short.toInt() and 0xffff
                }
                "data" -> {
                    dataStart = payload
                    dataLen = size.coerceAtMost(wav.size - payload)
                    break
                }
            }
            offset = payload + size + (size and 1)
        }
        require(dataStart >= 0) { "Voice note had no audio data" }
        val src = if (bits == 16) {
            val n = dataLen / 2
            val out = ShortArray(n)
            ByteBuffer.wrap(wav, dataStart, n * 2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(out)
            downmix(out, channels)
        } else {
            val n = dataLen
            val out = ShortArray(n)
            for (i in 0 until n) {
                out[i] = (((wav[dataStart + i].toInt() and 0xff) - 128) shl 8).toShort()
            }
            downmix(out, channels)
        }
        return resample(src, rate, SAMPLE_RATE)
    }

    private fun downmix(pcm: ShortArray, channels: Int): ShortArray {
        if (channels <= 1) return pcm
        val frames = pcm.size / channels
        val out = ShortArray(frames)
        for (i in 0 until frames) {
            var sum = 0
            for (c in 0 until channels) sum += pcm[i * channels + c].toInt()
            out[i] = (sum / channels).toShort()
        }
        return out
    }

    private fun resample(pcm: ShortArray, fromRate: Int, toRate: Int): ShortArray {
        if (fromRate == toRate || pcm.isEmpty()) return pcm
        val outLen = (pcm.size.toLong() * toRate / fromRate).toInt().coerceAtLeast(1)
        val out = ShortArray(outLen)
        for (i in 0 until outLen) {
            val src = i.toDouble() * fromRate / toRate
            val i0 = src.toInt().coerceIn(0, pcm.lastIndex)
            val i1 = (i0 + 1).coerceAtMost(pcm.lastIndex)
            val t = src - i0
            out[i] = ((pcm[i0] * (1 - t) + pcm[i1] * t).toInt()).toShort()
        }
        return out
    }

    private fun encodeAmrNb(pcm: ShortArray): ByteArray {
        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AMR_NB)
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AMR_NB, SAMPLE_RATE, 1)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 12200)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
        val out = ByteArrayOutputStream()
        out.write(AMR_HEADER)
        val frameBytes = FRAME_SAMPLES * 2
        val pcmBytes = ByteArray(pcm.size * 2)
        ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcm)
        var offset = 0
        var inputDone = false
        var outputDone = false
        val info = MediaCodec.BufferInfo()
        var idle = 0
        while (!outputDone && idle < 500) {
            if (!inputDone) {
                val ix = codec.dequeueInputBuffer(10_000)
                if (ix >= 0) {
                    val buf = codec.getInputBuffer(ix)!!
                    buf.clear()
                    if (offset >= pcmBytes.size) {
                        codec.queueInputBuffer(ix, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        val n = min(frameBytes, pcmBytes.size - offset)
                        buf.put(pcmBytes, offset, n)
                        val pts = offset * 1_000_000L / (SAMPLE_RATE * 2)
                        codec.queueInputBuffer(ix, 0, n, pts, 0)
                        offset += n
                    }
                    idle = 0
                }
            }
            val ox = codec.dequeueOutputBuffer(info, 10_000)
            when {
                ox >= 0 -> {
                    idle = 0
                    if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                        val buf = codec.getOutputBuffer(ox)!!
                        val chunk = ByteArray(info.size)
                        buf.position(info.offset)
                        buf.get(chunk)
                        out.write(chunk)
                    }
                    codec.releaseOutputBuffer(ox, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                }
                ox == MediaCodec.INFO_TRY_AGAIN_LATER -> idle++
                else -> idle++
            }
        }
        codec.stop()
        codec.release()
        val amr = out.toByteArray()
        require(amr.size > AMR_HEADER.size) { "Could not encode the voice note for MMS" }
        return amr
    }
}
