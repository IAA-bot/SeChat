package com.sechat.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val PrimaryBlue = Color(0xFF007AFF)
internal val SecureGreen = Color(0xFF4CAF50)
internal val ErrorRed = Color(0xFFF44336)
internal val White = Color.White
internal val SurfaceGray = Color(0xFFF5F5F5)
internal val TextPrimary = Color(0xFF000000)
internal val TextSecondary = Color(0xFF666666)
internal val TextHint = Color(0xFF999999)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    primaryContainer = PrimaryBlue.copy(alpha = 0.12f),
    secondary = SecureGreen,
    error = ErrorRed,
    background = White,
    surface = SurfaceGray,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = TextHint,
)

@Composable
fun SeChatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
