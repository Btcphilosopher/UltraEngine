package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProfessionalPolishColorScheme = lightColorScheme(
    primary = PolishPurple,
    onPrimary = Color.White,
    primaryContainer = PolishPurpleContainer,
    onPrimaryContainer = PolishPurpleDark,
    secondary = PolishBlue,
    onSecondary = Color.White,
    secondaryContainer = PolishBlueContainer,
    onSecondaryContainer = PolishBlue,
    tertiary = PolishGreen,
    onTertiary = Color.White,
    background = PolishCanvas,
    onBackground = PolishTextPrimary,
    surface = PolishSurface,
    onSurface = PolishTextPrimary,
    surfaceVariant = PolishSurfaceContainer,
    onSurfaceVariant = PolishTextSecondary,
    outline = PolishBorder,
    outlineVariant = PolishDivider,
    error = PolishRed,
    onError = Color.White,
    errorContainer = PolishRedContainer,
    onErrorContainer = PolishRedDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ProfessionalPolishColorScheme,
        typography = Typography,
        content = content
    )
}


