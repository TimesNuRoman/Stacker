package com.devtalk.messenger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import kotlinx.coroutines.delay
import kotlin.random.Random

// ========================================================
//  CRT Scanline Overlay — drawn on top of any composable
// ========================================================
@Composable
fun CrtOverlay(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Horizontal scanlines
        val lineSpacing = 3.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawRect(
                color = Color.Black.copy(alpha = 0.12f),
                topLeft = Offset(0f, y),
                size = Size(size.width, 1.dp.toPx())
            )
            y += lineSpacing
        }

        // Vignette effect (darkened corners)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.5f)
                ),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.width * 0.85f
            ),
            size = size
        )
    }
}

fun Modifier.scanlines(): Modifier = this.drawWithContent {
    drawContent()
    val lineSpacing = 3.dp.toPx()
    var y = 0f
    while (y < size.height) {
        drawRect(
            color = Color.Black.copy(alpha = 0.1f),
            topLeft = Offset(0f, y),
            size = Size(size.width, 1f)
        )
        y += lineSpacing
    }
}

fun Modifier.neonBorder(
    color: Color = IdeColors.accentGreen,
    width: Float = 1f
): Modifier = this.drawBehind {
    // Outer glow
    drawRect(
        color = color.copy(alpha = 0.15f),
        topLeft = Offset(-2f, -2f),
        size = Size(size.width + 4f, size.height + 4f)
    )
    // Border
    drawRect(
        color = color.copy(alpha = 0.6f),
        topLeft = Offset.Zero,
        size = size,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width)
    )
}

// ========================================================
//  Matrix Rain Background
// ========================================================
@Composable
fun MatrixRain(
    modifier: Modifier = Modifier,
    density: Int = 20,
    speed: Long = 80L,
    alpha: Float = 0.08f
) {
    val chars = "01アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン"
    var tick by remember { mutableIntStateOf(0) }
    val columns = remember { (0 until density).map {
        MatrixColumn(
            x = Random.nextFloat(),
            speed = Random.nextInt(1, 4),
            offset = Random.nextInt(0, 30),
            chars = (0..Random.nextInt(8, 25)).map { chars.random() }
        )
    }}

    LaunchedEffect(Unit) {
        while (true) {
            delay(speed)
            tick++
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        columns.forEach { col ->
            val x = col.x * size.width
            val step = 16.dp.toPx()
            col.chars.forEachIndexed { i, ch ->
                val y = ((i + col.offset + tick * col.speed) * step) % (size.height + step * 10) - step * 5
                if (y in -step..size.height + step) {
                    val isHead = i == col.chars.lastIndex
                    val charAlpha = if (isHead) alpha * 3f else alpha * (1f - i.toFloat() / col.chars.size * 0.5f)
                    drawContext.canvas.nativeCanvas.drawText(
                        ch.toString(),
                        x,
                        y,
                        android.graphics.Paint().apply {
                            color = if (isHead)
                                android.graphics.Color.argb((charAlpha * 255).toInt().coerceIn(0, 255), 0, 255, 65)
                            else
                                android.graphics.Color.argb((charAlpha * 255).toInt().coerceIn(0, 255), 0, 200, 40)
                            textSize = 14.dp.toPx()
                            typeface = android.graphics.Typeface.MONOSPACE
                        }
                    )
                }
            }
        }
    }
}

private data class MatrixColumn(
    val x: Float,
    val speed: Int,
    val offset: Int,
    val chars: List<Char>
)

// ========================================================
//  Glitch Text — randomly shifts characters
// ========================================================
@Composable
fun GlitchText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = IdeTypography.glitch,
    glitchIntensity: Float = 0.3f,
    enabled: Boolean = true
) {
    var displayText by remember { mutableStateOf(text) }
    var showGlitch by remember { mutableStateOf(false) }
    val glitchChars = "!@#$%^&*()_+{}|:<>?█▓▒░╠╣╦╩═║"

    LaunchedEffect(text, enabled) {
        if (!enabled) {
            displayText = text
            return@LaunchedEffect
        }
        while (true) {
            delay(Random.nextLong(2000, 5000))
            showGlitch = true
            repeat(Random.nextInt(2, 5)) {
                displayText = text.map {
                    if (Random.nextFloat() < glitchIntensity) glitchChars.random() else it
                }.joinToString("")
                delay(50)
            }
            displayText = text
            showGlitch = false
        }
    }

    Box(modifier = modifier) {
        Text(text = displayText, style = style)
        if (showGlitch) {
            Text(
                text = displayText,
                style = style.copy(color = IdeColors.accentCyan.copy(alpha = 0.4f)),
                modifier = Modifier.offset(x = 2.dp, y = (-1).dp)
            )
            Text(
                text = displayText,
                style = style.copy(color = IdeColors.accentPurple.copy(alpha = 0.3f)),
                modifier = Modifier.offset(x = (-2).dp, y = 1.dp)
            )
        }
    }
}

