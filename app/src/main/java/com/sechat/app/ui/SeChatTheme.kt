package com.sechat.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

internal val PrimaryBlue = androidx.compose.ui.graphics.Color(0xFF007AFF)
internal val SecureGreen = androidx.compose.ui.graphics.Color(0xFF4CAF50)
internal val ErrorRed = androidx.compose.ui.graphics.Color(0xFFF44336)
internal val White = androidx.compose.ui.graphics.Color.White
internal val SurfaceGray = androidx.compose.ui.graphics.Color(0xFFF5F5F5)
internal val TextPrimary = androidx.compose.ui.graphics.Color(0xFF000000)
internal val TextSecondary = androidx.compose.ui.graphics.Color(0xFF666666)
internal val TextHint = androidx.compose.ui.graphics.Color(0xFF999999)
