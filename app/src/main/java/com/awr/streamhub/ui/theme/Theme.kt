package com.awr.streamhub.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// ─── AWR Color Palette ────────────────────────────────────────────────────────
val Bg          = Color(0xFF05050A)
val Panel       = Color(0xFF10111A)
val Panel2      = Color(0xFF171826)
val Panel3      = Color(0xFF1E1F2E)

val Gold        = Color(0xFFFFD36A)
val GoldDim     = Color(0xFFCCAA45)
val Orange      = Color(0xFFFF8C42)
val Red         = Color(0xFFFF3F6E)
val Cyan        = Color(0xFF4DE8FF)
val Violet      = Color(0xFF9E7BFF)
val VioletDim   = Color(0xFF7B5CE8)
val Green       = Color(0xFF4DFFA3)

val TextPrimary = Color(0xFFFFFFFF)
val TextSoft    = Color(0xFFD7D7E7)
val TextMuted   = Color(0xFF8B8EA7)
val TextHint    = Color(0xFF555770)

// Type accent per category
val AnimeAccent = Violet
val MovieAccent = Red
val DramaAccent = Cyan

val AWRColorScheme = darkColorScheme(
    primary       = Gold,
    onPrimary     = Color.Black,
    secondary     = Orange,
    onSecondary   = Color.Black,
    tertiary      = Cyan,
    onTertiary    = Color.Black,
    background    = Bg,
    onBackground  = TextPrimary,
    surface       = Panel,
    onSurface     = TextPrimary,
    surfaceVariant = Panel2,
    onSurfaceVariant = TextSoft,
    error         = Red,
    onError       = Color.White
)

// Category accent helper
fun accentForType(typeLabel: String): Color = when (typeLabel.uppercase()) {
    "ANIME" -> AnimeAccent
    "MOVIE" -> MovieAccent
    "KDRAMA", "K-DRAMA" -> DramaAccent
    else -> Gold
}
