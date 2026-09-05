package app.lantext.widget

import android.content.Context
import android.content.res.Configuration
import app.lantext.data.AppSettings

data class WidgetColors(
    val background: Int,
    val title: Int,
    val status: Int,
    val button: Int,
    val buttonText: Int,
)

object WidgetTheme {
    val PALETTES = setOf(
        "fern", "ocean", "dusk", "ember", "slate", "sakura", "meadow", "nord", "contrast",
    )
    val MODES = setOf("auto", "light", "dark")

    fun normalizePalette(raw: String?): String {
        val id = raw?.trim()?.lowercase().orEmpty()
        return if (id in PALETTES) id else "fern"
    }

    fun normalizeMode(raw: String?): String {
        val id = raw?.trim()?.lowercase().orEmpty()
        return if (id in MODES) id else "auto"
    }

    fun colors(context: Context, settings: AppSettings): WidgetColors {
        val dark = when (normalizeMode(settings.webMode)) {
            "dark" -> true
            "light" -> false
            else -> {
                val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                night == Configuration.UI_MODE_NIGHT_YES
            }
        }
        return colors(normalizePalette(settings.webPalette), dark)
    }

    fun colors(palette: String, dark: Boolean): WidgetColors = when (palette) {
        "ocean" -> if (dark) {
            WidgetColors(rgb(0x132230), rgb(0xE6F1F7), rgb(0x8AA8B8), rgb(0x0369A1), rgb(0xF0F9FF))
        } else {
            WidgetColors(rgb(0x0B6E99), rgb(0xFFFFFF), rgb(0xD6EAF4), rgb(0xFFFFFF), rgb(0x0B6E99))
        }
        "dusk" -> if (dark) {
            WidgetColors(rgb(0x221B2E), rgb(0xF0EAF8), rgb(0xB0A3C9), rgb(0x6D28D9), rgb(0xF5F3FF))
        } else {
            WidgetColors(rgb(0x5B3CC4), rgb(0xFFFFFF), rgb(0xE8E0F8), rgb(0xFFFFFF), rgb(0x5B3CC4))
        }
        "ember" -> if (dark) {
            WidgetColors(rgb(0x2A1A14), rgb(0xF8EDE6), rgb(0xC4A494), rgb(0xC2410C), rgb(0xFFF7ED))
        } else {
            WidgetColors(rgb(0xC2410C), rgb(0xFFFFFF), rgb(0xF8E4D6), rgb(0xFFFFFF), rgb(0xC2410C))
        }
        "slate" -> if (dark) {
            WidgetColors(rgb(0x1E293B), rgb(0xF1F5F9), rgb(0x94A3B8), rgb(0x334155), rgb(0xF8FAFC))
        } else {
            WidgetColors(rgb(0x334155), rgb(0xFFFFFF), rgb(0xE2E8F0), rgb(0xFFFFFF), rgb(0x334155))
        }
        "sakura" -> if (dark) {
            WidgetColors(rgb(0x2A1520), rgb(0xFDECF3), rgb(0xD4A0B5), rgb(0xBE185D), rgb(0xFFF1F5))
        } else {
            WidgetColors(rgb(0xBE185D), rgb(0xFFFFFF), rgb(0xF8D5E4), rgb(0xFFFFFF), rgb(0xBE185D))
        }
        "meadow" -> if (dark) {
            WidgetColors(rgb(0x1A2418), rgb(0xEAF6E8), rgb(0xA3C49A), rgb(0x3F7D20), rgb(0xF7FEE7))
        } else {
            WidgetColors(rgb(0x3F7D20), rgb(0xFFFFFF), rgb(0xDCEFCF), rgb(0xFFFFFF), rgb(0x3F7D20))
        }
        "nord" -> if (dark) {
            WidgetColors(rgb(0x3B4252), rgb(0xECEFF4), rgb(0xA3B0C2), rgb(0x5E81AC), rgb(0xECEFF4))
        } else {
            WidgetColors(rgb(0x5E81AC), rgb(0xFFFFFF), rgb(0xD8E4EE), rgb(0xFFFFFF), rgb(0x5E81AC))
        }
        "contrast" -> if (dark) {
            WidgetColors(rgb(0x000000), rgb(0xFFFFFF), rgb(0xC8C8C8), rgb(0xFFFFFF), rgb(0x000000))
        } else {
            WidgetColors(rgb(0x000000), rgb(0xFFFFFF), rgb(0xE6E6E6), rgb(0xFFFFFF), rgb(0x000000))
        }
        else -> if (dark) {
            WidgetColors(rgb(0x1A1F1C), rgb(0xE6EDE8), rgb(0x9AADA3), rgb(0x1F8A70), rgb(0xFFFFFF))
        } else {
            WidgetColors(rgb(0x156B57), rgb(0xFFFFFF), rgb(0xD6EEE6), rgb(0xFFFFFF), rgb(0x156B57))
        }
    }

    private fun rgb(rgb: Int): Int = (0xFF shl 24) or (rgb and 0xFFFFFF)
}
