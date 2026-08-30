package app.lantext.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListenPortTest {
    @Test
    fun acceptsUserPorts() {
        assertTrue(ListenPort.isValid(1024))
        assertTrue(ListenPort.isValid(8743))
        assertTrue(ListenPort.isValid(65535))
        assertEquals(8743, ListenPort.parse("8743"))
        assertEquals(8080, ListenPort.parse(" 8080 "))
    }

    @Test
    fun rejectsReservedAndJunk() {
        assertFalse(ListenPort.isValid(80))
        assertFalse(ListenPort.isValid(443))
        assertFalse(ListenPort.isValid(0))
        assertFalse(ListenPort.isValid(65536))
        assertNull(ListenPort.parse(""))
        assertNull(ListenPort.parse("abc"))
        assertNull(ListenPort.parse("80"))
    }
}
