package app.lantext.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionTokenTest {
    @Test
    fun bearerWinsAndABlankBearerDoesNotFallThrough() {
        val headers = mapOf(
            "authorization" to "Bearer phone-token",
            "cookie" to "lt_session=cookie-token",
        )
        assertEquals("phone-token", GatewayServer.presentedToken(headers))
        assertNull(GatewayServer.presentedToken(mapOf("authorization" to "Bearer ")))
    }

    @Test
    fun cookieIsUsedWhenThereIsNoAuthorizationHeader() {
        val headers = mapOf("cookie" to "theme=dark; lt_session=cookie-token; other=1")
        assertEquals("cookie-token", GatewayServer.presentedToken(headers))
        assertNull(GatewayServer.presentedToken(emptyMap()))
        assertNull(GatewayServer.presentedToken(null))
    }

    @Test
    fun sessionCookieStaysOnThisHost() {
        assertEquals(
            "lt_session=phone-token; Path=/; Max-Age=34560000; Secure; HttpOnly; SameSite=Strict",
            GatewayServer.sessionCookie("phone-token"),
        )
    }
}
