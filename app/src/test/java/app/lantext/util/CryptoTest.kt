package app.lantext.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoTest {
    @Test
    fun pinIsEightDigits() {
        val pin = Crypto.randomPin()
        assertEquals(8, pin.length)
        assertTrue(pin.all { it.isDigit() })
    }

    @Test
    fun tokensDifferAndHashIsStable() {
        val a = Crypto.randomToken()
        val b = Crypto.randomToken()
        assertNotEquals(a, b)
        assertEquals(Crypto.sha256Hex("abc"), Crypto.sha256Hex("abc"))
        assertEquals(64, Crypto.sha256Hex("abc").length)
    }
}
