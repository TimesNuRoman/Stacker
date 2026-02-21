package com.devtalk.messenger.ui.screens

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
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(IdeColors.bgSecondary)
                .border(1.dp, IdeColors.accentRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(0.dp)
        ) {
            // Dialog title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeColors.bgToolbar)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = IdeColors.accentRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚠ Confirm process.exit()",
                    style = IdeTypography.code.copy(color = IdeColors.accentRed)
                )
            }
            Divider(color = IdeColors.border, thickness = 1.dp)

            // Content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "/**",
                    style = IdeTypography.comment
                )
                Text(
                    text = " * WARNING: This action is IRREVERSIBLE!",
                    style = IdeTypography.comment.copy(color = IdeColors.accentRed)
                )
                Text(
                    text = " *",
                    style = IdeTypography.comment
                )
                Text(
                    text = " * Exiting will permanently delete:",
                    style = IdeTypography.comment
                )
                Text(
                    text = " * - Your account ($username)",
                    style = IdeTypography.comment
                )
                Text(
                    text = " * - All your messages",
                    style = IdeTypography.comment
                )
                Text(
                    text = " * - All your contacts",
                    style = IdeTypography.comment
                )
                Text(
                    text = " * - Your profile data",
                    style = IdeTypography.comment
                )
                Text(
                    text = " *",
                    style = IdeTypography.comment
                )
                Text(
                    text = " * There is NO way to recover this data.",
                    style = IdeTypography.comment.copy(color = IdeColors.accentYellow)
                )
                Text(
                    text = " */",
                    style = IdeTypography.comment
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "rm -rf /home/$username/*",
                    style = IdeTypography.code.copy(color = IdeColors.accentRed)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    IdeButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        color = IdeColors.bgInput
                    )
                    IdeButton(
                        text = "Confirm Delete",
                        onClick = onConfirm,
                        icon = Icons.Default.DeleteForever,
                        color = IdeColors.accentRed
                    )
                }
            }
        }
    }
}
