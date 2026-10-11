package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val isDark: Boolean
) {
    OBSIDIAN_SAPPHIRE(
        title = "Obsidian Sapphire",
        subtitle = "Dark Studio • Electric Cyan Glow",
        primaryColor = ObsidianPrimary,
        backgroundColor = ObsidianBg,
        surfaceColor = ObsidianSurface,
        isDark = true
    ),
    CHAMPAGNE_GOLD(
        title = "Champagne Gold",
        subtitle = "Luxury Leica • Warm Amber Gold",
        primaryColor = ChampagnePrimary,
        backgroundColor = ChampagneBg,
        surfaceColor = ChampagneSurface,
        isDark = true
    ),
    CYBERPUNK_NEON(
        title = "Cyberpunk Synthwave",
        subtitle = "Futuristic • Neon Magenta & Violet",
        primaryColor = CyberPrimary,
        backgroundColor = CyberBg,
        surfaceColor = CyberSurface,
        isDark = true
    ),
    ALABASTER_LIGHT(
        title = "Alabaster Studio",
        subtitle = "Clean Gallery • Editorial Light",
        primaryColor = AlabasterPrimary,
        backgroundColor = AlabasterBg,
        surfaceColor = AlabasterSurface,
        isDark = false
    )
}

val LocalAppTheme = compositionLocalOf { AppThemeMode.OBSIDIAN_SAPPHIRE }
val LocalThemeSwitcher = compositionLocalOf<(AppThemeMode) -> Unit> { {} }
