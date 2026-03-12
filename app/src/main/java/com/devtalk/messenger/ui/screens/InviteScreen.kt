package com.devtalk.messenger.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import com.devtalk.messenger.util.InviteManager
import com.devtalk.messenger.util.QrCodeUtils

@Composable
fun InviteScreen(
    username: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profileLink = remember(username) { InviteManager.getProfileWebLink(username) }
    val qrBitmap: Bitmap = remember(username) {
        QrCodeUtils.generateQrBitmap(
            QrCodeUtils.generateProfileLink(username), 400,
            fgColor = android.graphics.Color.parseColor("#00FF41"),
            bgColor = android.graphics.Color.parseColor("#000000")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        MatrixRain(alpha = 0.02f, density = 8)

        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(IdeColors.bgToolbar)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IdeIconButton(
                    icon = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    tint = IdeColors.accentGreen
                )
                Text(
                    text = "[ INVITE :: SHARE PROFILE ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
            }
            NeonDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // QR code
                Text(
                    text = "[ YOUR QR CODE ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan, letterSpacing = 2.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IdeColors.bgPrimary)
                        .neonBorder(IdeColors.accentGreen)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "@ $username",
                    style = IdeTypography.code.copy(color = IdeColors.accentGreen)
                )
                Text(
                    text = profileLink,
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ===== QUICK ACTIONS =====
                HackerPanel(borderColor = IdeColors.accentGreen) {
                    Text(
                        text = "[QUICK SHARE]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IdeButton(
                            text = "COPY LINK",
                            onClick = { InviteManager.copyLinkToClipboard(context, username) },
                            icon = Icons.Default.ContentCopy,
                            color = IdeColors.accentGreen,
                            modifier = Modifier.weight(1f)
                        )
                        IdeButton(
                            text = "COPY @",
                            onClick = { InviteManager.copyHandleToClipboard(context, username) },
                            icon = Icons.Default.AlternateEmail,
                            color = IdeColors.accentCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ===== SHARE VIA APPS =====
                HackerPanel(borderColor = IdeColors.accentCyan) {
                    Text(
                        text = "[SHARE VIA]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ShareMethodRow(
                        icon = Icons.Default.Share,
                        label = "SYSTEM SHARE",
                        description = "Share via any installed app",
                        color = IdeColors.accentGreen,
                        onClick = { InviteManager.shareText(context, username) }
                    )
                    ShareMethodRow(
                        icon = Icons.Default.Chat,
                        label = "WHATSAPP",
                        description = "Share to WhatsApp contacts",
                        color = Color(0xFF25D366),
                        onClick = { InviteManager.shareViaWhatsApp(context, username) }
                    )
                    ShareMethodRow(
                        icon = Icons.Default.Send,
                        label = "TELEGRAM",
                        description = "Share to Telegram chats",
                        color = Color(0xFF0088CC),
                        onClick = { InviteManager.shareViaTelegram(context, username) }
                    )
                    ShareMethodRow(
                        icon = Icons.Default.Sms,
                        label = "SMS",
                        description = "Send invite via text message",
                        color = IdeColors.accentYellow,
                        onClick = { InviteManager.shareViaSms(context, username) }
                    )
                    ShareMethodRow(
                        icon = Icons.Default.Email,
                        label = "EMAIL",
                        description = "Send invite via email",
                        color = IdeColors.accentPurple,
                        onClick = { InviteManager.shareViaEmail(context, username) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ===== SHARE PROFILE CARD =====
                HackerPanel(borderColor = IdeColors.accentPurple) {
                    Text(
                        text = "[PROFILE CARD]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share a stylized card image with your QR code.",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                    )
                    Text(
                        text = "Perfect for social media, stories, and posts.",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IdeButton(
                        text = "SHARE PROFILE CARD",
                        onClick = { InviteManager.shareProfileCardImage(context, username, qrBitmap) },
                        icon = Icons.Default.Image,
                        color = IdeColors.accentPurple,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ===== INVITE TEXT PREVIEW =====
                HackerPanel(borderColor = IdeColors.border) {
                    Text(
                        text = "[INVITE MESSAGE PREVIEW]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = InviteManager.getInviteText(username),
                        style = IdeTypography.codeSmall.copy(
                            color = IdeColors.textSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Status bar
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = "INVITE", icon = Icons.Default.Share, color = IdeColors.accentPurple),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(text = username, color = IdeColors.accentGreen)
                )
            )
        }

        CrtOverlay()
    }
}

@Composable
private fun ShareMethodRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = IdeTypography.codeSmall.copy(color = color))
            Text(text = description, style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 10.sp))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = IdeColors.textComment,
            modifier = Modifier.size(16.dp)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(IdeColors.border)
    )
}

private val sp = androidx.compose.ui.unit.sp
