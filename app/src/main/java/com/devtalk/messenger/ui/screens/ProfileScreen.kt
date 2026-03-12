package com.devtalk.messenger.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.devtalk.messenger.data.model.*
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import com.devtalk.messenger.util.InviteManager
import com.devtalk.messenger.util.QrCodeUtils

private val sp = androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    user: User,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onStatusChange: (UserStatus) -> Unit,
    onBioChange: (String) -> Unit,
    onAvatarChange: (String, String, String) -> Unit = { _, _, _ -> },
    onOpenInvite: () -> Unit = {},
    // Wall
    wallPosts: List<WallPost> = emptyList(),
    wallComments: Map<String, List<WallComment>> = emptyMap(),
    onWallPost: (String, WallPostType) -> Unit = { _, _ -> },
    onWallLike: (WallPost) -> Unit = {},
    onWallComment: (WallPost, String) -> Unit = { _, _ -> },
    onWallDelete: (WallPost) -> Unit = {},
    onLoadComments: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val profileLink = remember(user.username) { QrCodeUtils.generateProfileLink(user.username) }
    val qrBitmap: Bitmap = remember(user.username) {
        QrCodeUtils.generateQrBitmap(
            profileLink, 400,
            fgColor = android.graphics.Color.parseColor("#00FF41"),
            bgColor = android.graphics.Color.parseColor("#000000")
        )
    }

    var activeSection by remember { mutableIntStateOf(0) } // 0=wall, 1=info, 2=qr
    var showAvatarEditor by remember { mutableStateOf(false) }
    var showBioEditor by remember { mutableStateOf(false) }
    var showCopied by remember { mutableStateOf(false) }

    // Avatar editor dialog
    if (showAvatarEditor) {
        AvatarEditorDialog(
            currentEmoji = user.avatarEmoji,
            currentAscii = user.avatarAscii,
            currentColor = user.avatarColor,
            onSave = { emoji, ascii, color ->
                onAvatarChange(emoji, ascii, color)
                showAvatarEditor = false
            },
            onDismiss = { showAvatarEditor = false }
        )
    }

    // Bio editor dialog
    if (showBioEditor) {
        BioEditorDialog(
            currentBio = user.bio,
            onSave = { onBioChange(it); showBioEditor = false },
            onDismiss = { showBioEditor = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
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
                IdeIconButton(icon = Icons.Default.ArrowBack, contentDescription = "Back", onClick = onBack, tint = IdeColors.accentGreen)
                Text("[ ${user.username} ]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen), modifier = Modifier.weight(1f))
                IdeIconButton(icon = Icons.Default.Share, contentDescription = "Invite", onClick = onOpenInvite, tint = IdeColors.accentPurple)
            }
            NeonDivider()

            // Profile header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeColors.bgSecondary)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(modifier = Modifier.clickable { showAvatarEditor = true }) {
                    HackerAvatar(
                        emoji = user.avatarEmoji,
                        asciiArt = user.avatarAscii,
                        color = try { Color(android.graphics.Color.parseColor(user.avatarColor)) } catch (_: Exception) { IdeColors.accentGreen },
                        size = 80.dp,
                        statusColor = when (user.status) {
                            UserStatus.ONLINE -> IdeColors.online
                            UserStatus.AWAY -> IdeColors.away
                            UserStatus.DO_NOT_DISTURB -> IdeColors.dnd
                            UserStatus.OFFLINE -> IdeColors.offline
                        }
                    )
                    // Edit icon overlay
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change avatar",
                        tint = IdeColors.accentGreen.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(IdeColors.bgPrimary)
                            .padding(2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "@ ${user.username}",
                    style = IdeTypography.codeLarge.copy(color = IdeColors.accentGreen)
                )

                // Bio (clickable to edit)
                Text(
                    text = user.bio,
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary),
                    modifier = Modifier
                        .clickable { showBioEditor = true }
                        .padding(vertical = 4.dp)
                )

                // Status row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (user.status) {
                                    UserStatus.ONLINE -> IdeColors.online
                                    UserStatus.AWAY -> IdeColors.away
                                    UserStatus.DO_NOT_DISTURB -> IdeColors.dnd
                                    UserStatus.OFFLINE -> IdeColors.offline
                                }
                            )
                    )
                    Text(user.status.toIdeString(), style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))

                    // Quick status change buttons
                    UserStatus.values().forEach { s ->
                        if (s != user.status) {
                            Text(
                                text = "[${s.name.take(3)}]",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp),
                                modifier = Modifier.clickable { onStatusChange(s) }
                            )
                        }
                    }
                }
            }
            NeonDivider()

            // Section tabs: Wall / Info / QR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(IdeColors.bgSecondary)
            ) {
                listOf("WALL", "INFO", "QR").forEachIndexed { idx, title ->
                    val isActive = activeSection == idx
                    Column(
                        modifier = Modifier
                            .clickable { activeSection = idx }
                            .padding(horizontal = 16.dp)
                            .height(32.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "[$title]",
                            style = IdeTypography.codeSmall.copy(
                                color = if (isActive) IdeColors.accentGreen else IdeColors.textComment
                            )
                        )
                    }
                }
            }
            NeonDivider()

            // Section content
            when (activeSection) {
                0 -> WallSection(
                    user = user,
                    wallPosts = wallPosts,
                    wallComments = wallComments,
                    onWallPost = onWallPost,
                    onWallLike = onWallLike,
                    onWallComment = onWallComment,
                    onWallDelete = onWallDelete,
                    onLoadComments = onLoadComments,
                    isOwnWall = true,
                    modifier = Modifier.weight(1f)
                )
                1 -> InfoSection(
                    user = user,
                    onLogout = onLogout,
                    onOpenInvite = onOpenInvite,
                    onStatusChange = onStatusChange,
                    profileLink = profileLink,
                    context = context,
                    qrBitmap = qrBitmap,
                    modifier = Modifier.weight(1f)
                )
                2 -> QrSection(
                    user = user,
                    profileLink = profileLink,
                    qrBitmap = qrBitmap,
                    context = context,
                    onOpenInvite = onOpenInvite,
                    modifier = Modifier.weight(1f)
                )
            }

            // Status bar
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = "PROFILE", icon = Icons.Default.Person, color = IdeColors.accentGreen),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(text = "${wallPosts.size} POSTS", color = IdeColors.accentCyan),
                    StatusBarItem(text = user.username, color = IdeColors.accentGreen)
                )
            )
        }

        CrtOverlay()
    }
}

