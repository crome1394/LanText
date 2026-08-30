package app.lantext.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivateNetworkTest {
    @Test
    fun rfc1918AndLoopbackArePrivate() {
        assertTrue(PrivateNetwork.isPrivateHost("10.74.10.148"))
        assertTrue(PrivateNetwork.isPrivateHost("192.168.1.20"))
        assertTrue(PrivateNetwork.isPrivateHost("172.16.0.4"))
        assertTrue(PrivateNetwork.isPrivateHost("127.0.0.1"))
        assertTrue(PrivateNetwork.isPrivateHost("localhost"))
        assertTrue(PrivateNetwork.isPrivateHost("fd12:3456::1"))
    }

    @Test
    fun publicAndBlankAreRejected() {
        assertFalse(PrivateNetwork.isPrivateHost("8.8.8.8"))
        assertFalse(PrivateNetwork.isPrivateHost("1.1.1.1"))
        assertFalse(PrivateNetwork.isPrivateHost(""))
        assertFalse(PrivateNetwork.isPrivateHost(null))
        assertFalse(PrivateNetwork.isPrivateHost("not an ip"))
    }
}
