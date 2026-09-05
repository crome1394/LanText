package app.lantext.widget

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
