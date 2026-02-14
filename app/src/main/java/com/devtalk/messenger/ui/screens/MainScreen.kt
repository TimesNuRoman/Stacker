package com.devtalk.messenger.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.*
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    currentUser: User,
    contacts: List<Contact>,
    chats: List<Chat>,
    messages: Map<String, List<Message>>,
    selectedChatId: String?,
    onSelectChat: (String) -> Unit,
    onCloseChat: (String) -> Unit,
    onSendMessage: (String, String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onStartCall: (String, CallType) -> Unit,
    onLogout: () -> Unit,
    onContactClick: (Contact) -> Unit,
    incomingCall: CallSignal? = null,
    onAcceptCall: () -> Unit = {},
    onRejectCall: () -> Unit = {}
) {
    var showSidebar by remember { mutableStateOf(true) }
    var sidebarTab by remember { mutableStateOf(0) } // 0=Project, 1=Contacts
    var messageInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // Build open tabs from chats that are "open"
    val openChats = chats.filter { chat ->
        selectedChatId == chat.id || messages.containsKey(chat.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        // === TOOLBAR (Menu bar) ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(IdeColors.bgToolbar)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo/Brand
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = IdeColors.accentBlue)) { append("Dev") }
                    withStyle(SpanStyle(color = IdeColors.textPrimary)) { append("Talk") }
                },
                style = IdeTypography.codeLarge
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Menu items
            listOf("File", "Edit", "View", "Tools").forEach { menu ->
                Text(
                    text = menu,
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary),
                    modifier = Modifier
                        .clickable {
                            when (menu) {
                                "File" -> showMenu = !showMenu
                                "View" -> showSidebar = !showSidebar
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            IdeIconButton(
                icon = Icons.Default.QrCodeScanner,
                contentDescription = "Scan QR",
                onClick = onOpenQrScanner,
                tint = IdeColors.textSecondary
            )
            IdeIconButton(
                icon = Icons.Default.Person,
                contentDescription = "Profile",
                onClick = onOpenProfile,
                tint = IdeColors.textSecondary
            )
            IdeIconButton(
                icon = Icons.Default.PowerSettingsNew,
                contentDescription = "Logout (Delete Account)",
                onClick = onLogout,
                tint = IdeColors.accentRed
            )
        }
        Divider(color = IdeColors.border, thickness = 1.dp)

        // Dropdown menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(IdeColors.bgPopup)
        ) {
            DropdownMenuItem(
                text = { Text("New Chat...", style = IdeTypography.codeSmall) },
                onClick = { showMenu = false; onOpenQrScanner() },
                leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = IdeColors.textSecondary) }
            )
            DropdownMenuItem(
                text = { Text("My Profile", style = IdeTypography.codeSmall) },
                onClick = { showMenu = false; onOpenProfile() },
                leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = IdeColors.textSecondary) }
            )
            Divider(color = IdeColors.border)
            DropdownMenuItem(
                text = { Text("Exit (Delete Account)", style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)) },
                onClick = { showMenu = false; onLogout() },
                leadingIcon = { Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(14.dp), tint = IdeColors.accentRed) }
            )
        }

        // === INCOMING CALL BANNER ===
        incomingCall?.let { call ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeColors.accentGreen.copy(alpha = 0.15f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Phone,
                    contentDescription = null,
                    tint = IdeColors.accentGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Incoming ${call.type.name.lowercase()} call from ${call.callerName}",
                    style = IdeTypography.code.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
                IdeButton(
                    text = "Accept",
                    onClick = onAcceptCall,
                    color = IdeColors.accentGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                IdeButton(
                    text = "Reject",
                    onClick = onRejectCall,
                    color = IdeColors.accentRed
                )
            }
            Divider(color = IdeColors.border, thickness = 1.dp)
        }

        // === MAIN CONTENT ===
        Row(modifier = Modifier.weight(1f)) {
            // === SIDEBAR ===
            AnimatedVisibility(
                visible = showSidebar,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                Row {
                    Column(
                        modifier = Modifier
                            .width(240.dp)
                            .fillMaxHeight()
                            .background(IdeColors.bgSidebar)
                    ) {
                        // Sidebar tab headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(IdeColors.bgToolbar)
                        ) {
                            listOf("Project", "Contacts").forEachIndexed { index, title ->
                                Text(
                                    text = title,
                                    style = IdeTypography.codeSmall.copy(
                                        color = if (sidebarTab == index) IdeColors.textPrimary else IdeColors.textSecondary
                                    ),
                                    modifier = Modifier
                                        .clickable { sidebarTab = index }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .then(
                                            if (sidebarTab == index)
                                                Modifier.border(
                                                    width = 0.dp,
                                                    color = Color.Transparent,
                                                    shape = RoundedCornerShape(0.dp)
                                                )
                                            else Modifier
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IdeIconButton(
                                icon = Icons.Default.Add,
                                contentDescription = "Add contact",
                                onClick = onOpenQrScanner,
                                tint = IdeColors.textSecondary
                            )
                        }
                        Divider(color = IdeColors.border, thickness = 1.dp)

                        if (sidebarTab == 0) {
                            // Project view - chats as files
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Project root
                                item {
                                    IdeTreeItem(
                                        text = "devtalk-chats",
                                        icon = Icons.Default.Folder,
                                        iconTint = IdeColors.accentOrange,
                                        isExpandable = true,
                                        isExpanded = true,
                                        depth = 0,
                                        onClick = {}
                                    )
                                }
                                items(chats) { chat ->
                                    val otherName = chat.getOtherName(currentUser.uid)
                                    val unread = chat.unreadCount[currentUser.uid] ?: 0
                                    IdeTreeItem(
                                        text = "$otherName.chat",
                                        icon = Icons.Default.Description,
                                        iconTint = IdeColors.accentBlue,
                                        isSelected = selectedChatId == chat.id,
                                        depth = 1,
                                        badge = if (unread > 0) "$unread" else null,
                                        onClick = { onSelectChat(chat.id) }
                                    )
                                }

                                // Separator
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IdeTreeItem(
                                        text = "contacts/",
                                        icon = Icons.Default.FolderOpen,
                                        iconTint = IdeColors.accentGreen,
                                        isExpandable = true,
                                        isExpanded = true,
                                        depth = 0,
                                        onClick = { sidebarTab = 1 }
                                    )
                                }
                                items(contacts) { contact ->
                                    IdeTreeItem(
                                        text = contact.username,
                                        icon = Icons.Default.Person,
                                        iconTint = IdeColors.textSecondary,
                                        depth = 1,
                                        statusColor = IdeColors.online,
                                        onClick = { onContactClick(contact) }
                                    )
                                }
                            }
                        } else {
                            // Contacts view
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IdeButton(
                                            text = "Scan QR",
                                            onClick = onOpenQrScanner,
                                            icon = Icons.Default.QrCodeScanner,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IdeButton(
                                            text = "My QR",
                                            onClick = onOpenProfile,
                                            icon = Icons.Default.QrCode,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                if (contacts.isEmpty()) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "// No contacts yet",
                                                style = IdeTypography.comment
                                            )
                                            Text(
                                                text = "// Scan a QR code to add",
                                                style = IdeTypography.comment
                                            )
                                        }
                                    }
                                }
                                items(contacts) { contact ->
                                    IdeTreeItem(
                                        text = contact.username,
                                        icon = Icons.Default.Person,
                                        iconTint = IdeColors.accentBlue,
                                        statusColor = IdeColors.online,
                                        onClick = { onContactClick(contact) }
                                    )
                                }
                            }
                        }
                    }
                    // Sidebar border
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(IdeColors.border)
                    )
                }
            }

            // === EDITOR AREA ===
            Column(modifier = Modifier.weight(1f)) {
                if (openChats.isNotEmpty() || selectedChatId != null) {
                    // Tab bar
                    val tabs = openChats.map { chat ->
                        val otherName = chat.getOtherName(currentUser.uid)
                        val unread = chat.unreadCount[currentUser.uid] ?: 0
                        TabItem(
                            title = "$otherName.chat",
                            icon = Icons.Default.Description,
                            unreadCount = unread
                        )
                    }
                    val selectedTabIndex = openChats.indexOfFirst { it.id == selectedChatId }

                    IdeTabBar(
                        tabs = tabs,
                        selectedIndex = if (selectedTabIndex >= 0) selectedTabIndex else 0,
                        onTabSelected = { index ->
                            if (index in openChats.indices) {
                                onSelectChat(openChats[index].id)
                            }
                        },
                        onTabClosed = { index ->
                            if (index in openChats.indices) {
                                onCloseChat(openChats[index].id)
                            }
                        }
                    )

                    // Chat content
                    val selectedChat = chats.find { it.id == selectedChatId }
                    if (selectedChat != null) {
                        val chatMessages = messages[selectedChatId] ?: emptyList()
                        ChatEditorView(
                            chat = selectedChat,
                            messages = chatMessages,
                            currentUser = currentUser,
                            messageInput = messageInput,
                            onMessageInputChange = { messageInput = it },
                            onSendMessage = {
                                if (messageInput.isNotBlank()) {
                                    onSendMessage(selectedChat.id, messageInput)
                                    messageInput = ""
                                }
                            },
                            onStartAudioCall = { onStartCall(selectedChat.getOtherParticipant(currentUser.uid), CallType.AUDIO) },
                            onStartVideoCall = { onStartCall(selectedChat.getOtherParticipant(currentUser.uid), CallType.VIDEO) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Empty state - welcome editor
                    WelcomeEditor(
                        username = currentUser.username,
                        contactCount = contacts.size,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // === STATUS BAR ===
        Divider(color = IdeColors.border, thickness = 1.dp)
        IdeStatusBar(
            items = listOf(
                StatusBarItem(
                    text = "main",
                    icon = Icons.Default.AccountTree,
                    color = IdeColors.accentBlue
                ),
                StatusBarItem(
                    text = currentUser.status.toIdeString(),
                    icon = Icons.Default.Circle,
                    color = when (currentUser.status) {
                        UserStatus.ONLINE -> IdeColors.online
                        UserStatus.AWAY -> IdeColors.away
                        UserStatus.DO_NOT_DISTURB -> IdeColors.dnd
                        UserStatus.OFFLINE -> IdeColors.offline
                    }
                ),
                StatusBarItem(text = "", fillWeight = true),
                StatusBarItem(
                    text = "${contacts.size} contacts",
                    color = IdeColors.textSecondary
                ),
                StatusBarItem(
                    text = "${chats.size} chats",
                    color = IdeColors.textSecondary
                ),
                StatusBarItem(
                    text = currentUser.username,
                    icon = Icons.Default.Person,
                    color = IdeColors.accentGreen,
                    isClickable = true,
                    onClick = onOpenProfile
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
private fun ChatEditorView(
    chat: Chat,
    messages: List<Message>,
    currentUser: User,
    messageInput: String,
    onMessageInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onStartAudioCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier) {
        // Breadcrumb / path bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(IdeColors.bgSecondary)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("devtalk", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            Text("/", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
            Text("chats", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            Text("/", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
            Text(
                chat.getOtherName(currentUser.uid),
                style = IdeTypography.codeSmall.copy(color = IdeColors.textPrimary)
            )
            Spacer(modifier = Modifier.weight(1f))

            // Call buttons
            IdeIconButton(
                icon = Icons.Default.Phone,
                contentDescription = "Audio call",
                onClick = onStartAudioCall,
                tint = IdeColors.accentGreen
            )
            IdeIconButton(
                icon = Icons.Default.Videocam,
                contentDescription = "Video call",
                onClick = onStartVideoCall,
                tint = IdeColors.accentBlue
            )
        }
        Divider(color = IdeColors.border, thickness = 1.dp)

        // Messages area (editor-like with line numbers)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(IdeColors.bgEditor)
        ) {
            // File header comment
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    TerminalLine(
                        lineNumber = 1,
                        text = "/**",
                        isSenderLine = false
                    )
                    TerminalLine(
                        lineNumber = 2,
                        text = " * Chat: ${chat.getOtherName(currentUser.uid)}",
                        isSenderLine = false
                    )
                    TerminalLine(
                        lineNumber = 3,
                        text = " * Started: ${formatDate(chat.createdAt)}",
                        isSenderLine = false
                    )
                    TerminalLine(
                        lineNumber = 4,
                        text = " */",
                        isSenderLine = false
                    )
                    TerminalLine(
                        lineNumber = 5,
                        text = "",
                        isSenderLine = false
                    )
                }
            }

            items(messages) { message ->
                val lineNum = 6 + messages.indexOf(message) * 2
                val isOwn = message.senderId == currentUser.uid
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                    // Sender line (like function call)
                    TerminalLine(
                        lineNumber = lineNum,
                        text = "${message.senderName}.say($timeStr) {",
                        isOwnMessage = isOwn,
                        isSenderLine = true
                    )
                    // Message content
                    TerminalLine(
                        lineNumber = lineNum + 1,
                        text = "    \"${message.content}\"",
                        isOwnMessage = isOwn
                    )
                    // Closing brace
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .background(IdeColors.gutter)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "${lineNum + 1}",
                                style = IdeTypography.lineNumber
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .heightIn(min = 20.dp)
                                .background(IdeColors.border)
                        )
                        Text(
                            text = "}",
                            style = IdeTypography.code.copy(color = IdeColors.textKeyword),
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }
                }
            }
        }

        // Input area (terminal-style)
        Divider(color = IdeColors.border, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeColors.bgSecondary)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = IdeColors.accentGreen)) {
                        append(currentUser.username)
                    }
                    withStyle(SpanStyle(color = IdeColors.textPrimary)) {
                        append("@devtalk:~$ ")
                    }
                },
                style = IdeTypography.codeSmall
            )
            IdeTextField(
                value = messageInput,
                onValueChange = onMessageInputChange,
                placeholder = "type your message...",
                modifier = Modifier.weight(1f)
            )
            IdeButton(
                text = "Send",
                onClick = onSendMessage,
                icon = Icons.Default.Send,
                enabled = messageInput.isNotBlank()
            )
        }
    }
}

