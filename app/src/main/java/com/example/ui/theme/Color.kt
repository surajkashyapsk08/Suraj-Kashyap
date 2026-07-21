package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Vibrant Palette - Purple Core Branding
val VibrantPrimary = Color(0xFF6750A4)
val VibrantOnPrimary = Color(0xFFFFFFFF)
val VibrantPrimaryContainer = Color(0xFFEADDFF)
val VibrantOnPrimaryContainer = Color(0xFF21005D)

val VibrantSecondary = Color(0xFF625B71)
val VibrantOnSecondary = Color(0xFFFFFFFF)
val VibrantSecondaryContainer = Color(0xFFE8DEF8)
val VibrantOnSecondaryContainer = Color(0xFF1D192B)

val VibrantBackground = Color(0xFFFDFCFF)
val VibrantOnBackground = Color(0xFF1C1B1F)
val VibrantSurface = Color(0xFFFFFFFF)
val VibrantOnSurface = Color(0xFF1C1B1F)
val VibrantSurfaceVariant = Color(0xFFE7E0EC)
val VibrantOnSurfaceVariant = Color(0xFF49454F)

// Subject-Specific Card Styling from the Vibrant Design Specification
val MathBg = Color(0xFFD0E4FF)
val MathText = Color(0xFF001D35)
val MathAccent = Color(0xFF004A77)

val ScienceBg = Color(0xFFB8F397)
val ScienceText = Color(0xFF042100)
val ScienceAccent = Color(0xFF0C3B00)

val EnglishBg = Color(0xFFFFD9E2)
val EnglishText = Color(0xFF3E001D)
val EnglishAccent = Color(0xFF6B0037)

val HindiBg = Color(0xFFFFDAD4)
val HindiText = Color(0xFF410001)
val HindiAccent = Color(0xFF930006)

val SocialBg = Color(0xFFCEE5FF)
val SocialText = Color(0xFF001D35)
val SocialAccent = Color(0xFF004070)

// Standard Dark Mode Fallbacks
val DarkBackground = Color(0xFF141218)
val DarkSurface = Color(0xFF1D1B20)
val DarkOnPrimary = Color(0xFFEADDFF)
val DarkOnSecondary = Color(0xFF21005D)

// Reusable Subject Styling Helper mapping subjectIds to custom backgrounds and accents
data class SubjectThemeColors(
    val bg: Color,
    val text: Color,
    val accent: Color
)

fun getSubjectThemeColors(subjectId: String): SubjectThemeColors {
    return when (subjectId) {
        "math" -> SubjectThemeColors(MathBg, MathText, MathAccent)
        "science" -> SubjectThemeColors(ScienceBg, ScienceText, ScienceAccent)
        "english" -> SubjectThemeColors(EnglishBg, EnglishText, EnglishAccent)
        "hindi" -> SubjectThemeColors(HindiBg, HindiText, HindiAccent)
        "social" -> SubjectThemeColors(SocialBg, SocialText, SocialAccent)
        else -> SubjectThemeColors(
            bg = Color(0xFFEADDFF),
            text = Color(0xFF21005D),
            accent = Color(0xFF6750A4)
        )
    }
}