// === WALL TAB ===
@Composable
fun WallSection(
    user: User,
    wallPosts: List<WallPost>,
    wallComments: Map<String, List<WallComment>>,
    onWallPost: (String, WallPostType) -> Unit,
    onWallLike: (WallPost) -> Unit,
    onWallComment: (WallPost, String) -> Unit,
    onWallDelete: (WallPost) -> Unit,
    onLoadComments: (String) -> Unit,
    isOwnWall: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Post composer (own wall or guest posting)
        item {
            WallPostComposer(
                authorEmoji = user.avatarEmoji,
                authorName = user.username,
                onPost = onWallPost
            )
        }

        if (wallPosts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("┌─────────────────────────────┐", style = IdeTypography.codeSmall.copy(color = IdeColors.border))
                    Text("│     WALL IS EMPTY            │", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                    Text("│     Write the first post!    │", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                    Text("└─────────────────────────────┘", style = IdeTypography.codeSmall.copy(color = IdeColors.border))
                }
            }
        }

        items(wallPosts, key = { it.id }) { post ->
            WallPostCard(
                post = post,
                currentUid = user.uid,
                isOwnerWall = isOwnWall,
                comments = wallComments[post.id] ?: emptyList(),
                onLike = { onWallLike(post) },
                onComment = { text -> onWallComment(post, text) },
                onDelete = { onWallDelete(post) }
            )

            // Load comments for visible posts
            LaunchedEffect(post.id) { onLoadComments(post.id) }
        }
    }
}

