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
}
