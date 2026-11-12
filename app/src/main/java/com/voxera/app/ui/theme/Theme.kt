package com.voxera.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.voxera.app.data.preferences.VoxeraThemeMode

val ClassicColors = darkColorScheme(
    primary = Color(0xFF35BDEB),
    secondary = Color(0xFFFF4D61),
    tertiary = Color(0xFF4EE3B8),
    background = Color(0xFF101820),
    surface = Color(0xFF1A2731),
    surfaceVariant = Color(0xFF243340),
    onPrimary = Color(0xFF07151C),
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF9AAAB5)
)

val MidnightColors = darkColorScheme(
    primary = Color(0xFF00E5FF),
    secondary = Color(0xFFFF2A55),
    tertiary = Color(0xFF39FF14),
    background = Color(0xFF07090C),
    surface = Color(0xFF11141A),
    surfaceVariant = Color(0xFF1B202A),
    onPrimary = Color(0xFF001217),
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF889AA7)
)

val RoyalColors = darkColorScheme(
    primary = Color(0xFFC084FC),
    secondary = Color(0xFFFFB703),
    tertiary = Color(0xFFF472B6),
    background = Color(0xFF0E0B1A),
    surface = Color(0xFF1A152E),
    surfaceVariant = Color(0xFF272044),
    onPrimary = Color(0xFF1A0830),
    onSecondary = Color(0xFF241600),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFAFA3C7)
)

val SystemLightColors = lightColorScheme(
    primary = Color(0xFF0288D1),
    secondary = Color(0xFFE53935),
    tertiary = Color(0xFF00897B),
    background = Color(0xFFF4F6F8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2E8F0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF101820),
    onSurface = Color(0xFF101820),
    onSurfaceVariant = Color(0xFF5A6E7C)
)

val LocalThemeMode = staticCompositionLocalOf { VoxeraThemeMode.CLASSIC }

@Composable
fun VoxeraTheme(
    themeMode: VoxeraThemeMode = VoxeraThemeMode.CLASSIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (themeMode) {
        VoxeraThemeMode.CLASSIC -> ClassicColors
        VoxeraThemeMode.MIDNIGHT -> MidnightColors
        VoxeraThemeMode.ROYAL -> RoyalColors
        VoxeraThemeMode.SYSTEM -> if (darkTheme) ClassicColors else SystemLightColors
    }

    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
