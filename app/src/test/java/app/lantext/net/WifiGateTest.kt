package app.lantext.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiGateTest {
    @Test
    fun stripsQuotesAndUnknown() {
        assertEquals("TestNet", WifiGate.normalizeSsid("\"TestNet\""))
        assertEquals("Home", WifiGate.normalizeSsid("Home"))
        assertNull(WifiGate.normalizeSsid("<unknown ssid>"))
        assertNull(WifiGate.normalizeSsid("0x"))
        assertNull(WifiGate.normalizeSsid("  "))
        assertNull(WifiGate.normalizeSsid(null))
    }

    @Test
    fun rememberKeepsLastNameWhenSsidIsRedacted() {
        WifiGate.remember("TestNet", "192.168.50.20")
        assertEquals("TestNet", WifiGate.lastSsid)
        WifiGate.remember("<unknown ssid>")
        assertEquals("TestNet", WifiGate.lastSsid)
        WifiGate.remember("0x")
        assertEquals("TestNet", WifiGate.lastSsid)
        WifiGate.remember(null)
        assertNull(WifiGate.lastSsid)
        assertNull(WifiGate.lastSsidIpv4)
    }

    @Test
    fun rememberedSsidOnlyMatchesSameIpv4() {
        WifiGate.remember("TestNet", "192.168.50.20")
        assertEquals("TestNet", WifiGate.rememberedSsidFor("192.168.50.20"))
        assertNull(WifiGate.rememberedSsidFor("10.0.0.5"))
        assertNull(WifiGate.rememberedSsidFor(null))
        WifiGate.remember(null)
        assertNull(WifiGate.rememberedSsidFor("192.168.50.20"))
    }

    @Test
    fun dhcpOnTheSameSubnetKeepsTheNetwork() {
        WifiGate.remember(null)
        assertTrue(WifiGate.sameSubnet24("192.168.50.20", "192.168.50.40"))
        assertFalse(WifiGate.sameSubnet24("192.168.50.20", "192.168.1.20"))
        assertFalse(WifiGate.sameSubnet24("192.168.50", "192.168.50.40"))
        assertFalse(WifiGate.sameSubnet24(null, "192.168.50.40"))
        WifiGate.remember("Home", "192.168.50.20")
        WifiGate.noteStationIp("192.168.50.40")
        assertEquals("Home", WifiGate.lastSsid)
        assertEquals("Home", WifiGate.rememberedSsidFor("192.168.50.40"))
        WifiGate.noteStationIp("192.168.1.20")
        assertNull(WifiGate.lastSsid)
        assertNull(WifiGate.lastSsidIpv4)
        WifiGate.remember(null)
    }
}
