package app.lantext.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class WebAssetsTest {
    @Test
    fun appJsParses() {
        val node = nodeBinary()
        assumeTrue("node is required to syntax-check the web shell", node != null)
        val js = webAsset("app.js")
        val proc = ProcessBuilder(node, "--check", js.absolutePath)
            .redirectErrorStream(true)
            .start()
        val out = proc.inputStream.bufferedReader().readText()
        assertEquals(out, 0, proc.waitFor())
    }

    @Test
    fun gifPreviewsStayOnThePhone() {
        val js = webAsset("app.js").readText()
        assertTrue(js.contains("function gifFileSrc"))
        assertTrue(js.contains("/api/v1/gif-file?u="))
        assertTrue(!js.contains("src=\"\${escapeHtml(hit.url)}\""))
    }

    @Test
    fun desktopShellUsesMailCards() {
        val html = webAsset("index.html").readText()
        val css = webAsset("styles.css").readText()
        assertTrue(html.contains("class=\"view mail-shell\""))
        assertTrue(html.contains("id=\"new-btn\" class=\"compose-btn\""))
        assertTrue(html.contains(">Compose<"))
        assertTrue(html.contains("class=\"thread-title\""))
        assertTrue(css.contains("#app-view.mail-shell"))
        assertTrue(css.contains("#new-btn.compose-btn"))
    }

    @Test
    fun threadHeaderLabels() {
        val html = webAsset("index.html").readText()
        val js = webAsset("app.js").readText()
        assertTrue(html.contains("aria-label=\"Copy Number\""))
        assertTrue(html.contains("aria-label=\"Details\""))
        assertTrue(html.contains("aria-label=\"Mark read\""))
        assertTrue(html.contains("aria-label=\"Delete\""))
        assertTrue(html.contains("aria-label=\"Download Thread\""))
        assertTrue(js.contains("class=\"row-pin"))
        assertTrue(js.contains("data-pin="))
        assertTrue(html.contains("id=\"thread-avatar\""))
        assertTrue(html.contains("id=\"convo-menu\""))
        assertTrue(html.contains("id=\"nav-btn\""))
        assertTrue(html.contains("id=\"nav-archived\"") && html.contains("Archived"))
        assertTrue(html.contains("id=\"nav-trash\"") && html.contains("Trash"))
        assertTrue(html.contains("id=\"nav-blocked\"") && html.contains("Blocked"))
        assertTrue(html.contains("id=\"nav-scheduled\"") && html.contains("Scheduled"))
        assertTrue(html.contains("id=\"nav-settings\"") && html.contains("Settings"))
        assertTrue(html.contains("aria-label=\"Mark as unread\""))
        assertTrue(html.contains("aria-label=\"Copy\""))
        assertTrue(html.contains("aria-label=\"Download\""))
        val icons = html.substringAfter("class=\"menu-icons\"")
        val unreadAt = icons.indexOf("id=\"message-menu-unread\"")
        val downloadAt = icons.indexOf("id=\"message-menu-download\"")
        val deleteAt = icons.indexOf("id=\"message-menu-delete\"")
        assertTrue(unreadAt in 0 until downloadAt && downloadAt < deleteAt)
        assertTrue(html.contains("id=\"message-menu-download\""))
        assertTrue(js.contains("function reactionLayout"))
        assertTrue(js.contains("react-chip"))
        assertTrue(!html.contains("id=\"message-menu-share\""))
        assertTrue(!html.contains("id=\"pin-btn\""))
        assertTrue(html.contains(">Close<"))
        assertTrue(js.contains("function copyImage"))
        assertTrue(!html.contains("id=\"voice-btn\""))
        assertTrue(!html.contains("id=\"new-voice-btn\""))
        assertTrue(js.contains("applyLauncherIcon"))
        assertTrue(js.contains("Nothing to react to"))
        assertTrue(!js.contains("voice-btn"))
    }

    @Test
    fun pairFormIsNotDialogMethod() {
        val html = webAsset("index.html").readText()
        assertTrue(html.contains("id=\"pair-form\""))
        assertTrue(
            "method=dialog swallows submit when the form is not inside a <dialog>",
            !html.contains("method=\"dialog\""),
        )
        assertTrue(html.contains("id=\"pair-status\""))
    }

    private fun webAsset(name: String): File {
        val candidates = listOf(
            File("src/main/assets/web/$name"),
            File("app/src/main/assets/web/$name"),
        )
        return candidates.first { it.isFile }
    }

    private fun nodeBinary(): String? {
        val proc = ProcessBuilder("which", "node").redirectErrorStream(true).start()
        if (proc.waitFor() != 0) return null
        return proc.inputStream.bufferedReader().readText().trim().ifBlank { null }
    }
}
