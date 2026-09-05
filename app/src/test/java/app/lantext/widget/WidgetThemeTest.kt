package app.lantext.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WidgetThemeTest {
    @Test
    fun normalizesPaletteAndMode() {
        assertEquals("fern", WidgetTheme.normalizePalette(null))
        assertEquals("ocean", WidgetTheme.normalizePalette("Ocean"))
        assertEquals("fern", WidgetTheme.normalizePalette("nope"))
        assertEquals("auto", WidgetTheme.normalizeMode(""))
        assertEquals("dark", WidgetTheme.normalizeMode("DARK"))
    }

    @Test
    fun fernLightMatchesLegacyWidgetGreen() {
        val colors = WidgetTheme.colors("fern", dark = false)
        assertEquals(0xFF156B57.toInt(), colors.background)
        assertEquals(0xFFFFFFFF.toInt(), colors.title)
        assertEquals(0xFF156B57.toInt(), colors.buttonText)
    }

    @Test
    fun toggleButtonDrawableIsPill() {
        val xml = widgetResource("drawable/widget_button.xml")
        assertTrue(xml.contains("android:shape=\"rectangle\""))
        assertTrue(xml.contains("android:radius=\"999dp\""))
    }

    private fun widgetResource(rel: String): String {
        val candidates = listOf(
            File("src/main/res/$rel"),
            File("app/src/main/res/$rel"),
        )
        return candidates.first { it.isFile }.readText()
    }
}
