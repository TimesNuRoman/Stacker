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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.devtalk.messenger.data.model.Attachment
import com.devtalk.messenger.data.model.AttachmentType
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography

private val sp = androidx.compose.ui.unit.sp

// ============================================================
//  Image preview inside chat bubble
// ============================================================
@Composable
fun ImageAttachmentPreview(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 250.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(IdeColors.bgPrimary)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(attachment.url.ifBlank { attachment.localUri })
                .crossfade(true)
                .build(),
            contentDescription = attachment.fileName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Neon corner overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .neonBorder(IdeColors.accentCyan.copy(alpha = 0.2f))
        )
        // File info overlay at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = attachment.fileName.takeLast(20),
                style = IdeTypography.codeSmall.copy(color = IdeColors.textPrimary, fontSize = 9.sp),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = attachment.getSizeString(),
                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp)
            )
        }
    }
}

// ============================================================
//  Image grid for multiple images in one message
// ============================================================
@Composable
fun ImageGrid(
    images: List<Attachment>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (images.size) {
        1 -> ImageAttachmentPreview(images[0], onClick = { onImageClick(0) }, modifier = modifier)
        2 -> Row(
            modifier = modifier.fillMaxWidth().height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            images.forEachIndexed { i, img ->
                ImageAttachmentPreview(img, onClick = { onImageClick(i) }, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
        else -> Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                images.take(2).forEachIndexed { i, img ->
                    ImageAttachmentPreview(img, onClick = { onImageClick(i) }, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }
            if (images.size > 2) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    images.drop(2).take(3).forEachIndexed { i, img ->
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            ImageAttachmentPreview(img, onClick = { onImageClick(i + 2) }, modifier = Modifier.fillMaxSize())
                            if (i == 2 && images.size > 5) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+${images.size - 5}", style = IdeTypography.codeLarge.copy(color = IdeColors.accentGreen))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
//  Video attachment preview
// ============================================================
@Composable
fun VideoAttachmentPreview(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(IdeColors.bgPrimary)
            .neonBorder(IdeColors.accentPurple.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (attachment.thumbnailUrl.isNotBlank()) {
            AsyncImage(
                model = attachment.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // Play button overlay
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(IdeColors.bgPrimary.copy(alpha = 0.7f))
                .neonBorder(IdeColors.accentPurple),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = IdeColors.accentPurple, modifier = Modifier.size(28.dp))
        }
        // Info overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🎬", style = IdeTypography.codeSmall)
            Text(attachment.fileName, style = IdeTypography.codeSmall.copy(color = IdeColors.textPrimary, fontSize = 9.sp), modifier = Modifier.weight(1f))
            if (attachment.duration > 0) {
                Text(formatDuration(attachment.duration), style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple, fontSize = 9.sp))
            }
            Text(attachment.getSizeString(), style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp))
        }
    }
}

// ============================================================
//  Audio attachment (waveform-style player)
// ============================================================
@Composable
fun AudioAttachmentPreview(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(IdeColors.bgPrimary)
            .neonBorder(IdeColors.accentGreen.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IdeColors.accentGreen.copy(alpha = 0.1f))
                .neonBorder(IdeColors.accentGreen.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = IdeColors.accentGreen, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(attachment.fileName, style = IdeTypography.codeSmall.copy(color = IdeColors.textPrimary), maxLines = 1)
            // Fake waveform visualization
            Row(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                val bars = 30
                (0 until bars).forEach { i ->
                    val h = ((kotlin.math.sin(i * 0.7) * 0.5 + 0.5) * 18 + 2).dp
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(h)
                            .background(IdeColors.accentGreen.copy(alpha = 0.4f))
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (attachment.duration > 0) {
                Text(formatDuration(attachment.duration), style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen, fontSize = 10.sp))
            }
            Text(attachment.getSizeString(), style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp))
        }
    }
}

// ============================================================
//  Code file preview (shows first lines)
// ============================================================
@Composable
fun CodeAttachmentPreview(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(IdeColors.bgPrimary)
            .neonBorder(IdeColors.accentCyan.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeColors.bgToolbar)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📝", style = IdeTypography.codeSmall)
            Spacer(modifier = Modifier.width(6.dp))
            Text(attachment.fileName, style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan), modifier = Modifier.weight(1f))
            Text(attachment.getSizeString(), style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp))
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IdeColors.border))
        // Preview area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp, max = 100.dp)
                .padding(8.dp)
        ) {
            Text(
                text = "// Tap to view full file\n// ${attachment.fileName}\n// ${attachment.getSizeString()}, .${attachment.getExtension()}",
                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, lineHeight = 16.sp)
            )
        }
    }
}

// ============================================================
//  Generic file attachment card
// ============================================================
@Composable
fun FileAttachmentCard(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(IdeColors.bgPrimary)
            .neonBorder(IdeColors.border)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(IdeColors.bgSecondary)
                .neonBorder(IdeColors.border),
            contentAlignment = Alignment.Center
        ) {
            Text(attachment.getIcon(), style = IdeTypography.code.copy(fontSize = 20.sp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.fileName,
                style = IdeTypography.codeSmall.copy(color = IdeColors.textPrimary),
                maxLines = 1
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = attachment.getSizeString(),
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp)
                )
                Text(
                    text = ".${attachment.getExtension().uppercase()}",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan, fontSize = 9.sp)
                )
                Text(
                    text = attachment.type.label(),
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Download",
            tint = IdeColors.accentGreen,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ============================================================
//  Attachment renderer — picks the right preview for each type
// ============================================================
@Composable
fun AttachmentRenderer(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (attachment.type) {
        AttachmentType.IMAGE -> ImageAttachmentPreview(attachment, onClick, modifier)
        AttachmentType.VIDEO -> VideoAttachmentPreview(attachment, onClick, modifier)
        AttachmentType.AUDIO -> AudioAttachmentPreview(attachment, onClick, modifier)
        AttachmentType.CODE -> CodeAttachmentPreview(attachment, onClick, modifier)
        else -> FileAttachmentCard(attachment, onClick, modifier)
    }
}

// ============================================================
//  Pending attachment strip (before sending)
// ============================================================
@Composable
fun PendingAttachmentStrip(
    attachments: List<Attachment>,
    uploadProgress: Map<String, Float>,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        attachments.forEachIndexed { index, att ->
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(IdeColors.bgPrimary)
                    .neonBorder(IdeColors.accentCyan.copy(alpha = 0.3f))
            ) {
                when (att.type) {
                    AttachmentType.IMAGE -> {
                        AsyncImage(
                            model = att.localUri.ifBlank { att.url },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(att.getIcon(), style = IdeTypography.code.copy(fontSize = 18.sp))
                            Text(
                                att.getExtension().uppercase().take(4),
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 8.sp)
                            )
                        }
                    }
                }

                // Upload progress
                val progress = uploadProgress[att.id]
                if (progress != null && progress < 1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${(progress * 100).toInt()}%", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen, fontSize = 10.sp))
                    }
                }

                // Remove button
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = IdeColors.accentRed,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { onRemove(index) }
                        .padding(2.dp)
                )
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val secs = millis / 1000
    val m = secs / 60
    val s = secs % 60
    return "%d:%02d".format(m, s)
}