// ========================================================
//  Blinking Cursor
// ========================================================
@Composable
fun BlinkingCursor(
    color: Color = IdeColors.accentGreen,
    style: androidx.compose.ui.text.TextStyle = IdeTypography.code
) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            visible = !visible
        }
    }
    Text(
        text = if (visible) "█" else " ",
        style = style.copy(color = color)
    )
}

// ========================================================
//  Hacker Toolbar (top bar)
// ========================================================
@Composable
fun IdeToolbar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(IdeColors.bgToolbar)
                .neonBorder(IdeColors.accentGreen.copy(alpha = 0.3f))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[ ",
                style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
            )
            Text(
                text = title,
                style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
            )
            Text(
                text = " ]",
                style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
            )
            Spacer(modifier = Modifier.weight(1f))
            actions()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            IdeColors.accentGreen.copy(alpha = 0.6f),
                            IdeColors.accentGreen,
                            IdeColors.accentGreen.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

// ========================================================
//  Tab Bar — hacker style with neon accents
// ========================================================
@Composable
fun IdeTabBar(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabClosed: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(IdeColors.bgSecondary)
            .horizontalScroll(rememberScrollState())
    ) {
        tabs.forEachIndexed { index, tab ->
            IdeTab(
                tab = tab,
                isSelected = index == selectedIndex,
                onClick = { onTabSelected(index) },
                onClose = onTabClosed?.let { { it(index) } }
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(IdeColors.border)
    )
}

data class TabItem(
    val title: String,
    val icon: ImageVector? = null,
    val hasChanges: Boolean = false,
    val unreadCount: Int = 0
)

@Composable
private fun IdeTab(
    tab: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onClose: (() -> Unit)? = null
) {
    val bgColor = if (isSelected) IdeColors.bgTabActive else Color.Transparent
    val textColor = if (isSelected) IdeColors.accentGreen else IdeColors.textComment
    val borderColor = if (isSelected) IdeColors.accentGreen else Color.Transparent

    Column {
        Row(
            modifier = Modifier
                .height(31.dp)
                .background(bgColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tab.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (isSelected) IdeColors.accentGreen else IdeColors.textComment,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = tab.title,
                style = IdeTypography.tab.copy(color = textColor),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (tab.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IdeColors.accentRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${tab.unreadCount}",
                        style = IdeTypography.codeSmall.copy(
                            color = Color.Black,
                            fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    )
                }
            }
            onClose?.let {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = IdeColors.textComment,
                    modifier = Modifier
                        .size(12.dp)
                        .clickable(onClick = it)
                )
            }
        }
        // Active tab neon underline
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(borderColor)
        )
    }
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(IdeColors.border)
    )
}

// ========================================================
//  Status Bar — hacker HUD at bottom
// ========================================================
@Composable
fun IdeStatusBar(
    items: List<StatusBarItem>,
    modifier: Modifier = Modifier
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            IdeColors.accentGreen.copy(alpha = 0.4f),
                            IdeColors.accentGreen.copy(alpha = 0.6f),
                            IdeColors.accentGreen.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(IdeColors.bgStatusBar)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0 && !item.fillWeight) {
                    Text(" │ ", style = IdeTypography.statusBar.copy(color = IdeColors.border))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = if (item.isClickable) Modifier.clickable { item.onClick?.invoke() } else Modifier
                ) {
                    item.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = item.color,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = item.text,
                        style = IdeTypography.statusBar.copy(color = item.color)
                    )
                }
                if (item.fillWeight) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class StatusBarItem(
    val text: String,
    val icon: ImageVector? = null,
    val color: Color = IdeColors.textSecondary,
    val fillWeight: Boolean = false,
    val isClickable: Boolean = false,
    val onClick: (() -> Unit)? = null
)

// ========================================================
//  Side Panel
// ========================================================
@Composable
fun IdeSidePanel(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(IdeColors.bgSidebar)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(IdeColors.bgToolbar)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[ $title ]",
                style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
            )
            Spacer(modifier = Modifier.weight(1f))
            actions()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(IdeColors.border)
        )
        content()
    }
}

