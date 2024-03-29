package dev.develsinthedetails.eatpoopyoucat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val lightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

interface WarningColors {
    val warning: Color
    val onWarning: Color
    val warningContainer: Color
    val onWarningContainer: Color
}

private class LightWarnings : WarningColors {
    override val warning: Color = light_warning
    override val onWarning = light_onWarning
    override val warningContainer = light_warningContainer
    override val onWarningContainer = light_onWarningContainer
}

private class DarkWarnings : WarningColors {
    override val warning: Color = dark_warning
    override val onWarning = dark_onWarning
    override val warningContainer = dark_warningContainer
    override val onWarningContainer = dark_onWarningContainer
}

private val darkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        lightColors
    } else {
        darkColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Composable
fun secondaryButtonColors() = ButtonDefaults.buttonColors(
    MaterialTheme.colorScheme.secondaryContainer,
    MaterialTheme.colorScheme.onSecondaryContainer,
)

@Composable
fun primaryButtonColors() = ButtonDefaults.buttonColors(
    MaterialTheme.colorScheme.primaryContainer,
    MaterialTheme.colorScheme.onPrimaryContainer
)

@Composable
fun warningColors(useDarkTheme: Boolean = isSystemInDarkTheme()): WarningColors {
    return if (!useDarkTheme) {
        LightWarnings()
    } else {
        DarkWarnings()
    }
}

val ColorScheme.drawingBackground: Color
    @Composable
    get() {
        return if (isSystemInDarkTheme()) {
            md_theme_dark_drawing_background
        } else {
            md_theme_light_drawing_background
        }
    }

val ColorScheme.drawingPen: Color
    @Composable
    get() {
        return if (isSystemInDarkTheme()) {
            md_theme_dark_drawing_pen
        } else {
            md_theme_light_drawing_pen
        }
    }