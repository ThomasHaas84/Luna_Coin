package de.meson_labs.luna_coin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LunaDarkColorScheme = darkColorScheme(
    primary = LunaPrimary,
    onPrimary = Color.White,
    primaryContainer = LunaPrimaryDark,
    onPrimaryContainer = LunaTextLight,

    secondary = LunaPrimaryLight,
    onSecondary = LunaNightBlue,
    secondaryContainer = LunaMediumBlueGray,
    onSecondaryContainer = LunaTextLight,

    tertiary = LunaCoinGold,
    onTertiary = LunaNightBlue,
    tertiaryContainer = LunaCoinGold,
    onTertiaryContainer = LunaNightBlue,

    background = LunaNightBlue,
    onBackground = LunaTextLight,

    surface = LunaDarkBlueGray,
    onSurface = LunaTextLight,

    surfaceVariant = LunaMediumBlueGray,
    onSurfaceVariant = LunaTextMuted,

    outline = LunaGrayDark,
    outlineVariant = LunaGrayLight,

    error = LunaError,
    onError = Color.Black
)

@Composable
fun LunaCoinTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LunaDarkColorScheme,
        typography = Typography,
        content = content
    )
}