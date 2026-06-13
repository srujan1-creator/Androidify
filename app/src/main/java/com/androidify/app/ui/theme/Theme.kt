package com.androidify.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AndroidGreen,
    onPrimary = OnAndroidGreen,
    primaryContainer = AndroidGreenContainer,
    onPrimaryContainer = AndroidGreenLight,
    secondary = DeepBlue,
    onSecondary = OnDeepBlue,
    secondaryContainer = DeepBlueDark,
    onSecondaryContainer = DeepBlueLight,
    tertiary = WarmOrange,
    onTertiary = OnWarmOrange,
    tertiaryContainer = WarmOrangeDark,
    onTertiaryContainer = WarmOrangeLight,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = NeutralGray,
    error = ErrorRed,
    onError = NeutralBlack,
    outline = NeutralGray
)

private val LightColorScheme = lightColorScheme(
    primary = AndroidGreenDark,
    onPrimary = NeutralWhite,
    primaryContainer = AndroidGreenLight,
    onPrimaryContainer = OnAndroidGreen,
    secondary = DeepBlue,
    onSecondary = NeutralWhite,
    secondaryContainer = DeepBlueLight,
    onSecondaryContainer = DeepBlueDark,
    tertiary = WarmOrangeDark,
    onTertiary = NeutralWhite,
    tertiaryContainer = WarmOrangeLight,
    onTertiaryContainer = OnWarmOrange,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = NeutralDarkGray,
    error = ErrorRedDark,
    onError = NeutralWhite,
    outline = NeutralGray
)

/**
 * Androidify Material 3 theme with Android-green color scheme.
 * Supports dynamic color on Android 12+ with fallback to custom palette.
 */
@Composable
fun AndroidifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to keep brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AndroidifyTypography,
        content = content
    )
}