@Composable
private fun WelcomeEditor(
    username: String,
    contactCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(IdeColors.bgEditor)
            .verticalScroll(rememberScrollState())
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ASCII art logo
        val asciiLogo = """
            ╔══════════════════════════════════════╗
            ║                                      ║
            ║         ██████╗ ████████╗            ║
            ║         ██╔══██╗╚══██╔══╝            ║
            ║         ██║  ██║   ██║               ║
            ║         ██║  ██║   ██║               ║
            ║         ██████╔╝   ██║               ║
            ║         ╚═════╝    ╚═╝               ║
            ║                                      ║
            ║           D E V T A L K              ║
            ║                                      ║
            ╚══════════════════════════════════════╝
        """.trimIndent()

        Text(
            text = asciiLogo,
            style = IdeTypography.codeSmall.copy(color = IdeColors.accentBlue),
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Welcome code block
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(IdeColors.bgSecondary)
                .border(1.dp, IdeColors.border, RoundedCornerShape(3.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "fun main() {",
                style = IdeTypography.code.copy(color = IdeColors.textKeyword)
            )
            Text(
                text = "    val user = \"$username\"",
                style = IdeTypography.code.copy(color = IdeColors.textString)
            )
            Text(
                text = "    val contacts = $contactCount",
                style = IdeTypography.code.copy(color = IdeColors.textNumber)
            )
            Text(
                text = "    println(\"Welcome back, \$user!\")",
                style = IdeTypography.code
            )
            Text(
                text = "    // Select a chat or scan a QR to start",
                style = IdeTypography.comment
            )
            Text(
                text = "}",
                style = IdeTypography.code.copy(color = IdeColors.textKeyword)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Keyboard shortcuts hint
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(IdeColors.bgSecondary)
                .border(1.dp, IdeColors.border, RoundedCornerShape(3.dp))
                .padding(16.dp)
        ) {
            Text(text = "Quick Actions:", style = IdeTypography.code.copy(color = IdeColors.textFunction))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "📷  Scan QR    — Add new contact", style = IdeTypography.codeSmall)
            Text(text = "👤  Profile    — Share your QR/link", style = IdeTypography.codeSmall)
            Text(text = "📞  Audio call — Voice chat", style = IdeTypography.codeSmall)
            Text(text = "📹  Video call — Face-to-face", style = IdeTypography.codeSmall)
            Text(text = "⏻   Exit       — Delete account", style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed))
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
