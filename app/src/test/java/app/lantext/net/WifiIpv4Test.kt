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
                Ipv4Candidate("10.215.173.1", 24, "ap0"),
                Ipv4Candidate("10.221.35.60", 32, "rmnet1"),
                Ipv4Candidate("10.74.10.93", 24, "wlan0"),
            ),
            dhcpHost = "10.74.10.93",
            gatewayHost = "10.74.10.1",
        )
        assertEquals("10.74.10.93", picked)
    }

    @Test
    fun dhcpWinsEvenIfMissingFromLinkAddresses() {
        val picked = WifiIpv4.select(
            listOf(Ipv4Candidate("10.215.173.1", 24, "wlan0")),
            dhcpHost = "10.74.10.93",
            gatewayHost = "10.74.10.1",
        )
        assertEquals("10.74.10.93", picked)
    }

    @Test
    fun skipsHotspotAndCellularWhenNoDhcp() {
        val picked = WifiIpv4.select(
            listOf(
                Ipv4Candidate("10.215.173.1", 24, "ap0"),
                Ipv4Candidate("10.221.35.60", 32, "rmnet1"),
                Ipv4Candidate("10.74.10.93", 24, "wlan0"),
            ),
        )
        assertEquals("10.74.10.93", picked)
    }

    @Test
    fun skipsGatewayAndSlash32Tunnels() {
        val picked = WifiIpv4.select(
            listOf(
                Ipv4Candidate("10.74.10.1", 24, "wlan0"),
                Ipv4Candidate("10.221.35.60", 32, "wlan0"),
                Ipv4Candidate("10.74.10.93", 24, "wlan0"),
            ),
            gatewayHost = "10.74.10.1",
        )
        assertEquals("10.74.10.93", picked)
    }

    @Test
    fun littleEndianDhcpInt() {
        // 10.74.10.93 in WifiManager little-endian form
        val packed = 10 or (74 shl 8) or (10 shl 16) or (93 shl 24)
        assertEquals("10.74.10.93", WifiIpv4.fromLittleEndian(packed))
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
