package com.example.unifinder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KuGreen = Color(0xFF006664)
val KuGreenDark = Color(0xFF004D4B)
val KuGreenLight = Color(0xFFE6F2F1)
val KuGrayBg = Color(0xFFF3F6F6)
val KuLine = Color(0xFFD5E0DF)

private val Scheme = lightColorScheme(
    primary = KuGreen,
    onPrimary = Color.White,
    primaryContainer = KuGreenLight,
    onPrimaryContainer = KuGreenDark,
    secondary = KuGreen,
    onSecondary = Color.White,
    secondaryContainer = KuGreenLight,
    onSecondaryContainer = KuGreenDark,
    background = Color.White,
    onBackground = Color(0xFF1B1F1F),
    surface = Color.White,
    onSurface = Color(0xFF1B1F1F),
    surfaceVariant = KuGrayBg,
    onSurfaceVariant = Color(0xFF5F6B6B),
    outline = KuLine,
    outlineVariant = KuLine
)

@Composable
fun UniFinderTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}