// ========================================================
//  Tree Item — file tree in hacker style
// ========================================================
@Composable
fun IdeTreeItem(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = IdeColors.textSecondary,
    isSelected: Boolean = false,
    depth: Int = 0,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    statusColor: Color? = null,
    badge: String? = null,
    onClick: () -> Unit = {}
) {
    val bgColor = if (isSelected) IdeColors.bgSelection else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(bgColor)
            .then(
                if (isSelected) Modifier.drawBehind {
                    drawRect(
                        color = IdeColors.accentGreen.copy(alpha = 0.1f),
                        size = size
                    )
                    // Left neon accent
                    drawRect(
                        color = IdeColors.accentGreen,
                        topLeft = Offset.Zero,
                        size = Size(2.dp.toPx(), size.height)
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(start = (8 + depth * 16).dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isExpandable) {
            Text(
                text = if (isExpanded) "▼" else "►",
                style = IdeTypography.codeSmall.copy(
                    color = IdeColors.accentGreen,
                    fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            )
        } else {
            Spacer(modifier = Modifier.width(10.dp))
        }

        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = if (isSelected) IdeColors.accentGreen else iconTint,
                modifier = Modifier.size(13.dp)
            )
        }

        Text(
            text = text,
            style = IdeTypography.codeSmall.copy(
                color = if (isSelected) IdeColors.accentGreen else IdeColors.textSecondary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        statusColor?.let {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(it)
                    .drawBehind {
                        // Glow
                        drawCircle(
                            color = it.copy(alpha = 0.4f),
                            radius = 5.dp.toPx()
                        )
                    }
            )
        }

        badge?.let {
            Text(
                text = it,
                style = IdeTypography.codeSmall.copy(
                    color = IdeColors.accentRed,
                    fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(1.dp))
                    .background(IdeColors.accentRed.copy(alpha = 0.15f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

// ========================================================
//  Hacker Text Input — terminal prompt style
// ========================================================
@Composable
fun IdeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    prefix: String = "",
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .background(IdeColors.bgInput, RoundedCornerShape(1.dp))
            .neonBorder(IdeColors.accentGreen.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        textStyle = IdeTypography.code.copy(color = IdeColors.accentGreen),
        singleLine = singleLine,
        cursorBrush = SolidColor(IdeColors.accentGreen),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                leadingIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = IdeColors.accentGreen.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (prefix.isNotEmpty()) {
                    Text(
                        text = prefix,
                        style = IdeTypography.code.copy(color = IdeColors.accentCyan)
                    )
                }
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = IdeTypography.code.copy(color = IdeColors.textComment)
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

// ========================================================
//  Hacker Button — neon bordered
// ========================================================
@Composable
fun IdeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = IdeColors.accentGreen,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.3f
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(1.dp))
            .background(color.copy(alpha = 0.1f * alpha))
            .neonBorder(color.copy(alpha = 0.7f * alpha))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = color.copy(alpha = alpha),
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text.uppercase(),
            style = IdeTypography.codeSmall.copy(
                color = color.copy(alpha = alpha),
                letterSpacing = 2.sp
            )
        )
    }
}

// ========================================================
//  Icon Button — minimal neon
// ========================================================
@Composable
fun IdeIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = IdeColors.textSecondary
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(28.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ========================================================
//  Terminal Line — message with line numbers and neon gutter
// ========================================================
@Composable
fun TerminalLine(
    lineNumber: Int,
    text: String,
    modifier: Modifier = Modifier,
    isOwnMessage: Boolean = false,
    isSenderLine: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isOwnMessage && !isSenderLine)
                    Modifier.background(IdeColors.accentGreen.copy(alpha = 0.03f))
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
            Text(
                text = "$lineNumber",
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
            text = text,
            style = when {
                isSenderLine -> IdeTypography.code.copy(color = IdeColors.accentCyan)
                isOwnMessage -> IdeTypography.code.copy(color = IdeColors.accentGreen)
                else -> IdeTypography.code.copy(color = IdeColors.textPrimary)
            },
            modifier = Modifier
                .padding(start = 8.dp, top = 2.dp, bottom = 2.dp, end = 8.dp)
        )
    }
}

// ========================================================
//  Section Divider — neon line
// ========================================================
@Composable
fun IdeSectionDivider(text: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(IdeColors.border)
        )
        if (text.isNotEmpty()) {
            Text(
                text = " $text ",
                style = IdeTypography.comment
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(IdeColors.border)
            )
        }
    }
}

// ========================================================
//  Neon Horizontal Divider
// ========================================================
@Composable
fun NeonDivider(color: Color = IdeColors.accentGreen) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.4f),
                        color.copy(alpha = 0.8f),
                        color.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
    )
}

// ========================================================
//  Hacker typing animation
// ========================================================
@Composable
fun TypewriterText(
    fullText: String,
    style: androidx.compose.ui.text.TextStyle = IdeTypography.code,
    typingSpeed: Long = 35L,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(fullText) {
        displayedText = ""
        fullText.forEachIndexed { _, char ->
            displayedText += char
            delay(typingSpeed)
        }
        onComplete()
    }

    Row {
        Text(text = displayedText, style = style)
        if (displayedText.length < fullText.length) {
            BlinkingCursor(style = style)
        }
    }
}

// ========================================================
//  Hacker Panel (card with neon border)
// ========================================================
@Composable
fun HackerPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = IdeColors.accentGreen,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(IdeColors.bgSecondary)
            .neonBorder(borderColor.copy(alpha = 0.5f))
            .padding(12.dp),
        content = content
    )
}
