package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ThemePreset(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val accentColor: Color,
    val surfaceColor: Color,
    val backgroundColor: Color,
    val onBackgroundColor: Color,
    val isDark: Boolean
) {
    CYBER_NEON(
        title = "Cyber Neon",
        subtitle = "Electric cyan & deep obsidian",
        primaryColor = Color(0xFF00E5FF),
        accentColor = Color(0xFF76FF03),
        surfaceColor = Color(0xFF131A26),
        backgroundColor = Color(0xFF090D14),
        onBackgroundColor = Color(0xFFE2E8F0),
        isDark = true
    ),
    SUNSET_FLARE(
        title = "Sunset Flare",
        subtitle = "Warm amber & rich charcoal",
        primaryColor = Color(0xFFFF9100),
        accentColor = Color(0xFFFF5252),
        surfaceColor = Color(0xFF1E1714),
        backgroundColor = Color(0xFF120E0C),
        onBackgroundColor = Color(0xFFFFF3E0),
        isDark = true
    ),
    EMERALD_MATRIX(
        title = "Emerald Matrix",
        subtitle = "Mint green & jungle midnight",
        primaryColor = Color(0xFF00E676),
        accentColor = Color(0xFF1DE9B6),
        surfaceColor = Color(0xFF11221A),
        backgroundColor = Color(0xFF09140E),
        onBackgroundColor = Color(0xFFE8F5E9),
        isDark = true
    ),
    AMETHYST_GLOW(
        title = "Amethyst Glow",
        subtitle = "Royal violet & celestial lilac",
        primaryColor = Color(0xFFD500F9),
        accentColor = Color(0xFF7C4DFF),
        surfaceColor = Color(0xFF1C132B),
        backgroundColor = Color(0xFF0F081D),
        onBackgroundColor = Color(0xFFF3E5F5),
        isDark = true
    ),
    CRIMSON_PULSE(
        title = "Crimson Pulse",
        subtitle = "Bold scarlet & sleek carbon",
        primaryColor = Color(0xFFFF3366),
        accentColor = Color(0xFFFF5252),
        surfaceColor = Color(0xFF221518),
        backgroundColor = Color(0xFF120A0C),
        onBackgroundColor = Color(0xFFFFEBEE),
        isDark = true
    ),
    CLEAN_SLATE(
        title = "Clean Minimal",
        subtitle = "Crisp sapphire & modern slate",
        primaryColor = Color(0xFF2563EB),
        accentColor = Color(0xFF0284C7),
        surfaceColor = Color(0xFFFFFFFF),
        backgroundColor = Color(0xFFF8FAFC),
        onBackgroundColor = Color(0xFF0F172A),
        isDark = false
    )
}

enum class KeypadShape {
    CIRCLE,
    ROUNDED_SQUIRCLE,
    NEUMORPHIC_PILL
}
