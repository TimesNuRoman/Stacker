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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.*
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
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
    onOpenSearch: () -> Unit,
    onOpenInvite: () -> Unit,
    onOpenBots: () -> Unit,
    onStartCall: (String, CallType) -> Unit,
    onLogout: () -> Unit,
    onContactClick: (Contact) -> Unit,
    incomingCall: CallSignal? = null,
    onAcceptCall: () -> Unit = {},
    onRejectCall: () -> Unit = {}
) {
    var showSidebar by remember { mutableStateOf(true) }
    var sidebarTab by remember { mutableIntStateOf(0) }
    var messageInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val openChats = chats.filter { chat ->
        selectedChatId == chat.id || messages.containsKey(chat.id)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // === TOOLBAR ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(IdeColors.bgToolbar)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Neon skull icon
                Text(
                    text = "☠",
                    style = IdeTypography.code.copy(
                        color = IdeColors.accentGreen,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                GlitchText(
                    text = "DEVTALK",
                    style = IdeTypography.codeLarge.copy(
                        color = IdeColors.accentGreen,
                        letterSpacing = 4.sp
                    ),
                    glitchIntensity = 0.1f
                )
                Spacer(modifier = Modifier.width(16.dp))

                // Menu items
                listOf("FILE", "NET", "VIEW", "TOOLS").forEach { menu ->
                    Text(
                        text = "[$menu]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment),
                        modifier = Modifier
                            .clickable {
                                when (menu) {
                                    "FILE" -> showMenu = !showMenu
                                    "VIEW" -> showSidebar = !showSidebar
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IdeIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Find agents",
                    onClick = onOpenSearch,
                    tint = IdeColors.accentGreen
                )
                IdeIconButton(
                    icon = Icons.Default.SmartToy,
                    contentDescription = "Bots",
                    onClick = onOpenBots,
                    tint = IdeColors.accentYellow
                )
                IdeIconButton(
                    icon = Icons.Default.PersonAdd,
                    contentDescription = "Invite",
                    onClick = onOpenInvite,
                    tint = IdeColors.accentPurple
                )
                IdeIconButton(
                    icon = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan",
                    onClick = onOpenQrScanner,
                    tint = IdeColors.accentCyan
                )
                IdeIconButton(
                    icon = Icons.Default.Person,
                    contentDescription = "Profile",
                    onClick = onOpenProfile,
                    tint = IdeColors.accentGreen
                )
                IdeIconButton(
                    icon = Icons.Default.PowerSettingsNew,
                    contentDescription = "SELF-DESTRUCT",
                    onClick = onLogout,
                    tint = IdeColors.accentRed
                )
            }
            NeonDivider()

            // Dropdown menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .background(IdeColors.bgPopup)
                    .neonBorder(IdeColors.accentGreen.copy(alpha = 0.3f))
            ) {
                DropdownMenuItem(
                    text = { Text("[🔍] FIND AGENTS", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)) },
                    onClick = { showMenu = false; onOpenSearch() }
                )
                DropdownMenuItem(
                    text = { Text("[📡] SCAN QR", style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)) },
                    onClick = { showMenu = false; onOpenQrScanner() }
                )
                DropdownMenuItem(
                    text = { Text("[🤖] BOT CATALOG", style = IdeTypography.codeSmall.copy(color = IdeColors.accentYellow)) },
                    onClick = { showMenu = false; onOpenBots() }
                )
                DropdownMenuItem(
                    text = { Text("[📢] INVITE FRIENDS", style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)) },
                    onClick = { showMenu = false; onOpenInvite() }
                )
                DropdownMenuItem(
                    text = { Text("[👤] IDENTITY", style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)) },
                    onClick = { showMenu = false; onOpenProfile() }
                )
                Divider(color = IdeColors.border)
                DropdownMenuItem(
                    text = { Text("[☠] SELF-DESTRUCT", style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)) },
                    onClick = { showMenu = false; onLogout() }
                )
            }

            // === INCOMING CALL BANNER ===
            incomingCall?.let { call ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IdeColors.accentGreen.copy(alpha = 0.08f))
                        .neonBorder(IdeColors.accentGreen)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡",
                        style = IdeTypography.code
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[INCOMING ${call.type.name}] from ${call.callerName}",
                        style = IdeTypography.code.copy(color = IdeColors.accentGreen),
                        modifier = Modifier.weight(1f)
                    )
                    IdeButton(
                        text = "ACCEPT",
                        onClick = onAcceptCall,
                        color = IdeColors.accentGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IdeButton(
                        text = "REJECT",
                        onClick = onRejectCall,
                        color = IdeColors.accentRed
                    )
                }
                NeonDivider()
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
                                .width(220.dp)
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
                                listOf("NODES", "AGENTS").forEachIndexed { index, title ->
                                    Text(
                                        text = "[$title]",
                                        style = IdeTypography.codeSmall.copy(
                                            color = if (sidebarTab == index) IdeColors.accentGreen else IdeColors.textComment
                                        ),
                                        modifier = Modifier
                                            .clickable { sidebarTab = index }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                IdeIconButton(
                                    icon = Icons.Default.Add,
                                    contentDescription = "Add",
                                    onClick = onOpenQrScanner,
                                    tint = IdeColors.accentGreen
                                )
                            }
                            NeonDivider()

                            if (sidebarTab == 0) {
                                // Channels (chats) view
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        IdeTreeItem(
                                            text = "secure-channels/",
                                            icon = Icons.Default.Folder,
                                            iconTint = IdeColors.accentGreen,
                                            isExpandable = true,
                                            isExpanded = true,
                                            depth = 0
                                        )
                                    }
                                    items(chats) { chat ->
                                        val otherName = chat.getOtherName(currentUser.uid)
                                        val unread = chat.unreadCount[currentUser.uid] ?: 0
                                        IdeTreeItem(
                                            text = "#$otherName",
                                            icon = Icons.Default.Lock,
                                            iconTint = IdeColors.accentCyan,
                                            isSelected = selectedChatId == chat.id,
                                            depth = 1,
                                            badge = if (unread > 0) "$unread" else null,
                                            onClick = { onSelectChat(chat.id) }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        IdeTreeItem(
                                            text = "agents/",
                                            icon = Icons.Default.FolderOpen,
                                            iconTint = IdeColors.accentPurple,
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
                                            iconTint = IdeColors.accentGreen.copy(alpha = 0.6f),
                                            depth = 1,
                                            statusColor = IdeColors.online,
                                            onClick = { onContactClick(contact) }
                                        )
                                    }
                                }
                            } else {
                                // Contacts (agents) view
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IdeButton(
                                                text = "FIND AGENTS",
                                                onClick = onOpenSearch,
                                                icon = Icons.Default.Search,
                                                modifier = Modifier.fillMaxWidth(),
                                                color = IdeColors.accentGreen
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                IdeButton(
                                                    text = "SCAN",
                                                    onClick = onOpenQrScanner,
                                                    icon = Icons.Default.QrCodeScanner,
                                                    modifier = Modifier.weight(1f),
                                                    color = IdeColors.accentCyan
                                                )
                                                IdeButton(
                                                    text = "INVITE",
                                                    onClick = onOpenInvite,
                                                    icon = Icons.Default.Share,
                                                    modifier = Modifier.weight(1f),
                                                    color = IdeColors.accentPurple
                                                )
                                            }
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
                                                Text("[NO AGENTS FOUND]", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                                                Text("[SCAN QR TO ADD]", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                                            }
                                        }
                                    }
                                    items(contacts) { contact ->
                                        IdeTreeItem(
                                            text = contact.username,
                                            icon = Icons.Default.Person,
                                            iconTint = IdeColors.accentGreen,
                                            statusColor = IdeColors.online,
                                            onClick = { onContactClick(contact) }
                                        )
                                    }
                                }
                            }
                        }
                        // Sidebar neon border
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            IdeColors.accentGreen.copy(alpha = 0.6f),
                                            IdeColors.accentGreen.copy(alpha = 0.2f),
                                            IdeColors.accentGreen.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                    }
                }

                // === EDITOR AREA ===
                Column(modifier = Modifier.weight(1f)) {
                    if (openChats.isNotEmpty() || selectedChatId != null) {
                        val tabs = openChats.map { chat ->
                            val otherName = chat.getOtherName(currentUser.uid)
                            val unread = chat.unreadCount[currentUser.uid] ?: 0
                            TabItem(
                                title = "#$otherName",
                                icon = Icons.Default.Lock,
                                unreadCount = unread
                            )
                        }
                        val selectedTabIndex = openChats.indexOfFirst { it.id == selectedChatId }

                        IdeTabBar(
                            tabs = tabs,
                            selectedIndex = if (selectedTabIndex >= 0) selectedTabIndex else 0,
                            onTabSelected = { index ->
                                if (index in openChats.indices) onSelectChat(openChats[index].id)
                            },
                            onTabClosed = { index ->
                                if (index in openChats.indices) onCloseChat(openChats[index].id)
                            }
                        )

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
                                onStartAudioCall = {
                                    onStartCall(selectedChat.getOtherParticipant(currentUser.uid), CallType.AUDIO)
                                },
                                onStartVideoCall = {
                                    onStartCall(selectedChat.getOtherParticipant(currentUser.uid), CallType.VIDEO)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        WelcomeEditor(
                            username = currentUser.username,
                            contactCount = contacts.size,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // === STATUS BAR ===
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(
                        text = "SECURE",
                        icon = Icons.Default.Lock,
                        color = IdeColors.accentGreen
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
                        text = "${contacts.size} AGENTS",
                        color = IdeColors.textComment
                    ),
                    StatusBarItem(
                        text = "${chats.size} CHANNELS",
                        color = IdeColors.textComment
                    ),
                    StatusBarItem(
                        text = currentUser.username,
                        icon = Icons.Default.Person,
                        color = IdeColors.accentGreen,
                        isClickable = true,
                        onClick = onOpenProfile
                    ),
                    StatusBarItem(
                        text = "E2E",
                        color = IdeColors.accentCyan
                    )
                )
            )
        }
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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier) {
        // Breadcrumb / channel path
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(IdeColors.bgSecondary)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🔒", style = IdeTypography.codeSmall)
            Text("devtalk", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
            Text(">", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen))
            Text("channels", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
            Text(">", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen))
            Text(
                "#${chat.getOtherName(currentUser.uid)}",
                style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
            )
            Spacer(modifier = Modifier.weight(1f))

            // Call buttons
            IdeIconButton(
                icon = Icons.Default.Phone,
                contentDescription = "Voice",
                onClick = onStartAudioCall,
                tint = IdeColors.accentGreen
            )
            IdeIconButton(
                icon = Icons.Default.Videocam,
                contentDescription = "Video",
                onClick = onStartVideoCall,
                tint = IdeColors.accentCyan
            )
        }
        NeonDivider()

        // Messages area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(IdeColors.bgEditor)
        ) {
            // Channel header
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 0.dp)) {
                    TerminalLine(
                        lineNumber = 1,
                        text = "╔══════════════════════════════════════╗"
                    )
                    TerminalLine(
                        lineNumber = 2,
                        text = "║  ENCRYPTED CHANNEL: #${chat.getOtherName(currentUser.uid)}"
                    )
                    TerminalLine(
                        lineNumber = 3,
                        text = "║  CREATED: ${formatDate(chat.createdAt)}"
                    )
                    TerminalLine(
                        lineNumber = 4,
                        text = "║  ENCRYPTION: AES-256-GCM + RSA-4096"
                    )
                    TerminalLine(
                        lineNumber = 5,
                        text = "╚══════════════════════════════════════╝"
                    )
                    TerminalLine(lineNumber = 6, text = "")
                }
            }

            items(messages) { message ->
                val lineNum = 7 + messages.indexOf(message) * 2
                val isOwn = message.senderId == currentUser.uid
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))

                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                    // Sender line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isOwn) Modifier.background(IdeColors.accentGreen.copy(alpha = 0.03f))
                                else Modifier
                            )
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Gutter
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .background(IdeColors.gutter)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text("$lineNum", style = IdeTypography.lineNumber)
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .heightIn(min = 18.dp)
                                .background(IdeColors.border)
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = IdeColors.textComment)) { append(" [$timeStr] ") }
                                withStyle(SpanStyle(color = if (isOwn) IdeColors.accentGreen else IdeColors.accentCyan)) {
                                    append("<${message.senderName}>")
                                }
                            },
                            style = IdeTypography.codeSmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    // Message content
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isOwn) Modifier.background(IdeColors.accentGreen.copy(alpha = 0.03f))
                                else Modifier
                            )
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .background(IdeColors.gutter)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text("${lineNum + 1}", style = IdeTypography.lineNumber)
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .heightIn(min = 18.dp)
                                .background(IdeColors.border)
                        )
                        Text(
                            text = "   ${message.content}",
                            style = IdeTypography.code.copy(
                                color = if (isOwn) IdeColors.accentGreen else IdeColors.textPrimary
                            ),
                            modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                        )
                    }
                }
            }
        }

        // Input area (terminal prompt)
        NeonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeColors.bgSecondary)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = IdeColors.accentRed)) { append(currentUser.username) }
                    withStyle(SpanStyle(color = IdeColors.textPrimary)) { append("@") }
                    withStyle(SpanStyle(color = IdeColors.accentGreen)) { append("dtalk") }
                    withStyle(SpanStyle(color = IdeColors.textPrimary)) { append(":~# ") }
                },
                style = IdeTypography.codeSmall
            )
            IdeTextField(
                value = messageInput,
                onValueChange = onMessageInputChange,
                placeholder = "transmit message...",
                modifier = Modifier.weight(1f)
            )
            IdeButton(
                text = "SEND",
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
    Box(modifier = modifier.fillMaxWidth()) {
        // Matrix background
        MatrixRain(alpha = 0.04f, density = 12)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hacker ASCII skull
            GlitchText(
                text = """
        ░██████╗██╗░░██╗██╗░░░██╗██╗░░░░░██╗░░░░░
        ██╔════╝██║░██╔╝██║░░░██║██║░░░░░██║░░░░░
        ╚█████╗░█████═╝░██║░░░██║██║░░░░░██║░░░░░
        ░╚═══██╗██╔═██╗░██║░░░██║██║░░░░░██║░░░░░
        ██████╔╝██║░╚██╗╚██████╔╝███████╗███████╗
        ╚═════╝░╚═╝░░╚═╝░╚═════╝░╚══════╝╚══════╝
                """.trimIndent(),
                style = IdeTypography.ascii.copy(
                    color = IdeColors.accentGreen,
                    fontSize = 7.sp
                ),
                glitchIntensity = 0.1f
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "▀▄▀▄▀▄ D E V T A L K ▄▀▄▀▄▀",
                style = IdeTypography.code.copy(
                    color = IdeColors.accentGreen,
                    letterSpacing = 4.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Status panel
            HackerPanel(
                modifier = Modifier.padding(horizontal = 24.dp),
                borderColor = IdeColors.accentGreen
            ) {
                Text(
                    text = "┌─── SYSTEM STATUS ───────────────────┐",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                )
                Text(
                    text = "│ AGENT    : $username",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                )
                Text(
                    text = "│ CONTACTS : $contactCount connected",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                )
                Text(
                    text = "│ STATUS   : ONLINE / ENCRYPTED",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                )
                Text(
                    text = "│ TUNNEL   : ACTIVE (P2P mesh)",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                )
                Text(
                    text = "└─────────────────────────────────────┘",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick actions
            HackerPanel(
                modifier = Modifier.padding(horizontal = 24.dp),
                borderColor = IdeColors.accentCyan
            ) {
                Text(
                    text = "┌─── AVAILABLE COMMANDS ──────────────┐",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                )
                Text(
                    text = "│ [SEARCH]  Find agents on network    │",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                )
                Text(
                    text = "│ [SCAN]    Scan QR — import agent    │",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                )
                Text(
                    text = "│ [INVITE]  Share link / QR to invite │",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                )
                Text(
                    text = "│ [BOTS]    Create & use public bots  │",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentYellow)
                )
                Text(
                    text = "│ [VOICE]   Encrypted voice channel   │",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                )
                Text(
                    text = "│ [VIDEO]   Encrypted video feed      │",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                )
                Text(
                    text = "└─────────────────────────────────────┘",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private val sp = androidx.compose.ui.unit.sp
