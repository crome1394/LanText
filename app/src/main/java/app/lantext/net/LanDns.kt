package app.lantext.net

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface

/**
 * Answers mDNS for [HOST] with the phone's current Wi-Fi address.
 * The TLS certificate already names that host, so a computer can keep the
 * same page open when the phone leaves and comes back with a new address.
 */
object LanDns {
    const val HOST = "blurttext.lan"
    private const val PORT = 5353
    private const val TAG = "LanText"

    @Volatile private var address: String? = null
    @Volatile private var running = false
    private var worker: Thread? = null
    private var socket: MulticastSocket? = null
    private var lock: WifiManager.MulticastLock? = null

    fun advertise(context: Context, ip: String) {
        address = ip
        if (running) {
            announce()
            return
        }
        running = true
        val appContext = context.applicationContext
        worker = Thread({
            try {
                listen(appContext)
            } catch (t: Throwable) {
                Log.w(TAG, "mDNS stopped", t)
            } finally {
                release()
            }
        }, "lantext-mdns").also {
            it.isDaemon = true
            it.start()
        }
    }

    fun stop() {
        running = false
        address = null
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        worker = null
    }

    internal fun queryWantsHost(packet: ByteArray): Boolean {
        if (packet.size < 12) return false
        val flags = ((packet[2].toInt() and 0xff) shl 8) or (packet[3].toInt() and 0xff)
        if (flags and 0x8000 != 0) return false
        val questions = ((packet[4].toInt() and 0xff) shl 8) or (packet[5].toInt() and 0xff)
        var index = 12
        repeat(questions) {
            val read = readName(packet, index) ?: return false
            index = read.second
            if (index + 4 > packet.size) return false
            val type = ((packet[index].toInt() and 0xff) shl 8) or (packet[index + 1].toInt() and 0xff)
            index += 4
            if (read.first.equals(HOST, true) && (type == 1 || type == 255)) return true
        }
        return false
    }

    internal fun answerA(queryId0: Int, queryId1: Int, ipv4: String): ByteArray {
        val name = encodeName(HOST)
        val parts = ipv4.split('.')
        require(parts.size == 4) { "ipv4" }
        val out = ByteArray(12 + name.size + 14)
        out[0] = queryId0.toByte()
        out[1] = queryId1.toByte()
        out[2] = 0x84.toByte()
        out[7] = 1
        var index = 12
        name.copyInto(out, index)
        index += name.size
        out[index + 1] = 1
        out[index + 3] = 1
        out[index + 7] = 120
        out[index + 9] = 4
        index += 10
        parts.forEach { out[index++] = (it.toInt() and 0xff).toByte() }
        return out
    }

    internal fun encodeName(host: String): ByteArray {
        val out = ArrayList<Byte>()
        for (label in host.split('.')) {
            out += label.length.toByte()
            label.forEach { out += it.code.toByte() }
        }
        out += 0
        return out.toByteArray()
    }

    private fun listen(context: Context) {
        val group = InetAddress.getByName("224.0.0.251")
        val opened = openMdnsSocket()
        val ip = address
        val iface = ip?.let { NetworkInterface.getByInetAddress(InetAddress.getByName(it)) }
        if (opened.localPort == PORT) {
            try {
                if (iface != null) {
                    opened.joinGroup(InetSocketAddress(group, PORT), iface)
                } else {
                    @Suppress("DEPRECATION")
                    opened.joinGroup(group)
                }
            } catch (_: Exception) {
            }
        }
        socket = opened
        try {
            val wifi = context.getSystemService(WifiManager::class.java)
            lock = wifi?.createMulticastLock("lantext-mdns")?.apply {
                setReferenceCounted(false)
                acquire()
            }
        } catch (_: Exception) {
            lock = null
        }
        announce()
        if (opened.localPort != PORT) {
            while (running) {
                try {
                    Thread.sleep(15_000)
                } catch (_: InterruptedException) {
                    break
                }
                if (!running) break
                announce()
            }
            return
        }
        val buf = ByteArray(900)
        while (running) {
            val packet = DatagramPacket(buf, buf.size)
            try {
                opened.receive(packet)
            } catch (_: Exception) {
                break
            }
            val data = buf.copyOf(packet.length)
            if (!queryWantsHost(data)) continue
            val host = address ?: continue
            val answer = answerA(data[0].toInt() and 0xff, data[1].toInt() and 0xff, host)
            send(opened, answer, group)
        }
    }

    private fun openMdnsSocket(): MulticastSocket {
        fun prepare(socket: MulticastSocket) {
            socket.reuseAddress = true
            socket.timeToLive = 255
            socket.loopbackMode = true
        }
        val preferred = MulticastSocket(null)
        prepare(preferred)
        try {
            preferred.bind(InetSocketAddress(PORT))
            return preferred
        } catch (_: Exception) {
            try {
                preferred.close()
            } catch (_: Exception) {
            }
        }
        val fallback = MulticastSocket(null)
        prepare(fallback)
        fallback.bind(InetSocketAddress(0))
        return fallback
    }

    private fun announce() {
        val opened = socket ?: return
        val host = address ?: return
        val group = InetAddress.getByName("224.0.0.251")
        send(opened, answerA(0, 0, host), group)
    }

    private fun send(opened: MulticastSocket, bytes: ByteArray, group: InetAddress) {
        try {
            opened.send(DatagramPacket(bytes, bytes.size, group, PORT))
        } catch (_: Exception) {
        }
    }

    private fun release() {
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        socket = null
        try {
            if (lock?.isHeld == true) lock?.release()
        } catch (_: Exception) {
        }
        lock = null
        running = false
    }

    private fun readName(packet: ByteArray, start: Int): Pair<String, Int>? {
        val labels = ArrayList<String>()
        var index = start
        var jumped = false
        var next = start
        var guard = 0
        while (index < packet.size && guard++ < 40) {
            val len = packet[index].toInt() and 0xff
            if (len == 0) {
                if (!jumped) next = index + 1
                return labels.joinToString(".") to next
            }
            if (len and 0xC0 == 0xC0) {
                if (index + 1 >= packet.size) return null
                if (!jumped) next = index + 2
                index = ((len and 0x3F) shl 8) or (packet[index + 1].toInt() and 0xff)
                jumped = true
                continue
            }
            if (index + 1 + len > packet.size) return null
            labels += packet.copyOfRange(index + 1, index + 1 + len).toString(Charsets.US_ASCII)
            index += 1 + len
            if (!jumped) next = index
        }
        return null
    }
}
