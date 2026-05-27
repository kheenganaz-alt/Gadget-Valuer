package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Define Colors
val SlateDark = Color(0xFF0F172A)     // Slate 900
val DeepBlue = Color(0xFF1E293B)      // Slate 800
val AccentEmerald = Color(0xFF10B981) // Emerald 500
val LightEmerald = Color(0xFF34D399)  // Emerald 400
val AccentAmber = Color(0xFFF59E0B)    // Amber 500
val Charcoal = Color(0xFF1E293B)      // Card Background Dark
val CoolWhite = Color(0xFFF8FAFC)     // Slate 50
val MutedSlate = Color(0xFF64748B)    // Slate 500
val BorderSlate = Color(0xFF334155)   // Slate 700

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = AccentEmerald,
    secondary = AccentAmber,
    tertiary = LightEmerald,
    background = SlateDark,
    surface = DeepBlue,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = CoolWhite,
    onSurface = CoolWhite,
    outline = BorderSlate
)

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),       // Emerald 600
    secondary = Color(0xFFD97706),     // Amber 600
    tertiary = Color(0xFF10B981),
    background = CoolWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SlateDark,
    onSurface = SlateDark,
    outline = Color(0xFFE2E8F0)
)

// Typography
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace, // Monospace for stats and values!
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun GadgetValuerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
