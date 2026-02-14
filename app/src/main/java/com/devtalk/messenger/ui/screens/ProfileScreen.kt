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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.User
import com.devtalk.messenger.data.model.UserStatus
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import com.devtalk.messenger.util.QrCodeUtils

@Composable
fun ProfileScreen(
    user: User,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onStatusChange: (UserStatus) -> Unit,
    onBioChange: (String) -> Unit
) {
    val context = LocalContext.current
    val profileLink = remember(user.username) {
        QrCodeUtils.generateProfileLink(user.username)
    }
    val qrBitmap: Bitmap = remember(user.username) {
        QrCodeUtils.generateQrBitmap(profileLink, 400)
    }
    var editingBio by remember { mutableStateOf(false) }
    var bioText by remember { mutableStateOf(user.bio) }
    var showCopied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
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
                onClick = onBack
            )
            Text(
                text = "DevTalk — Profile",
                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary),
                modifier = Modifier.weight(1f)
            )
        }
        Divider(color = IdeColors.border, thickness = 1.dp)

        // Tab bar
        IdeTabBar(
            tabs = listOf(
                TabItem("profile.kt", icon = Icons.Default.Person),
                TabItem("qr_code.png", icon = Icons.Default.QrCode)
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
            // Profile as code
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(IdeColors.bgSecondary)
                    .border(1.dp, IdeColors.border, RoundedCornerShape(3.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "data class Profile(",
                    style = IdeTypography.code.copy(color = IdeColors.textKeyword)
                )
                ProfileField("val", "username", "\"${user.username}\"", IdeColors.textString)
                ProfileField("val", "uid", "\"${user.uid.take(8)}...\"", IdeColors.textComment)
                ProfileField("val", "status", "Status.${user.status.name}", IdeColors.accentGreen)
                ProfileField("val", "contacts", "Int", IdeColors.textNumber)

                // Bio field
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("val ", style = IdeTypography.code.copy(color = IdeColors.textKeyword))
                    Text("bio", style = IdeTypography.code.copy(color = IdeColors.textConstant))
                    Text(" = ", style = IdeTypography.code)
                    if (editingBio) {
                        IdeTextField(
                            value = bioText,
                            onValueChange = { bioText = it },
                            modifier = Modifier.weight(1f),
                            prefix = "\""
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
                            text = "\"${user.bio}\"",
                            style = IdeTypography.code.copy(color = IdeColors.textString),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { editingBio = true }
                        )
                        IdeIconButton(
                            icon = Icons.Default.Edit,
                            contentDescription = "Edit bio",
                            onClick = { editingBio = true }
                        )
                    }
                }
                Text(
                    text = ")",
                    style = IdeTypography.code.copy(color = IdeColors.textKeyword)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QR Code
            Text(
                text = "// Scan this QR to add me",
                style = IdeTypography.comment
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(2.dp, IdeColors.border, RoundedCornerShape(4.dp))
                    .background(IdeColors.bgPrimary)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile link
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(IdeColors.bgSecondary)
                    .border(1.dp, IdeColors.border, RoundedCornerShape(3.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "// Share link:",
                    style = IdeTypography.comment
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profileLink,
                        style = IdeTypography.code.copy(color = IdeColors.textLink),
                        modifier = Modifier.weight(1f)
                    )
                    IdeIconButton(
                        icon = Icons.Default.ContentCopy,
                        contentDescription = "Copy link",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("DevTalk Profile", profileLink))
                            showCopied = true
                        }
                    )
                    IdeIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Add me on DevTalk: $profileLink")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share DevTalk profile"))
                        }
                    )
                }
                if (showCopied) {
                    Text(
                        text = "// Copied to clipboard ✓",
                        style = IdeTypography.comment.copy(color = IdeColors.accentGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(IdeColors.bgSecondary)
                    .border(1.dp, IdeColors.border, RoundedCornerShape(3.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "// Set your status:",
                    style = IdeTypography.comment
                )
                Spacer(modifier = Modifier.height(8.dp))
                UserStatus.values().forEach { status ->
                    val isSelected = user.status == status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) IdeColors.bgSelection else Color.Transparent)
                            .clickable { onStatusChange(status) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
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
                            style = IdeTypography.code.copy(
                                color = if (isSelected) IdeColors.textPrimary else IdeColors.textSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger zone
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .border(1.dp, IdeColors.accentRed.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "// ⚠️ DANGER ZONE",
                    style = IdeTypography.comment.copy(color = IdeColors.accentRed)
                )
                Spacer(modifier = Modifier.height(8.dp))
                IdeButton(
                    text = "process.exit() — Delete Account & Logout",
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
                    text = "profile",
                    icon = Icons.Default.Person,
                    color = IdeColors.accentBlue
                ),
                StatusBarItem(text = "", fillWeight = true),
                StatusBarItem(
                    text = user.username,
                    color = IdeColors.accentGreen
                ),
                StatusBarItem(
                    text = "UTF-8",
                    color = IdeColors.textSecondary
                )
            )
        )
    }
}

@Composable
private fun ProfileField(
    keyword: String,
    name: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
        Text("$keyword ", style = IdeTypography.code.copy(color = IdeColors.textKeyword))
        Text(name, style = IdeTypography.code.copy(color = IdeColors.textConstant))
        Text(" = ", style = IdeTypography.code)
        Text(value, style = IdeTypography.code.copy(color = valueColor))
        Text(",", style = IdeTypography.code)
    }
}
