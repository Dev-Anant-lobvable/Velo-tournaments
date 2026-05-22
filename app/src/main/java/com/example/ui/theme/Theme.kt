package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberpunkYellow,
    secondary = ElectricBlue,
    tertiary = NeonGreen,
    background = DeepSpaceBlack,
    surface = DarkSurface,
    onPrimary = DeepSpaceBlack,
    onSecondary = DeepSpaceBlack,
    onTertiary = DeepSpaceBlack,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = NeonRed,
    onError = TextWhite,
    surfaceVariant = DarkSurfaceGlass,
    onSurfaceVariant = TextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force dark theme as per user requirement (Gamer / Cyberpunk UI)
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