// === INFO TAB ===
@Composable
private fun InfoSection(
    user: User,
    onLogout: () -> Unit,
    onOpenInvite: () -> Unit,
    onStatusChange: (UserStatus) -> Unit,
    profileLink: String,
    context: Context,
    qrBitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Identity card
        HackerPanel(borderColor = IdeColors.accentGreen) {
            Text("╔══ AGENT IDENTITY ═══════════════╗", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen))
            Text("║ HANDLE  : ${user.username}", style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan))
            Text("║ UID     : ${user.uid.take(12)}...", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
            Text("║ STATUS  : ${user.status.toIdeString()}", style = IdeTypography.codeSmall.copy(
                color = when (user.status) {
                    UserStatus.ONLINE -> IdeColors.accentGreen
                    UserStatus.AWAY -> IdeColors.accentYellow
                    UserStatus.DO_NOT_DISTURB -> IdeColors.accentRed
                    UserStatus.OFFLINE -> IdeColors.textComment
                }
            ))
            Text("║ BIO     : ${user.bio}", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            Text("║ CRYPTO  : AES-256 / RSA-4096", style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple))
            Text("╚═════════════════════════════════╝", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen))
        }

        // Status selector
        HackerPanel(borderColor = IdeColors.accentPurple) {
            Text("[SET STATUS]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple))
            Spacer(modifier = Modifier.height(4.dp))
            UserStatus.values().forEach { status ->
                val isSelected = user.status == status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(1.dp))
                        .then(if (isSelected) Modifier.background(IdeColors.bgSelection).neonBorder(IdeColors.accentGreen.copy(alpha = 0.4f)) else Modifier)
                        .clickable { onStatusChange(status) }
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(
                        when (status) { UserStatus.ONLINE -> IdeColors.online; UserStatus.AWAY -> IdeColors.away; UserStatus.DO_NOT_DISTURB -> IdeColors.dnd; UserStatus.OFFLINE -> IdeColors.offline }
                    ))
                    Text(status.toIdeString(), style = IdeTypography.codeSmall.copy(color = if (isSelected) IdeColors.accentGreen else IdeColors.textSecondary))
                }
            }
        }

        // Invite banner
        HackerPanel(borderColor = IdeColors.accentPurple) {
            Text("[INVITE FRIENDS]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple))
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IdeButton(text = "ALL METHODS", onClick = onOpenInvite, icon = Icons.Default.Share, color = IdeColors.accentPurple, modifier = Modifier.weight(1f))
                IdeButton(text = "CARD", onClick = { InviteManager.shareProfileCardImage(context, user.username, qrBitmap) }, icon = Icons.Default.Image, color = IdeColors.accentCyan, modifier = Modifier.weight(1f))
            }
        }

        // Danger zone
        HackerPanel(borderColor = IdeColors.accentRed) {
            Text("⚠ DANGER ZONE ⚠", style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed, letterSpacing = 2.sp))
            Spacer(modifier = Modifier.height(8.dp))
            IdeButton(text = "☠ SELF-DESTRUCT ☠", onClick = onLogout, icon = Icons.Default.DeleteForever, color = IdeColors.accentRed, modifier = Modifier.fillMaxWidth())
        }
    }
}

// === QR TAB ===
@Composable
private fun QrSection(
    user: User,
    profileLink: String,
    qrBitmap: Bitmap,
    context: Context,
    onOpenInvite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("[SCAN TO ADD AGENT]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan, letterSpacing = 2.sp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(IdeColors.bgPrimary)
                .neonBorder(IdeColors.accentGreen)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = "QR", modifier = Modifier.fillMaxSize())
        }

        Text("@ ${user.username}", style = IdeTypography.code.copy(color = IdeColors.accentGreen))
        Text(profileLink, style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan))

        HackerPanel(borderColor = IdeColors.accentGreen) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IdeButton(text = "COPY", onClick = { InviteManager.copyLinkToClipboard(context, user.username) }, icon = Icons.Default.ContentCopy, color = IdeColors.accentGreen, modifier = Modifier.weight(1f))
                IdeButton(text = "SHARE", onClick = { InviteManager.shareText(context, user.username) }, icon = Icons.Default.Share, color = IdeColors.accentCyan, modifier = Modifier.weight(1f))
                IdeButton(text = "MORE", onClick = onOpenInvite, icon = Icons.Default.MoreHoriz, color = IdeColors.accentPurple, modifier = Modifier.weight(1f))
            }
        }
    }
}

