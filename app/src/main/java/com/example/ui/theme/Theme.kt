package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = DarkNavyBase,
    primaryContainer = RoyalPurpleDark,
    onPrimaryContainer = RoyalGold,
    secondary = LudoBlue,
    onSecondary = CleanWhite,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = LudoRed,
    onTertiary = CleanWhite,
    background = DarkNavyBase,
    onBackground = TextPrimaryDark,
    surface = DarkSurfaceCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder
)

private val LightColorScheme = darkColorScheme( // Gaming apps excel with deep rich royal theme
    primary = RoyalGold,
    onPrimary = DarkNavyBase,
    primaryContainer = RoyalPurpleDark,
    onPrimaryContainer = RoyalGold,
    secondary = LudoBlue,
    onSecondary = CleanWhite,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = LudoRed,
    onTertiary = CleanWhite,
    background = DarkNavyBase,
    onBackground = TextPrimaryDark,
    surface = DarkSurfaceCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = DarkNavyBase.toArgb()
                it.navigationBarColor = DarkNavyBase.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
