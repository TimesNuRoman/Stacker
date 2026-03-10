package com.devtalk.messenger.ui.screens

import android.graphics.Bitmap
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
import com.devtalk.messenger.data.model.*
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography

private val sp = androidx.compose.ui.unit.sp

@Composable
fun UserProfileScreen(
    user: User,
    currentUser: User,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onCall: (CallType) -> Unit,
    isContact: Boolean,
    onAddContact: () -> Unit,
    // Wall
    wallPosts: List<WallPost> = emptyList(),
    wallComments: Map<String, List<WallComment>> = emptyMap(),
    onWallPost: (String, WallPostType) -> Unit = { _, _ -> },
    onWallLike: (WallPost) -> Unit = {},
    onWallComment: (WallPost, String) -> Unit = { _, _ -> },
    onLoadComments: (String) -> Unit = {}
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0=wall, 1=info

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
                HackerAvatar(
                    emoji = user.avatarEmoji,
                    asciiArt = user.avatarAscii,
                    color = try { Color(android.graphics.Color.parseColor(user.avatarColor)) } catch (_: Exception) { IdeColors.accentGreen },
                    size = 72.dp,
                    statusColor = when (user.status) {
                        UserStatus.ONLINE -> IdeColors.online
                        UserStatus.AWAY -> IdeColors.away
                        UserStatus.DO_NOT_DISTURB -> IdeColors.dnd
                        UserStatus.OFFLINE -> IdeColors.offline
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("@ ${user.username}", style = IdeTypography.codeLarge.copy(color = IdeColors.accentGreen))
                Text(user.bio, style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    if (!isContact) {
                        IdeButton(text = "+ ADD", onClick = onAddContact, icon = Icons.Default.PersonAdd, color = IdeColors.accentCyan)
                    }
                    IdeButton(text = "MSG", onClick = onMessage, icon = Icons.Default.Chat, color = IdeColors.accentGreen)
                    IdeButton(text = "CALL", onClick = { onCall(CallType.AUDIO) }, icon = Icons.Default.Phone, color = IdeColors.accentPurple)
                }
            }
            NeonDivider()

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(IdeColors.bgSecondary)
            ) {
                listOf("WALL", "INFO").forEachIndexed { idx, title ->
                    Text(
                        text = "[$title]",
                        style = IdeTypography.codeSmall.copy(
                            color = if (activeTab == idx) IdeColors.accentGreen else IdeColors.textComment
                        ),
                        modifier = Modifier
                            .clickable { activeTab = idx }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            NeonDivider()

            when (activeTab) {
                0 -> {
                    // Wall — visitors can post too
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Guest post composer
                        item {
                            WallPostComposer(
                                authorEmoji = currentUser.avatarEmoji,
                                authorName = currentUser.username,
                                onPost = onWallPost
                            )
                        }

                        if (wallPosts.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("[WALL IS EMPTY]", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                                    Text("Be the first to write here!", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                                }
                            }
                        }

                        items(wallPosts, key = { it.id }) { post ->
                            WallPostCard(
                                post = post,
                                currentUid = currentUser.uid,
                                isOwnerWall = false,
                                comments = wallComments[post.id] ?: emptyList(),
                                onLike = { onWallLike(post) },
                                onComment = { text -> onWallComment(post, text) },
                                onDelete = null
                            )
                            LaunchedEffect(post.id) { onLoadComments(post.id) }
                        }
                    }
                }
                1 -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HackerPanel(borderColor = IdeColors.accentGreen) {
                            Text("╔══ AGENT INFO ════════════════════╗", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen))
                            Text("║ HANDLE  : ${user.username}", style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan))
                            Text("║ STATUS  : ${user.status.toIdeString()}", style = IdeTypography.codeSmall.copy(
                                color = when (user.status) {
                                    UserStatus.ONLINE -> IdeColors.accentGreen
                                    UserStatus.AWAY -> IdeColors.accentYellow
                                    UserStatus.DO_NOT_DISTURB -> IdeColors.accentRed
                                    UserStatus.OFFLINE -> IdeColors.textComment
                                }
                            ))
                            Text("║ BIO     : ${user.bio}", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                            Text("╚═════════════════════════════════╝", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen))
                        }
                    }
                }
            }

            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = user.username, icon = Icons.Default.Person, color = IdeColors.accentGreen),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(text = "${wallPosts.size} POSTS", color = IdeColors.accentCyan),
                    StatusBarItem(text = user.status.toIdeString(), color = if (user.status == UserStatus.ONLINE) IdeColors.accentGreen else IdeColors.textComment)
                )
            )
        }

        CrtOverlay()
    }
}
