package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PetEmpireDarkColorScheme = darkColorScheme(
    primary = PenyaGold,
    onPrimary = ObsidianBackground,
    primaryContainer = PenyaGoldDark,
    onPrimaryContainer = PenyaGoldLight,
    secondary = FCoinCyan,
    onSecondary = ObsidianBackground,
    secondaryContainer = FCoinCyanDark,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldProfit,
    onTertiary = ObsidianBackground,
    tertiaryContainer = EmeraldProfitContainer,
    onTertiaryContainer = TextPrimary,
    error = CrimsonBurnAlert,
    onError = TextPrimary,
    errorContainer = CrimsonBurnContainer,
    onErrorContainer = TextPrimary,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSubtle
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PetEmpireDarkColorScheme,
        typography = Typography,
        content = content
    )
}
