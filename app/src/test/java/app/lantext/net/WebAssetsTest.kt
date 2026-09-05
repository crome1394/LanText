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
    fun threadHeaderLabels() {
        val html = webAsset("index.html").readText()
        assertTrue(html.contains(">Copy Number<"))
        assertTrue(html.contains(">Download Thread<"))
        assertTrue(html.contains("id=\"pin-btn\""))
        assertTrue(html.contains("id=\"thread-avatar\""))
        assertTrue(html.contains("id=\"convo-menu\""))
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
