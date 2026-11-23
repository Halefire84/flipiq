package com.crtc.flipiq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    surface = Color(0xFFF8F9FA),
    background = Color(0xFFFFFFFF),
    error = Color(0xFFB71C1C)
)

@Composable
fun FlipIQTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
