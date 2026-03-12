package com.devtalk.messenger.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// === HACKER / NEON / MATRIX Color Palette ===
object IdeColors {
    // Backgrounds — pure black & near-black
    val bgPrimary = Color(0xFF000000)
    val bgSecondary = Color(0xFF0A0A0A)
    val bgEditor = Color(0xFF000000)
    val bgToolbar = Color(0xFF050505)
    val bgTab = Color(0xFF0A0A0A)
    val bgTabActive = Color(0xFF001A00)
    val bgSidebar = Color(0xFF050505)
    val bgStatusBar = Color(0xFF001100)
    val bgInput = Color(0xFF0D0D0D)
    val bgHover = Color(0xFF001A00)
    val bgSelection = Color(0xFF003300)
    val bgPopup = Color(0xFF0A0F0A)

    // Borders — dim neon green
    val border = Color(0xFF0D3B0D)
    val borderActive = Color(0xFF00FF41)

    // Text — Matrix neon green as primary
    val textPrimary = Color(0xFF00FF41)
    val textSecondary = Color(0xFF00AA2A)
    val textKeyword = Color(0xFF00FF41)
    val textString = Color(0xFF00FFCC)      // cyan
    val textNumber = Color(0xFFFF00FF)       // magenta/pink
    val textComment = Color(0xFF005500)
    val textFunction = Color(0xFF00FF41)
    val textType = Color(0xFF00FFCC)
    val textAnnotation = Color(0xFFFFFF00)   // yellow warning
    val textConstant = Color(0xFFFF00FF)     // magenta
    val textError = Color(0xFFFF0040)        // neon red
    val textLink = Color(0xFF00CCFF)         // electric blue

    // Accents — neon palette
    val accentBlue = Color(0xFF00CCFF)
    val accentGreen = Color(0xFF00FF41)
    val accentOrange = Color(0xFFFF6600)
    val accentRed = Color(0xFFFF0040)
    val accentYellow = Color(0xFFFFFF00)
    val accentPurple = Color(0xFFFF00FF)
    val accentCyan = Color(0xFF00FFCC)

    // Gutter / line numbers
    val gutter = Color(0xFF0A0A0A)
    val gutterText = Color(0xFF004400)

    // Status indicators
    val online = Color(0xFF00FF41)
    val away = Color(0xFFFFFF00)
    val dnd = Color(0xFFFF0040)
    val offline = Color(0xFF333333)

    // Neon glow accent (for special effects)
    val neonGlow = Color(0xFF00FF41)
    val neonGlowCyan = Color(0xFF00FFCC)
    val neonGlowPink = Color(0xFFFF00FF)

    // Scanline / CRT overlay
    val scanline = Color(0x0800FF41)
    val crtVignette = Color(0xFF000000)
}

val JetBrainsMono = FontFamily.Monospace

object IdeTypography {
    val code = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textPrimary,
        letterSpacing = 1.sp
    )
    val codeSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 11.sp,
        color = IdeColors.textSecondary,
        letterSpacing = 1.sp
    )
    val codeLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 15.sp,
        color = IdeColors.textPrimary,
        letterSpacing = 1.sp
    )
    val heading = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = IdeColors.textPrimary,
        letterSpacing = 2.sp
    )
    val tab = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 12.sp,
        color = IdeColors.textSecondary,
        letterSpacing = 1.sp
    )
    val statusBar = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 11.sp,
        color = IdeColors.textSecondary,
        letterSpacing = 1.sp
    )
    val terminal = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textPrimary,
        letterSpacing = 1.sp,
        lineHeight = 20.sp
    )
    val keyword = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textKeyword,
        letterSpacing = 1.sp
    )
    val string = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textString,
        letterSpacing = 1.sp
    )
    val comment = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textComment,
        letterSpacing = 1.sp
    )
    val lineNumber = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 12.sp,
        color = IdeColors.gutterText,
        letterSpacing = 1.sp
    )
    val glitch = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = IdeColors.neonGlow,
        letterSpacing = 4.sp
    )
    val ascii = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 10.sp,
        color = IdeColors.accentGreen,
        letterSpacing = 0.sp,
        lineHeight = 12.sp
    )
}

private val HackerDarkColorScheme = darkColorScheme(
    primary = IdeColors.accentGreen,
    onPrimary = Color.Black,
    primaryContainer = IdeColors.bgSelection,
    secondary = IdeColors.accentCyan,
    onSecondary = Color.Black,
    background = IdeColors.bgPrimary,
    onBackground = IdeColors.textPrimary,
    surface = IdeColors.bgSecondary,
    onSurface = IdeColors.textPrimary,
    surfaceVariant = IdeColors.bgToolbar,
    onSurfaceVariant = IdeColors.textSecondary,
    error = IdeColors.accentRed,
    onError = Color.Black,
    outline = IdeColors.border,
    outlineVariant = IdeColors.border
)

@Composable
fun DevTalkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HackerDarkColorScheme,
        typography = Typography(
            bodyLarge = IdeTypography.code,
            bodyMedium = IdeTypography.code,
            bodySmall = IdeTypography.codeSmall,
            titleLarge = IdeTypography.heading,
            titleMedium = IdeTypography.codeLarge,
            labelSmall = IdeTypography.statusBar,
            labelMedium = IdeTypography.tab
        ),
        content = content
    )
}
