package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.model.KeypadShape
import com.example.data.model.ThemePreset

data class PulseDialCustomTheme(
    val preset: ThemePreset = ThemePreset.CYBER_NEON,
    val keypadShape: KeypadShape = KeypadShape.ROUNDED_SQUIRCLE,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)

val LocalPulseDialTheme = staticCompositionLocalOf { PulseDialCustomTheme() }

@Composable
fun PulseDialTheme(
    themeConfig: PulseDialCustomTheme = PulseDialCustomTheme(),
    content: @Composable () -> Unit
) {
    val preset = themeConfig.preset

    val colorScheme: ColorScheme = if (preset.isDark) {
        darkColorScheme(
            primary = preset.primaryColor,
            onPrimary = if (preset.isDark) Color.Black else Color.White,
            secondary = preset.accentColor,
            onSecondary = Color.Black,
            background = preset.backgroundColor,
            onBackground = preset.onBackgroundColor,
            surface = preset.surfaceColor,
            onSurface = preset.onBackgroundColor,
            surfaceVariant = preset.surfaceColor.copy(alpha = 0.85f),
            onSurfaceVariant = preset.onBackgroundColor.copy(alpha = 0.7f),
            outline = preset.primaryColor.copy(alpha = 0.3f)
        )
    } else {
        lightColorScheme(
            primary = preset.primaryColor,
            onPrimary = Color.White,
            secondary = preset.accentColor,
            onSecondary = Color.White,
            background = preset.backgroundColor,
            onBackground = preset.onBackgroundColor,
            surface = preset.surfaceColor,
            onSurface = preset.onBackgroundColor,
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1)
        )
    }

    CompositionLocalProvider(LocalPulseDialTheme provides themeConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
