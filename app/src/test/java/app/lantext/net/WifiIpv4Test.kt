package app.lantext.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiIpv4Test {
    @Test
    fun prefersDhcpWifiAddressOverOtherPrivateIps() {
        val picked = WifiIpv4.select(
            listOf(
                Ipv4Candidate("172.16.8.1", 24, "ap0"),
                Ipv4Candidate("172.16.9.60", 32, "rmnet1"),
                Ipv4Candidate("192.168.50.20", 24, "wlan0"),
            ),
            dhcpHost = "192.168.50.20",
            gatewayHost = "192.168.50.1",
        )
        assertEquals("192.168.50.20", picked)
    }

    @Test
    fun dhcpWinsEvenIfMissingFromLinkAddresses() {
        val picked = WifiIpv4.select(
            listOf(Ipv4Candidate("172.16.8.1", 24, "wlan0")),
            dhcpHost = "192.168.50.20",
            gatewayHost = "192.168.50.1",
        )
        assertEquals("192.168.50.20", picked)
    }

    @Test
    fun skipsHotspotAndCellularWhenNoDhcp() {
        val picked = WifiIpv4.select(
            listOf(
                Ipv4Candidate("172.16.8.1", 24, "ap0"),
                Ipv4Candidate("172.16.9.60", 32, "rmnet1"),
                Ipv4Candidate("192.168.50.20", 24, "wlan0"),
            ),
        )
        assertEquals("192.168.50.20", picked)
    }

    @Test
    fun skipsGatewayAndSlash32Tunnels() {
        val picked = WifiIpv4.select(
            listOf(
                Ipv4Candidate("192.168.50.1", 24, "wlan0"),
                Ipv4Candidate("172.16.9.60", 32, "wlan0"),
                Ipv4Candidate("192.168.50.20", 24, "wlan0"),
            ),
            gatewayHost = "192.168.50.1",
        )
        assertEquals("192.168.50.20", picked)
    }

    @Test
    fun littleEndianDhcpInt() {
        // 192.168.50.20 in WifiManager little-endian form
        val packed = 192 or (168 shl 8) or (50 shl 16) or (20 shl 24)
        assertEquals("192.168.50.20", WifiIpv4.fromLittleEndian(packed))
        assertNull(WifiIpv4.fromLittleEndian(0))
    }

    @Test
    fun staIfaceFilter() {
        assertTrue(WifiIpv4.isStaIface("wlan0"))
        assertTrue(WifiIpv4.isStaIface("swlan0"))
        assertFalse(WifiIpv4.isStaIface("ap0"))
        assertFalse(WifiIpv4.isStaIface("rmnet1"))
        assertFalse(WifiIpv4.isStaIface("ipsec1382"))
    }
}
