package com.gettaximeter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Vibrant Palette Branding Colors
val TaxiRosePrimary = Color(0xFFE11D48)       // Vibrant Rose 600
val TaxiRoseDark = Color(0xFF9F1239)          // Rose 800
val TaxiRoseAccent = Color(0xFFFFF1F2)        // Rose 50
val TaxiSlate900 = Color(0xFF0F172A)          // Slate 900

val TaxiLightBackground = Color(0xFFF8FAFC)  // Slate 50 Background
val TaxiLightSurface = Color(0xFFFFFFFF)     // High-contrast clean white
val TaxiDarkBackground = Color(0xFF0F172A)   // Slate 900 Background for Dark
val TaxiDarkSurface = Color(0xFF1E293B)      // Slate 800 Surface for Dark

private val LightColorScheme = lightColorScheme(
    primary = TaxiRosePrimary,
    onPrimary = Color.White,
    primaryContainer = TaxiRoseAccent,
    secondary = TaxiSlate900,
    onSecondary = Color.White,
    background = TaxiLightBackground,
    surface = TaxiLightSurface,
    onBackground = TaxiSlate900,
    onSurface = Color(0xFF1E293B)
)

private val DarkColorScheme = darkColorScheme(
    primary = TaxiRosePrimary,
    onPrimary = Color.White,
    primaryContainer = TaxiRoseDark,
    secondary = Color(0xFF94A3B8), // Slate 400
    onSecondary = Color.Black,
    background = TaxiDarkBackground,
    surface = TaxiDarkSurface,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFE2E8F0)
)

@Composable
fun GetTaxiMeterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
