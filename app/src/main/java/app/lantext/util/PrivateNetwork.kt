package app.lantext.util

import java.net.InetAddress
import java.net.Inet4Address
import java.net.Inet6Address

object PrivateNetwork {
    fun isPrivateHost(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        val cleaned = host.trim().removePrefix("/").substringBefore("%")
        if (cleaned == "localhost" || cleaned == "127.0.0.1" || cleaned == "::1") return true
        return try {
            isPrivateAddress(InetAddress.getByName(cleaned))
        } catch (_: Exception) {
            false
        }
    }

    fun isPrivateAddress(address: InetAddress): Boolean {
        if (address.isLoopbackAddress || address.isLinkLocalAddress) return true
        return when (address) {
            is Inet4Address -> isPrivateV4(address.address)
            is Inet6Address -> address.isSiteLocalAddress || isUniqueLocal(address)
            else -> false
        }
    }

    private fun isPrivateV4(bytes: ByteArray): Boolean {
        if (bytes.size != 4) return false
        val a = bytes[0].toInt() and 0xff
        val b = bytes[1].toInt() and 0xff
        return when {
            a == 10 -> true
            a == 172 && b in 16..31 -> true
            a == 192 && b == 168 -> true
            a == 127 -> true
            else -> false
        }
    }

    private fun isUniqueLocal(address: Inet6Address): Boolean {
        val first = address.address[0].toInt() and 0xff
        return first == 0xfc || first == 0xfd
    }
}
