package com.devtalk.messenger.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography

@Composable
fun HackerAvatar(
    emoji: String,
    asciiArt: String = "",
    color: Color = IdeColors.accentGreen,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
    showBorder: Boolean = true,
    statusColor: Color? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(IdeColors.bgSecondary)
            .then(if (showBorder) Modifier.neonBorder(color.copy(alpha = 0.6f)) else Modifier)
            .drawBehind {
                drawRect(color.copy(alpha = 0.05f))
            },
        contentAlignment = Alignment.Center
    ) {
        if (asciiArt.isNotBlank()) {
            Text(
                text = asciiArt,
                style = IdeTypography.ascii.copy(
                    color = color,
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        (size.value / 12).coerceIn(4f, 10f),
                        androidx.compose.ui.unit.TextUnitType.Sp
                    ),
                    lineHeight = androidx.compose.ui.unit.TextUnit(
                        (size.value / 10).coerceIn(5f, 12f),
                        androidx.compose.ui.unit.TextUnitType.Sp
                    )
                )
            )
        } else {
            Text(
                text = emoji,
                style = IdeTypography.code.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        size.value * 0.45f,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    )
                )
            )
        }

        // Status dot in corner
        statusColor?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(size * 0.2f)
                    .clip(RoundedCornerShape(50))
                    .background(IdeColors.bgPrimary)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(it)
            )
        }
    }
}

object AvatarPresets {
    val emojis = listOf(
        "👤", "☠", "👾", "🤖", "👽", "🦾", "🧑‍💻", "👩‍💻",
        "🥷", "🕵️", "🐱", "🐺", "🦊", "🐍", "🦅", "🐙",
        "💀", "👻", "🎭", "🔮", "⚡", "🌀", "♠", "♟",
        "🛡", "⚔", "🗡", "🏴‍☠️", "🧬", "🔒", "📡", "💎"
    )

    val asciiArts = listOf(
        "" to "Emoji only",
        "╔═╗\n║☠║\n╚═╝" to "Skull box",
        " /\\\n/  \\\n\\  /\n \\/" to "Diamond",
        "┌─┐\n│▓│\n└─┘" to "Block",
        "[##]\n#  #\n[##]" to "Frame",
        "╱╲\n╲╱" to "X mark",
        "/\\_/\\\n>^.^<" to "Cat",
        " ║\n═╬═\n ║" to "Cross",
    )

    val colors = listOf(
        "#00FF41" to "Matrix Green",
        "#00FFCC" to "Cyan",
        "#FF00FF" to "Magenta",
        "#FF0040" to "Red",
        "#FFFF00" to "Yellow",
        "#00CCFF" to "Electric Blue",
        "#FF6600" to "Orange",
        "#9966FF" to "Purple",
    )
}
