package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Sophisticated Natural Dark Design Palette (Earthy Sage, Botanical Soft Greens, Warm Wheat)
val PrimaryIndigo = Color(0xFFA2D5AB)      // Soft Sage Green
val SecondaryViolet = Color(0xFF8BBA94)    // Accent Pine/Earthy Green
val AccentCoral = Color(0xFFE9B384)        // Warm Honey/Oat peach (discounts, highlights)

val BackgroundSlate = Color(0xFF131411)    // Deep Obsidian Ground
val SurfaceWhite = Color(0xFF1E211A)       // Warm Charcoal with Olive undertone
val BorderSlateSoft = Color(0xFF2D3227)    // Organic leafy soft border card

val TextSlateDark = Color(0xFFE4E3DB)      // Soft Cotton/Linen white
val TextSlateMedium = Color(0xFFB5B9B0)    // Gentle Warm Gray / Sage dust
val TextSlateLight = Color(0xFF7A7E75)     // Subdued forest mist placeholder

// Elegant multi-color botanical gradients
val EarthyBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F100E), // Ultra deep carbon spruce
        Color(0xFF191B15), // Deep dark mountain olive
        Color(0xFF11120F)  // Warm mineral charcoal
    )
)

val GoldenGlowGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF8BBA94),
        Color(0xFFA2D5AB),
        Color(0xFFE9B384)
    )
)

// Dark Mode Palette Fallbacks (Matched to handle both configurations cleanly)
val DarkPrimary = Color(0xFFA2D5AB)
val DarkSecondary = Color(0xFF8BBA94)
val DarkBackground = Color(0xFF131411)
val DarkSurface = Color(0xFF1E211A)
val DarkTextLight = Color(0xFFE4E3DB)
val DarkTextMedium = Color(0xFFB5B9B0)

