package app.lantext.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF1F8A70)
private val GreenDark = Color(0xFF156B57)
private val LightBg = Color(0xFFF4F7F5)
private val DarkBg = Color(0xFF101412)

@Composable
fun LanTextTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) {
        darkColorScheme(
            primary = Green,
            onPrimary = Color.White,
            secondary = Green,
            background = DarkBg,
            surface = Color(0xFF1A1F1C),
            onBackground = Color(0xFFE6EDE8),
            onSurface = Color(0xFFE6EDE8),
        )
    } else {
        lightColorScheme(
            primary = GreenDark,
            onPrimary = Color.White,
            secondary = Green,
            background = LightBg,
            surface = Color.White,
            onBackground = Color(0xFF14201A),
            onSurface = Color(0xFF14201A),
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
