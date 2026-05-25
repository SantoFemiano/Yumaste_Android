package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryViolet,
    tertiary = AccentCoral,
    background = BackgroundSlate,
    surface = SurfaceWhite,
    onPrimary = BackgroundSlate,
    onSecondary = BackgroundSlate,
    onBackground = TextSlateDark,
    onSurface = TextSlateDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryViolet,
    tertiary = AccentCoral,
    background = BackgroundSlate,
    surface = SurfaceWhite,
    onPrimary = BackgroundSlate,
    onSecondary = BackgroundSlate,
    onBackground = TextSlateDark,
    onSurface = TextSlateDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
