package com.devtalk.messenger.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography

@Composable
fun LogoutConfirmDialog(
    username: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "warn")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warningPulse"
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(IdeColors.bgPrimary)
                .neonBorder(IdeColors.accentRed.copy(alpha = warningAlpha))
                .padding(0.dp)
        ) {
            // Title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeColors.accentRed.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☠",
                    style = IdeTypography.code.copy(color = IdeColors.accentRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlitchText(
                    text = "SELF-DESTRUCT SEQUENCE",
                    style = IdeTypography.code.copy(
                        color = IdeColors.accentRed,
                        letterSpacing = 2.sp
                    ),
                    glitchIntensity = 0.2f
                )
            }
            NeonDivider(color = IdeColors.accentRed)

            // Content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "╔════════════════════════════════╗",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)
                )
                Text(
                    text = "║  ⚠  IRREVERSIBLE OPERATION  ⚠  ║",
                    style = IdeTypography.codeSmall.copy(
                        color = IdeColors.accentRed.copy(alpha = warningAlpha)
                    )
                )
                Text(
                    text = "╠════════════════════════════════╣",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)
                )
                Text(
                    text = "║ WIPE TARGET:                   ║",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                )
                Text(
                    text = "║  → Agent: $username",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                )
                Text(
                    text = "║  → All messages",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentYellow)
                )
                Text(
                    text = "║  → All contacts",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentYellow)
                )
                Text(
                    text = "║  → All session data",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentYellow)
                )
                Text(
                    text = "║  → Encryption keys",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentYellow)
                )
                Text(
                    text = "╠════════════════════════════════╣",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)
                )
                Text(
                    text = "║  NO RECOVERY POSSIBLE.         ║",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)
                )
                Text(
                    text = "╚════════════════════════════════╝",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$ rm -rf /home/$username/* && shred -vfz",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    IdeButton(
                        text = "ABORT",
                        onClick = onDismiss,
                        color = IdeColors.textComment
                    )
                    IdeButton(
                        text = "☠ CONFIRM WIPE",
                        onClick = onConfirm,
                        icon = Icons.Default.DeleteForever,
                        color = IdeColors.accentRed
                    )
                }
            }
        }
    }
}

private val sp = androidx.compose.ui.unit.sp
