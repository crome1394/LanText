package app.lantext.sms

import org.junit.Assert.assertTrue
import org.junit.Test

class ThreadPdfTest {
    @Test
    fun fileNameSanitizesDisplayName() {
        assertTrue(ThreadPdf.fileName("Alice").matches(Regex("LanText-Alice-\\d{4}-\\d{2}-\\d{2}\\.pdf")))
        assertTrue(ThreadPdf.fileName("Alice / Bob?").startsWith("LanText-Alice-Bob-"))
        assertTrue(ThreadPdf.fileName("   ").startsWith("LanText-thread-"))
    }
}
