package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

fun getThemeColorScheme(themeMode: AppThemeMode): ColorScheme {
    return when (themeMode) {
        AppThemeMode.OBSIDIAN_SAPPHIRE -> darkColorScheme(
            primary = ObsidianPrimary,
            onPrimary = Color(0xFF00363D),
            primaryContainer = Color(0xFF004F58),
            onPrimaryContainer = Color(0xFF97F0FF),
            secondary = ObsidianSecondary,
            onSecondary = Color(0xFF003544),
            tertiary = ObsidianAccent,
            background = ObsidianBg,
            onBackground = TextPrimaryDark,
            surface = ObsidianSurface,
            onSurface = TextPrimaryDark,
            surfaceVariant = ObsidianSurfaceVariant,
            onSurfaceVariant = TextSecondaryDark,
            outline = ObsidianBorder
        )
        AppThemeMode.CHAMPAGNE_GOLD -> darkColorScheme(
            primary = ChampagnePrimary,
            onPrimary = Color(0xFF452B00),
            primaryContainer = Color(0xFF633F00),
            onPrimaryContainer = Color(0xFFFFDDB3),
            secondary = ChampagneSecondary,
            onSecondary = Color(0xFF412D00),
            tertiary = ChampagneAccent,
            background = ChampagneBg,
            onBackground = TextPrimaryDark,
            surface = ChampagneSurface,
            onSurface = TextPrimaryDark,
            surfaceVariant = ChampagneSurfaceVariant,
            onSurfaceVariant = TextSecondaryDark,
            outline = ChampagneBorder
        )
        AppThemeMode.CYBERPUNK_NEON -> darkColorScheme(
            primary = CyberPrimary,
            onPrimary = Color(0xFF40001B),
            primaryContainer = Color(0xFF63002D),
            onPrimaryContainer = Color(0xFFFFD9E2),
            secondary = CyberSecondary,
            onSecondary = Color(0xFF381E72),
            tertiary = CyberAccent,
            background = CyberBg,
            onBackground = TextPrimaryDark,
            surface = CyberSurface,
            onSurface = TextPrimaryDark,
            surfaceVariant = CyberSurfaceVariant,
            onSurfaceVariant = TextSecondaryDark,
            outline = CyberBorder
        )
        AppThemeMode.ALABASTER_LIGHT -> lightColorScheme(
            primary = AlabasterPrimary,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDBEAFE),
            onPrimaryContainer = Color(0xFF1E40AF),
            secondary = AlabasterSecondary,
            onSecondary = Color.White,
            tertiary = AlabasterAccent,
            background = AlabasterBg,
            onBackground = TextPrimaryLight,
            surface = AlabasterSurface,
            onSurface = TextPrimaryLight,
            surfaceVariant = AlabasterSurfaceVariant,
            onSurfaceVariant = TextSecondaryLight,
            outline = AlabasterBorder
        )
    }
}

@Composable
fun MyApplicationTheme(
    initialThemeMode: AppThemeMode = AppThemeMode.OBSIDIAN_SAPPHIRE,
    content: @Composable () -> Unit
) {
    var currentTheme by remember { mutableStateOf(initialThemeMode) }

    CompositionLocalProvider(
        LocalAppTheme provides currentTheme,
        LocalThemeSwitcher provides { newTheme -> currentTheme = newTheme }
    ) {
        MaterialTheme(
            colorScheme = getThemeColorScheme(currentTheme),
            typography = Typography,
            content = content
        )
    }
}
