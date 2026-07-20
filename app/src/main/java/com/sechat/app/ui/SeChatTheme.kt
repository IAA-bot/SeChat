package com.sechat.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val PrimaryBlue = Color(0xFF007AFF)
internal val PrimaryBlueDark = Color(0xFF0A84FF)
internal val SecureGreen = Color(0xFF4CAF50)
internal val SecureGreenDark = Color(0xFF66BB6A)
internal val ErrorRed = Color(0xFFF44336)
internal val ErrorRedDark = Color(0xFFEF5350)
internal val White = Color.White
internal val SurfaceGray = Color(0xFFF5F5F5)
internal val TextPrimary = Color(0xFF000000)
internal val TextSecondary = Color(0xFF666666)
internal val TextHint = Color(0xFF999999)

internal val DarkBackground = Color(0xFF121212)
internal val DarkSurface = Color(0xFF1E1E1E)
internal val DarkSurfaceVariant = Color(0xFF2C2C2C)
internal val DarkTextPrimary = Color(0xFFE0E0E0)
internal val DarkTextSecondary = Color(0xFFBBBBBB)
internal val DarkTextHint = Color(0xFF999999)

private val LightColorScheme =
    lightColorScheme(
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

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryBlueDark,
        onPrimary = DarkBackground,
        primaryContainer = PrimaryBlueDark.copy(alpha = 0.24f),
        secondary = SecureGreenDark,
        error = ErrorRedDark,
        background = DarkBackground,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        onBackground = DarkTextPrimary,
        onSurface = DarkTextPrimary,
        outline = DarkTextHint,
    )

@Composable
fun SeChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
