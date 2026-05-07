package com.example.milos_achats.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary              = MilosBlue,
    onPrimary            = Color.White,
    primaryContainer     = MilosBlueContainer,
    onPrimaryContainer   = MilosBlueDeep,

    secondary            = MilosCoffee,
    onSecondary          = Color.White,
    secondaryContainer   = MilosCoffeeContainer,
    onSecondaryContainer = MilosCoffeeOnLight,

    tertiary             = ConfirmedGreen,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFD8F5D8),
    onTertiaryContainer  = Color(0xFF0A2E0A),

    background           = MilosSurface,
    surface              = Color.White,
    onBackground         = Color(0xFF1A1F2E),
    onSurface            = Color(0xFF1A1F2E),

    surfaceVariant       = MilosBlueLight,
    onSurfaceVariant     = Color(0xFF3D5068),
    outline              = Color(0xFF8BA8C8),
    outlineVariant       = Color(0xFFCCDDEF),

    error                = Color(0xFFB3261E),
    onError              = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary              = MilosBlue80,
    onPrimary            = MilosBlueDark,
    primaryContainer     = MilosBlueDark,
    onPrimaryContainer   = MilosBlueContainer,

    secondary            = MilosCoffee80,
    onSecondary          = MilosCoffeeDark,
    secondaryContainer   = Color(0xFF6B3C00),
    onSecondaryContainer = MilosCoffeeContainer,

    tertiary             = Color(0xFF66BB6A),
    onTertiary           = Color(0xFF0A2E0A),
    tertiaryContainer    = Color(0xFF1B5E20),
    onTertiaryContainer  = Color(0xFFD8F5D8),

    background           = MilosSurfaceDark,
    surface              = Color(0xFF1E2A3D),
    onBackground         = Color(0xFFDEE8F5),
    onSurface            = Color(0xFFDEE8F5),

    surfaceVariant       = Color(0xFF253348),
    onSurfaceVariant     = Color(0xFFAAC0D8),
    outline              = Color(0xFF5A7A9E),
    outlineVariant       = Color(0xFF2E4460),
)

@Composable
fun MilosachatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}
