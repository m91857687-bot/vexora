package com.voxera.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VoxeraColors = darkColorScheme(
    primary = Color(0xFF35BDEB),
    secondary = Color(0xFFFF4D61),
    background = Color(0xFF101820),
    surface = Color(0xFF1A2731),
    onPrimary = Color(0xFF07151C),
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun VoxeraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VoxeraColors,
        content = content
    )
}
