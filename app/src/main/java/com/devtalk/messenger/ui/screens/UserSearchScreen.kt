package com.devtalk.messenger.ui.screens

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
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.User
import com.devtalk.messenger.data.model.UserStatus
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import kotlinx.coroutines.delay

@Composable
fun UserSearchScreen(
    onBack: () -> Unit,
    onAddUser: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onOpenInvite: () -> Unit,
    searchResults: List<User>,
    onlineUsers: List<User>,
    recentUsers: List<User>,
    onSearchQuery: (String) -> Unit,
    isSearching: Boolean,
    currentUid: String,
    existingContactUids: Set<String>
) {
    var query by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(0) } // 0=search, 1=online, 2=recent

    LaunchedEffect(query) {
        if (query.length >= 2) {
            delay(300) // debounce
            onSearchQuery(query)
        }
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
                IdeIconButton(
                    icon = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    tint = IdeColors.accentGreen
                )
                Text(
                    text = "[ FIND AGENTS ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
                IdeIconButton(
                    icon = Icons.Default.QrCodeScanner,
                    contentDescription = "QR",
                    onClick = onOpenScanner,
                    tint = IdeColors.accentCyan
                )
                IdeIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Invite",
                    onClick = onOpenInvite,
                    tint = IdeColors.accentPurple
                )
            }
            NeonDivider()

            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "find",
                    style = IdeTypography.code.copy(color = IdeColors.accentCyan)
                )
                IdeTextField(
                    value = query,
                    onValueChange = {
                        query = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '-' }
                    },
                    placeholder = "search by handle...",
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Search
                )
                if (query.isNotEmpty()) {
                    IdeIconButton(
                        icon = Icons.Default.Clear,
                        contentDescription = "Clear",
                        onClick = { query = "" },
                        tint = IdeColors.textComment
                    )
                }
            }

            // Tab bar for discovery modes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(IdeColors.bgSecondary),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("SEARCH", "ONLINE", "RECENT").forEachIndexed { index, title ->
                    val isSelected = activeTab == index
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = index }
                            .padding(horizontal = 16.dp, vertical = 0.dp)
                            .height(32.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "[$title]",
                            style = IdeTypography.codeSmall.copy(
                                color = if (isSelected) IdeColors.accentGreen else IdeColors.textComment
                            )
                        )
                    }
                }
            }
            NeonDivider()

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> {
                        // Search results
                        if (query.length < 2) {
                            item {
                                EmptyStateHint(
                                    title = "[TYPE TO SEARCH]",
                                    subtitle = "Enter at least 2 characters to find agents",
                                    icon = Icons.Default.Search
                                )
                            }
                        } else if (isSearching) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "[SCANNING NETWORK...]",
                                        style = IdeTypography.code.copy(color = IdeColors.accentCyan)
                                    )
                                    BlinkingCursor(color = IdeColors.accentCyan)
                                }
                            }
                        } else if (searchResults.isEmpty()) {
                            item {
                                EmptyStateHint(
                                    title = "[NO AGENTS FOUND]",
                                    subtitle = "No matches for \"$query\"",
                                    icon = Icons.Default.SearchOff
                                )
                            }
                            item {
                                // Suggest inviting
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "[INVITE THEM TO DEVTALK]",
                                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IdeButton(
                                        text = "SEND INVITE",
                                        onClick = onOpenInvite,
                                        icon = Icons.Default.PersonAdd,
                                        color = IdeColors.accentPurple
                                    )
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = "  [${searchResults.size} RESULTS]",
                                    style = IdeTypography.codeSmall.copy(color = IdeColors.textComment),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(searchResults) { user ->
                                UserListItem(
                                    user = user,
                                    isCurrentUser = user.uid == currentUid,
                                    isAlreadyContact = user.uid in existingContactUids,
                                    onAdd = { onAddUser(user.username) }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Online users
                        if (onlineUsers.isEmpty()) {
                            item {
                                EmptyStateHint(
                                    title = "[NO AGENTS ONLINE]",
                                    subtitle = "Invite friends to join the network",
                                    icon = Icons.Default.WifiOff
                                )
                            }
                        } else {
                            item {
                                Text(
                                    text = "  [${onlineUsers.size} ONLINE]",
                                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(onlineUsers) { user ->
                                UserListItem(
                                    user = user,
                                    isCurrentUser = user.uid == currentUid,
                                    isAlreadyContact = user.uid in existingContactUids,
                                    onAdd = { onAddUser(user.username) }
                                )
                            }
                        }
                    }
                    2 -> {
                        // Recently joined
                        if (recentUsers.isEmpty()) {
                            item {
                                EmptyStateHint(
                                    title = "[NO RECENT AGENTS]",
                                    subtitle = "Be the first to bring friends",
                                    icon = Icons.Default.History
                                )
                            }
                        } else {
                            item {
                                Text(
                                    text = "  [RECENTLY JOINED]",
                                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(recentUsers) { user ->
                                UserListItem(
                                    user = user,
                                    isCurrentUser = user.uid == currentUid,
                                    isAlreadyContact = user.uid in existingContactUids,
                                    onAdd = { onAddUser(user.username) }
                                )
                            }
                        }
                    }
                }

                // Always show invite CTA at bottom
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    InviteCTABanner(onOpenInvite = onOpenInvite, onOpenScanner = onOpenScanner)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Status bar
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = "SEARCH", icon = Icons.Default.Search, color = IdeColors.accentGreen),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(
                        text = "${onlineUsers.size} ONLINE",
                        color = IdeColors.accentGreen
                    ),
                    StatusBarItem(text = "E2E", color = IdeColors.accentCyan)
                )
            )
        }
    }
}

@Composable
private fun UserListItem(
    user: User,
    isCurrentUser: Boolean,
    isAlreadyContact: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrentUser && !isAlreadyContact, onClick = onAdd)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status indicator
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

        // Username
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                style = IdeTypography.code.copy(
                    color = if (isCurrentUser) IdeColors.textComment else IdeColors.accentGreen
                )
            )
            Text(
                text = user.status.toDisplayString() + if (isCurrentUser) " (you)" else "",
                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
            )
        }

        // Action
        when {
            isCurrentUser -> {
                Text("[YOU]", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
            }
            isAlreadyContact -> {
                Text("[ADDED ✓]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen))
            }
            else -> {
                IdeButton(
                    text = "+ ADD",
                    onClick = onAdd,
                    color = IdeColors.accentCyan
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(1.dp)
            .background(IdeColors.border)
    )
}

@Composable
private fun EmptyStateHint(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IdeColors.textComment,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, style = IdeTypography.code.copy(color = IdeColors.textComment))
        Text(text = subtitle, style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
    }
}

@Composable
fun InviteCTABanner(
    onOpenInvite: () -> Unit,
    onOpenScanner: () -> Unit
) {
    HackerPanel(
        modifier = Modifier.padding(horizontal = 12.dp),
        borderColor = IdeColors.accentPurple
    ) {
        Text(
            text = "[GROW THE NETWORK]",
            style = IdeTypography.code.copy(color = IdeColors.accentPurple)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Invite friends — share your link or QR code",
            style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IdeButton(
                text = "INVITE",
                onClick = onOpenInvite,
                icon = Icons.Default.Share,
                color = IdeColors.accentPurple,
                modifier = Modifier.weight(1f)
            )
            IdeButton(
                text = "SCAN QR",
                onClick = onOpenScanner,
                icon = Icons.Default.QrCodeScanner,
                color = IdeColors.accentCyan,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
