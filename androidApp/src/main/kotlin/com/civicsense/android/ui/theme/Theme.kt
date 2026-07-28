package com.civicsense.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CivicSenseColors = darkColorScheme(
    primary = Color(0xFF00D4AA),
    onPrimary = Color(0xFF00382A),
    secondary = Color(0xFF7C4DFF),
    background = Color(0xFF1A1A2E),
    surface = Color(0xFF262640),
    onBackground = Color(0xFFE8E8F0),
    onSurface = Color(0xFFE8E8F0),
    error = Color(0xFFFF5252),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun CivicSenseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CivicSenseColors,
        content = content
    )
}
