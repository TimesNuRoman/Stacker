package com.devtalk.messenger.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.User
import com.devtalk.messenger.data.model.UserStatus
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import com.devtalk.messenger.util.InviteManager
import com.devtalk.messenger.util.QrCodeUtils

@Composable
fun ProfileScreen(
    user: User,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onStatusChange: (UserStatus) -> Unit,
    onBioChange: (String) -> Unit,
    onOpenInvite: () -> Unit = {}
) {
    val context = LocalContext.current
    val profileLink = remember(user.username) {
        QrCodeUtils.generateProfileLink(user.username)
    }
    val qrBitmap: Bitmap = remember(user.username) {
        QrCodeUtils.generateQrBitmap(
            profileLink, 400,
            fgColor = android.graphics.Color.parseColor("#00FF41"),
            bgColor = android.graphics.Color.parseColor("#000000")
        )
    }
    var editingBio by remember { mutableStateOf(false) }
    var bioText by remember { mutableStateOf(user.bio) }
    var showCopied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        // Subtle matrix rain
        MatrixRain(alpha = 0.03f, density = 10)

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
                    text = "[ IDENTITY :: ${user.username} ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
            }
            NeonDivider()

            // Tabs
            IdeTabBar(
                tabs = listOf(
                    TabItem("identity.dat", icon = Icons.Default.Person),
                    TabItem("qr_key.png", icon = Icons.Default.QrCode)
                ),
                selectedIndex = 0,
                onTabSelected = {}
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Identity card
                HackerPanel(borderColor = IdeColors.accentGreen) {
                    Text(
                        text = "╔══════════════════════════════════╗",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                    )
                    Text(
                        text = "║     ☠ AGENT IDENTITY CARD ☠     ║",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                    )
                    Text(
                        text = "╠══════════════════════════════════╣",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                    )
                    Text(
                        text = "║ HANDLE  : ${user.username}",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                    )
                    Text(
                        text = "║ UID     : ${user.uid.take(12)}...",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                    )
                    Text(
                        text = "║ STATUS  : ${user.status.toIdeString()}",
                        style = IdeTypography.codeSmall.copy(
                            color = when (user.status) {
                                UserStatus.ONLINE -> IdeColors.accentGreen
                                UserStatus.AWAY -> IdeColors.accentYellow
                                UserStatus.DO_NOT_DISTURB -> IdeColors.accentRed
                                UserStatus.OFFLINE -> IdeColors.textComment
                            }
                        )
                    )
                    Text(
                        text = "║ CRYPTO  : AES-256 / RSA-4096",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                    )

                    // Bio
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "║ BIO     : ",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                        )
                        if (editingBio) {
                            IdeTextField(
                                value = bioText,
                                onValueChange = { bioText = it },
                                modifier = Modifier.weight(1f)
                            )
                            IdeIconButton(
                                icon = Icons.Default.Check,
                                contentDescription = "Save",
                                onClick = {
                                    onBioChange(bioText)
                                    editingBio = false
                                },
                                tint = IdeColors.accentGreen
                            )
                        } else {
                            Text(
                                text = user.bio,
                                style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { editingBio = true }
                            )
                            IdeIconButton(
                                icon = Icons.Default.Edit,
                                contentDescription = "Edit",
                                onClick = { editingBio = true },
                                tint = IdeColors.textComment
                            )
                        }
                    }
                    Text(
                        text = "╚══════════════════════════════════╝",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // QR Code
                Text(
                    text = "[ SCAN TO ADD AGENT ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan, letterSpacing = 2.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IdeColors.bgPrimary)
                        .neonBorder(IdeColors.accentGreen)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "█▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀█",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Share link
                HackerPanel(borderColor = IdeColors.accentCyan) {
                    Text(
                        text = "[SHARE LINK]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profileLink,
                            style = IdeTypography.code.copy(color = IdeColors.accentGreen),
                            modifier = Modifier.weight(1f)
                        )
                        IdeIconButton(
                            icon = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("DevTalk", profileLink))
                                showCopied = true
                            },
                            tint = IdeColors.accentCyan
                        )
                        IdeIconButton(
                            icon = Icons.Default.Share,
                            contentDescription = "Share",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Join DevTalk: $profileLink")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share"))
                            },
                            tint = IdeColors.accentGreen
                        )
                    }
                    if (showCopied) {
                        Text(
                            text = "[COPIED TO CLIPBOARD ✓]",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Invite friends banner
                HackerPanel(borderColor = IdeColors.accentPurple) {
                    Text(
                        text = "[INVITE FRIENDS]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share via WhatsApp, Telegram, SMS, email",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                    )
                    Text(
                        text = "or share a stylized profile card image.",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IdeButton(
                            text = "ALL METHODS",
                            onClick = onOpenInvite,
                            icon = Icons.Default.Share,
                            color = IdeColors.accentPurple,
                            modifier = Modifier.weight(1f)
                        )
                        IdeButton(
                            text = "CARD",
                            onClick = {
                                InviteManager.shareProfileCardImage(context, user.username, qrBitmap)
                            },
                            icon = Icons.Default.Image,
                            color = IdeColors.accentCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status selector
                HackerPanel(borderColor = IdeColors.accentPurple) {
                    Text(
                        text = "[SET STATUS]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UserStatus.values().forEach { status ->
                        val isSelected = user.status == status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(1.dp))
                                .then(
                                    if (isSelected) Modifier
                                        .background(IdeColors.bgSelection)
                                        .neonBorder(IdeColors.accentGreen.copy(alpha = 0.4f))
                                    else Modifier
                                )
                                .clickable { onStatusChange(status) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (status) {
                                            UserStatus.ONLINE -> IdeColors.online
                                            UserStatus.AWAY -> IdeColors.away
                                            UserStatus.DO_NOT_DISTURB -> IdeColors.dnd
                                            UserStatus.OFFLINE -> IdeColors.offline
                                        }
                                    )
                            )
                            Text(
                                text = status.toIdeString(),
                                style = IdeTypography.codeSmall.copy(
                                    color = if (isSelected) IdeColors.accentGreen else IdeColors.textSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Danger zone — self-destruct
                HackerPanel(borderColor = IdeColors.accentRed) {
                    Text(
                        text = "⚠ ⚠ ⚠  DANGER ZONE  ⚠ ⚠ ⚠",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed, letterSpacing = 2.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "[!] Self-destruct sequence",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)
                    )
                    Text(
                        text = "[!] Erases all data permanently",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentYellow)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IdeButton(
                        text = "☠ SELF-DESTRUCT ☠",
                        onClick = onLogout,
                        icon = Icons.Default.DeleteForever,
                        color = IdeColors.accentRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Status bar
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(
                        text = "IDENTITY",
                        icon = Icons.Default.Person,
                        color = IdeColors.accentGreen
                    ),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(
                        text = user.username,
                        color = IdeColors.accentCyan
                    ),
                    StatusBarItem(
                        text = "ENCRYPTED",
                        color = IdeColors.accentGreen
                    )
                )
            )
        }

        CrtOverlay()
    }
}

private val sp = androidx.compose.ui.unit.sp
