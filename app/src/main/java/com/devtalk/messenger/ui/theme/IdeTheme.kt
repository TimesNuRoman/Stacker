package com.devtalk.messenger.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// === IDE Color Palette (IntelliJ Darcula inspired) ===
object IdeColors {
    // Backgrounds
    val bgPrimary = Color(0xFF1E1F22)
    val bgSecondary = Color(0xFF2B2D30)
    val bgEditor = Color(0xFF1E1F22)
    val bgToolbar = Color(0xFF3C3F41)
    val bgTab = Color(0xFF2B2D30)
    val bgTabActive = Color(0xFF1E1F22)
    val bgSidebar = Color(0xFF2B2D30)
    val bgStatusBar = Color(0xFF3C3F41)
    val bgInput = Color(0xFF45494A)
    val bgHover = Color(0xFF2E436E)
    val bgSelection = Color(0xFF214283)
    val bgPopup = Color(0xFF3C3F41)

    // Borders
    val border = Color(0xFF323232)
    val borderActive = Color(0xFF4E94CE)

    // Text - Code Editor colors
    val textPrimary = Color(0xFFA9B7C6)
    val textSecondary = Color(0xFF808080)
    val textKeyword = Color(0xFFCC7832)
    val textString = Color(0xFF6A8759)
    val textNumber = Color(0xFF6897BB)
    val textComment = Color(0xFF808080)
    val textFunction = Color(0xFFFFC66D)
    val textType = Color(0xFFA9B7C6)
    val textAnnotation = Color(0xFFBBB529)
    val textConstant = Color(0xFF9876AA)
    val textError = Color(0xFFFF6B68)
    val textLink = Color(0xFF287BDE)

    // Accent
    val accentBlue = Color(0xFF3574F0)
    val accentGreen = Color(0xFF499C54)
    val accentOrange = Color(0xFFCC7832)
    val accentRed = Color(0xFFFF6B68)
    val accentYellow = Color(0xFFFFC66D)
    val accentPurple = Color(0xFF9876AA)

    // Gutter (line numbers area)
    val gutter = Color(0xFF313335)
    val gutterText = Color(0xFF606366)

    // Status indicators
    val online = Color(0xFF499C54)
    val away = Color(0xFFFFC66D)
    val dnd = Color(0xFFFF6B68)
    val offline = Color(0xFF606366)
}

// Monospace font family
val JetBrainsMono = FontFamily.Monospace

// IDE Typography
object IdeTypography {
    val code = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textPrimary,
        letterSpacing = 0.sp
    )
    val codeSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 11.sp,
        color = IdeColors.textSecondary,
        letterSpacing = 0.sp
    )
    val codeLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 15.sp,
        color = IdeColors.textPrimary,
        letterSpacing = 0.sp
    )
    val heading = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = IdeColors.textPrimary,
        letterSpacing = 0.sp
    )
    val tab = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 12.sp,
        color = IdeColors.textSecondary,
        letterSpacing = 0.sp
    )
    val statusBar = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 11.sp,
        color = IdeColors.textSecondary,
        letterSpacing = 0.sp
    )
    val terminal = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textPrimary,
        letterSpacing = 0.sp,
        lineHeight = 20.sp
    )
    val keyword = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textKeyword,
        letterSpacing = 0.sp
    )
    val string = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textString,
        letterSpacing = 0.sp
    )
    val comment = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 13.sp,
        color = IdeColors.textComment,
        letterSpacing = 0.sp
    )
    val lineNumber = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 12.sp,
        color = IdeColors.gutterText,
        letterSpacing = 0.sp
    )
}

// Material3 dark color scheme with IDE colors
private val IdeDarkColorScheme = darkColorScheme(
    primary = IdeColors.accentBlue,
    onPrimary = Color.White,
    primaryContainer = IdeColors.bgSelection,
    secondary = IdeColors.accentGreen,
    onSecondary = Color.White,
    background = IdeColors.bgPrimary,
    onBackground = IdeColors.textPrimary,
    surface = IdeColors.bgSecondary,
    onSurface = IdeColors.textPrimary,
    surfaceVariant = IdeColors.bgToolbar,
    onSurfaceVariant = IdeColors.textSecondary,
    error = IdeColors.accentRed,
    onError = Color.White,
    outline = IdeColors.border,
    outlineVariant = IdeColors.border
)

@Composable
fun DevTalkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IdeDarkColorScheme,
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
