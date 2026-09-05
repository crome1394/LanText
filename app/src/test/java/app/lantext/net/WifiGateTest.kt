package app.lantext.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiGateTest {
    @Test
    fun stripsQuotesAndUnknown() {
        assertEquals("crome-IoT", WifiGate.normalizeSsid("\"crome-IoT\""))
        assertEquals("Home", WifiGate.normalizeSsid("Home"))
        assertNull(WifiGate.normalizeSsid("<unknown ssid>"))
        assertNull(WifiGate.normalizeSsid("0x"))
        assertNull(WifiGate.normalizeSsid("  "))
        assertNull(WifiGate.normalizeSsid(null))
    }

    @Test
    fun rememberKeepsLastNameWhenSsidIsRedacted() {
        WifiGate.remember("crome-IoT", "10.74.10.93")
        assertEquals("crome-IoT", WifiGate.lastSsid)
        WifiGate.remember("<unknown ssid>")
        assertEquals("crome-IoT", WifiGate.lastSsid)
        WifiGate.remember("0x")
        assertEquals("crome-IoT", WifiGate.lastSsid)
        WifiGate.remember(null)
        assertNull(WifiGate.lastSsid)
        assertNull(WifiGate.lastSsidIpv4)
    }

    @Test
    fun rememberedSsidOnlyMatchesSameIpv4() {
        WifiGate.remember("crome-IoT", "10.74.10.93")
        assertEquals("crome-IoT", WifiGate.rememberedSsidFor("10.74.10.93"))
        assertNull(WifiGate.rememberedSsidFor("10.0.0.5"))
        assertNull(WifiGate.rememberedSsidFor(null))
        WifiGate.remember(null)
        assertNull(WifiGate.rememberedSsidFor("10.74.10.93"))
    }
}
