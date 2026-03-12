package com.devtalk.messenger.ui.components

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
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.WallComment
import com.devtalk.messenger.data.model.WallPost
import com.devtalk.messenger.data.model.WallPostType
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import java.text.SimpleDateFormat
import java.util.*

private val sp = androidx.compose.ui.unit.sp

@Composable
fun WallPostCard(
    post: WallPost,
    currentUid: String,
    isOwnerWall: Boolean,
    comments: List<WallComment> = emptyList(),
    onLike: () -> Unit,
    onComment: (String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onAuthorClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    val isLiked = post.isLikedBy(currentUid)
    val timeAgo = formatTimeAgo(post.timestamp)

    HackerPanel(
        borderColor = IdeColors.border,
        modifier = modifier
    ) {
        // Header: author + time
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HackerAvatar(
                emoji = post.authorEmoji,
                color = IdeColors.accentGreen,
                size = 32.dp,
                showBorder = false
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.authorName,
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = if (onAuthorClick != null) Modifier.clickable(onClick = onAuthorClick) else Modifier
                )
                Text(
                    text = timeAgo,
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp)
                )
            }
            if (post.authorUid == currentUid || isOwnerWall) {
                onDelete?.let {
                    IdeIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Delete",
                        onClick = it,
                        tint = IdeColors.accentRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when (post.type) {
            WallPostType.TEXT -> {
                Text(
                    text = post.content,
                    style = IdeTypography.code.copy(color = IdeColors.textPrimary, lineHeight = 20.sp)
                )
            }
            WallPostType.CODE -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(IdeColors.bgPrimary)
                        .neonBorder(IdeColors.accentCyan.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = post.content,
                        style = IdeTypography.code.copy(
                            color = IdeColors.accentGreen,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
            WallPostType.ASCII_ART -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = post.content,
                        style = IdeTypography.ascii.copy(
                            color = IdeColors.accentGreen,
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions: like + comment
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            Row(
                modifier = Modifier.clickable(onClick = onLike),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) IdeColors.accentRed else IdeColors.textComment,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (post.likeCount > 0) "${post.likeCount}" else "++",
                    style = IdeTypography.codeSmall.copy(
                        color = if (isLiked) IdeColors.accentRed else IdeColors.textComment
                    )
                )
            }

            // Comment toggle
            Row(
                modifier = Modifier.clickable { showComments = !showComments },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comments",
                    tint = IdeColors.textComment,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (post.commentsCount > 0) "${post.commentsCount}" else "//",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                )
            }
        }

        // Comments section
        if (showComments) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(IdeColors.border)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Existing comments
            comments.forEach { comment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${comment.authorEmoji} ",
                        style = IdeTypography.codeSmall
                    )
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = comment.authorName,
                                style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                            )
                            Text(
                                text = formatTimeAgo(comment.timestamp),
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp)
                            )
                        }
                        Text(
                            text = comment.content,
                            style = IdeTypography.codeSmall.copy(color = IdeColors.textPrimary)
                        )
                    }
                }
            }

            // Comment input
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("//", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                IdeTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = "add comment...",
                    modifier = Modifier.weight(1f)
                )
                IdeIconButton(
                    icon = Icons.Default.Send,
                    contentDescription = "Send",
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onComment(commentText)
                            commentText = ""
                        }
                    },
                    tint = IdeColors.accentGreen
                )
            }
        }
    }
}

// Post composer
@Composable
fun WallPostComposer(
    authorEmoji: String,
    authorName: String,
    onPost: (String, WallPostType) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf(WallPostType.TEXT) }

    HackerPanel(
        borderColor = IdeColors.accentGreen.copy(alpha = 0.3f),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HackerAvatar(emoji = authorEmoji, size = 28.dp, showBorder = false)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$authorName@wall:~\$",
                style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Type selector
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WallPostType.values().forEach { type ->
                val label = when (type) {
                    WallPostType.TEXT -> "TEXT"
                    WallPostType.CODE -> "CODE"
                    WallPostType.ASCII_ART -> "ASCII"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (postType == type) IdeColors.bgSelection else Color.Transparent)
                        .then(
                            if (postType == type) Modifier.neonBorder(IdeColors.accentGreen.copy(alpha = 0.5f))
                            else Modifier
                        )
                        .clickable { postType = type }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "[$label]",
                        style = IdeTypography.codeSmall.copy(
                            color = if (postType == type) IdeColors.accentGreen else IdeColors.textComment,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        IdeTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = when (postType) {
                WallPostType.TEXT -> "Write on the wall..."
                WallPostType.CODE -> "Paste your code..."
                WallPostType.ASCII_ART -> "Create ASCII art..."
            },
            singleLine = false
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IdeButton(
                text = "POST",
                onClick = {
                    if (text.isNotBlank()) {
                        onPost(text, postType)
                        text = ""
                    }
                },
                icon = Icons.Default.Send,
                color = IdeColors.accentGreen,
                enabled = text.isNotBlank()
            )
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
