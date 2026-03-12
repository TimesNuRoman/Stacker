package com.devtalk.messenger.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.devtalk.messenger.data.model.Attachment
import com.devtalk.messenger.data.model.AttachmentType
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography

private val sp = androidx.compose.ui.unit.sp

// ============================================================
//  Full-screen image gallery viewer with pager
// ============================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGalleryViewer(
    images: List<Attachment>,
    initialIndex: Int = 0,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }
    var showControls by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
            .clickable { showControls = !showControls }
    ) {
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val img = images[page]
            var scale by remember { mutableFloatStateOf(1f) }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img.url.ifBlank { img.localUri })
                    .crossfade(true)
                    .build(),
                contentDescription = img.fileName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )
        }

        // Controls overlay
        if (showControls) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeColors.bgPrimary.copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IdeIconButton(icon = Icons.Default.ArrowBack, contentDescription = "Back", onClick = onBack, tint = IdeColors.accentGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        images.getOrNull(pagerState.currentPage)?.fileName ?: "",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                        maxLines = 1
                    )
                    Text(
                        "${pagerState.currentPage + 1} / ${images.size}",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp)
                    )
                }
                IdeIconButton(
                    icon = Icons.Default.OpenInBrowser,
                    contentDescription = "Open externally",
                    onClick = {
                        val url = images.getOrNull(pagerState.currentPage)?.url
                        if (!url.isNullOrBlank()) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                    tint = IdeColors.accentCyan
                )
            }

            // Bottom info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeColors.bgPrimary.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val current = images.getOrNull(pagerState.currentPage)
                current?.let {
                    Text(it.getSizeString(), style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(it.mimeType, style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                    if (it.width > 0 && it.height > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${it.width}×${it.height}", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                    }
                }
            }

            // Page indicator dots
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    images.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == pagerState.currentPage) 8.dp else 5.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (i == pagerState.currentPage) IdeColors.accentGreen
                                    else IdeColors.accentGreen.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
//  Generic file detail viewer
// ============================================================
@Composable
fun FileDetailViewer(
    attachment: Attachment,
    onBack: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(IdeColors.bgToolbar)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IdeIconButton(icon = Icons.Default.ArrowBack, contentDescription = "Back", onClick = onBack, tint = IdeColors.accentGreen)
                Text("[ FILE VIEWER ]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen), modifier = Modifier.weight(1f))
            }
            NeonDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Big icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(IdeColors.bgSecondary)
                        .neonBorder(IdeColors.accentCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Text(attachment.getIcon(), style = IdeTypography.glitch.copy(fontSize = 40.sp))
                }

                // File info
                HackerPanel(borderColor = IdeColors.accentCyan) {
                    Text("╔══ FILE INFO ═══════════════════╗", style = IdeTypography.codeSmall.copy(color = IdeColors.border))
                    Text("║ NAME : ${attachment.fileName}", style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen))
                    Text("║ SIZE : ${attachment.getSizeString()}", style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan))
                    Text("║ TYPE : ${attachment.mimeType}", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                    Text("║ EXT  : .${attachment.getExtension()}", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                    Text("║ KIND : ${attachment.type.label()}", style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple))
                    if (attachment.width > 0) {
                        Text("║ DIM  : ${attachment.width}×${attachment.height}", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                    }
                    if (attachment.duration > 0) {
                        val s = attachment.duration / 1000
                        Text("║ DUR  : ${s / 60}m ${s % 60}s", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                    }
                    Text("╚═══════════════════════════════╝", style = IdeTypography.codeSmall.copy(color = IdeColors.border))
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IdeButton(
                        text = "DOWNLOAD",
                        onClick = onDownload,
                        icon = Icons.Default.Download,
                        color = IdeColors.accentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    IdeButton(
                        text = "OPEN",
                        onClick = {
                            if (attachment.url.isNotBlank()) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(attachment.url)))
                            }
                        },
                        icon = Icons.Default.OpenInBrowser,
                        color = IdeColors.accentCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = attachment.type.label().uppercase(), icon = Icons.Default.AttachFile, color = IdeColors.accentGreen),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(text = attachment.getSizeString(), color = IdeColors.accentCyan)
                )
            )
        }

        CrtOverlay()
    }
}