// === AVATAR EDITOR DIALOG ===
@Composable
private fun AvatarEditorDialog(
    currentEmoji: String,
    currentAscii: String,
    currentColor: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var emoji by remember { mutableStateOf(currentEmoji) }
    var ascii by remember { mutableStateOf(currentAscii) }
    var colorHex by remember { mutableStateOf(currentColor) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(IdeColors.bgPrimary)
                .neonBorder(IdeColors.accentGreen)
                .heightIn(max = 520.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(IdeColors.bgToolbar).padding(12.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("[EDIT AVATAR]", style = IdeTypography.code.copy(color = IdeColors.accentGreen))
            }
            NeonDivider()

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preview
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HackerAvatar(
                        emoji = emoji,
                        asciiArt = ascii,
                        color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { IdeColors.accentGreen },
                        size = 80.dp
                    )
                }

                // Emoji picker
                Text("Emoji:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    AvatarPresets.emojis.forEach { e ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (emoji == e && ascii.isBlank()) IdeColors.bgSelection else IdeColors.bgInput)
                                .clickable { emoji = e; ascii = "" }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(e, style = IdeTypography.code.copy(fontSize = 18.sp)) }
                    }
                }

                // ASCII art presets
                Text("ASCII art:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AvatarPresets.asciiArts.forEach { (art, label) ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (ascii == art) IdeColors.bgSelection else IdeColors.bgInput)
                                .neonBorder(if (ascii == art) IdeColors.accentGreen else IdeColors.border)
                                .clickable { ascii = art }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (art.isNotBlank()) {
                                Text(art, style = IdeTypography.ascii.copy(color = IdeColors.accentGreen, fontSize = 7.sp, lineHeight = 9.sp))
                            } else {
                                Text(emoji, style = IdeTypography.code.copy(fontSize = 16.sp))
                            }
                            Text(label, style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 8.sp))
                        }
                    }
                }

                // Custom ASCII input
                Text("Custom ASCII:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                IdeTextField(value = ascii, onValueChange = { ascii = it }, placeholder = "Type ASCII art...", singleLine = false)

                // Color picker
                Text("Color:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AvatarPresets.colors.forEach { (hex, name) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .then(if (colorHex == hex) Modifier.border(2.dp, Color.White, RoundedCornerShape(2.dp)) else Modifier)
                                .clickable { colorHex = hex }
                        )
                    }
                }

                // Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    IdeButton(text = "CANCEL", onClick = onDismiss, color = IdeColors.textComment)
                    IdeButton(text = "SAVE", onClick = { onSave(emoji, ascii, colorHex) }, icon = Icons.Default.Check, color = IdeColors.accentGreen)
                }
            }
        }
    }
}

// === BIO EDITOR DIALOG ===
@Composable
private fun BioEditorDialog(
    currentBio: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var bio by remember { mutableStateOf(currentBio) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(IdeColors.bgPrimary)
                .neonBorder(IdeColors.accentCyan)
        ) {
            Row(modifier = Modifier.fillMaxWidth().background(IdeColors.bgToolbar).padding(12.dp, 8.dp)) {
                Text("[EDIT BIO]", style = IdeTypography.code.copy(color = IdeColors.accentCyan))
            }
            NeonDivider(color = IdeColors.accentCyan)

            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Write anything about yourself:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))

                IdeTextField(value = bio, onValueChange = { bio = it }, placeholder = "// your bio...", singleLine = false)

                // Quick bio templates
                Text("Quick fill:", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                val templates = listOf(
                    "// just a hacker passing through",
                    "while(alive) { code(); sleep(); }",
                    "sudo rm -rf /bugs/*",
                    "404: Bio not found",
                    "echo \"Hello, World!\"",
                    "// trust no one, verify everything"
                )
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    templates.forEach { t ->
                        Text(
                            text = t.take(20) + if (t.length > 20) "..." else "",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan, fontSize = 9.sp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(IdeColors.bgInput)
                                .clickable { bio = t }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Preview
                HackerPanel(borderColor = IdeColors.border) {
                    Text("Preview:", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                    Text(bio.ifBlank { "// no bio yet" }, style = IdeTypography.code.copy(color = IdeColors.textPrimary))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    IdeButton(text = "CANCEL", onClick = onDismiss, color = IdeColors.textComment)
                    IdeButton(text = "SAVE", onClick = { onSave(bio) }, icon = Icons.Default.Check, color = IdeColors.accentGreen)
                }
            }
        }
    }
}
