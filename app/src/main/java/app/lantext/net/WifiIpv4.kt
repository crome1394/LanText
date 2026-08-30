package app.lantext.net

import app.lantext.util.PrivateNetwork

internal data class Ipv4Candidate(
    val host: String,
    val prefix: Int = 24,
    val iface: String? = null,
)

/**
 * Picks the Wi-Fi *station* IPv4 used for pairing (the phone's address on
 * the LAN). About-screen examples stay generic; this is the live address.
 */
internal object WifiIpv4 {
    fun isStaIface(name: String?): Boolean {
        if (name.isNullOrBlank()) return true
        val n = name.lowercase()
        if (n.startsWith("p2p") || n.startsWith("ap") || n.startsWith("softap") ||
            n.startsWith("dummy") || n.startsWith("rmnet") || n.startsWith("ccmni") ||
            n.startsWith("tun") || n.startsWith("wg") || n.startsWith("ipsec") ||
            n.startsWith("vpn") || n.contains("clat")
        ) {
            return false
        }
        return n.startsWith("wlan") || n.startsWith("wifi") || n.startsWith("swlan")
    }

    fun fromLittleEndian(ip: Int): String? {
        if (ip == 0) return null
        return "${ip and 0xff}.${(ip shr 8) and 0xff}.${(ip shr 16) and 0xff}.${(ip shr 24) and 0xff}"
    }

    fun select(
        candidates: List<Ipv4Candidate>,
        dhcpHost: String? = null,
        gatewayHost: String? = null,
    ): String? {
        val usable = candidates.filter { c ->
            PrivateNetwork.isPrivateHost(c.host) &&
                c.prefix in 8..30 &&
                c.host != gatewayHost &&
                isStaIface(c.iface)
        }
        if (!dhcpHost.isNullOrBlank() && PrivateNetwork.isPrivateHost(dhcpHost) && dhcpHost != gatewayHost) {
            usable.find { it.host == dhcpHost }?.let { return it.host }
            return dhcpHost
        }
        if (usable.isEmpty()) return null
        val lan = usable.filter { it.prefix in 16..24 }.ifEmpty { usable }
        return lan.firstOrNull { it.iface?.lowercase()?.startsWith("wlan") == true }?.host
            ?: lan.first().host
    }
}
