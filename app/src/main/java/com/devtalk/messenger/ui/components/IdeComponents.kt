package com.devtalk.messenger.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography

/**
 * IDE-style toolbar at the top (like IntelliJ menu bar)
 */
@Composable
fun IdeToolbar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(IdeColors.bgToolbar)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary),
            modifier = Modifier.weight(1f)
        )
        actions()
    }
    Divider(color = IdeColors.border, thickness = 1.dp)
}

/**
 * IDE-style tab bar (like editor tabs)
 */
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
    Divider(color = IdeColors.border, thickness = 1.dp)
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
    val textColor = if (isSelected) IdeColors.textPrimary else IdeColors.textSecondary

    Row(
        modifier = Modifier
            .height(32.dp)
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
                tint = if (isSelected) IdeColors.accentBlue else IdeColors.textSecondary,
                modifier = Modifier.size(14.dp)
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(IdeColors.accentBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tab.unreadCount}",
                    style = IdeTypography.codeSmall.copy(color = Color.White, fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp))
                )
            }
        }
        if (tab.hasChanges) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(IdeColors.accentBlue)
            )
        }
        onClose?.let {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close tab",
                tint = IdeColors.textSecondary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = it)
            )
        }
    }
    if (!false) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(IdeColors.border)
        )
    }
}

/**
 * IDE-style status bar at the bottom
 */
@Composable
fun IdeStatusBar(
    items: List<StatusBarItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(IdeColors.bgStatusBar)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(14.dp)
                        .background(IdeColors.border)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = if (item.isClickable) Modifier.clickable { item.onClick?.invoke() } else Modifier
            ) {
                item.icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(12.dp)
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

data class StatusBarItem(
    val text: String,
    val icon: ImageVector? = null,
    val color: Color = IdeColors.textSecondary,
    val fillWeight: Boolean = false,
    val isClickable: Boolean = false,
    val onClick: (() -> Unit)? = null
)

/**
 * IDE-style sidebar panel (like Project tool window)
 */
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
        // Panel header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(IdeColors.bgToolbar)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary),
                modifier = Modifier.weight(1f)
            )
            actions()
        }
        Divider(color = IdeColors.border, thickness = 1.dp)
        content()
    }
}

/**
 * IDE-style tree item (like project file tree)
 */
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
            .clickable(onClick = onClick)
            .padding(start = (8 + depth * 16).dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isExpandable) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = IdeColors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(14.dp))
        }

        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = text,
            style = IdeTypography.codeSmall.copy(
                color = if (isSelected) IdeColors.textPrimary else IdeColors.textSecondary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        statusColor?.let {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(it)
            )
        }

        badge?.let {
            Text(
                text = it,
                style = IdeTypography.codeSmall.copy(
                    color = IdeColors.accentBlue,
                    fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(IdeColors.accentBlue.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

/**
 * IDE-style text input (like search box)
 */
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
            .background(IdeColors.bgInput, RoundedCornerShape(2.dp))
            .border(1.dp, IdeColors.border, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        textStyle = IdeTypography.code,
        singleLine = singleLine,
        cursorBrush = SolidColor(IdeColors.textPrimary),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                leadingIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = IdeColors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (prefix.isNotEmpty()) {
                    Text(
                        text = prefix,
                        style = IdeTypography.code.copy(color = IdeColors.textKeyword)
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

/**
 * IDE-style button (like Run button)
 */
@Composable
fun IdeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = IdeColors.accentBlue,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.5f
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = alpha))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = Color.White.copy(alpha = alpha),
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = IdeTypography.codeSmall.copy(color = Color.White.copy(alpha = alpha))
        )
    }
}

/**
 * IDE-style icon button (like toolbar icons)
 */
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

/**
 * Terminal-style message line with line numbers
 */
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
            .background(if (isOwnMessage && !isSenderLine) IdeColors.bgEditor else Color.Transparent)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Line number gutter
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
        // Gutter border
        Box(
            modifier = Modifier
                .width(1.dp)
                .heightIn(min = 20.dp)
                .background(IdeColors.border)
        )
        // Content
        Text(
            text = text,
            style = when {
                isSenderLine -> IdeTypography.code.copy(color = IdeColors.textKeyword)
                isOwnMessage -> IdeTypography.code.copy(color = IdeColors.textString)
                else -> IdeTypography.code
            },
            modifier = Modifier
                .padding(start = 8.dp, top = 2.dp, bottom = 2.dp, end = 8.dp)
        )
    }
}

/**
 * Section divider in IDE style
 */
@Composable
fun IdeSectionDivider(text: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(color = IdeColors.border, modifier = Modifier.weight(1f))
        if (text.isNotEmpty()) {
            Text(
                text = " $text ",
                style = IdeTypography.comment
            )
            Divider(color = IdeColors.border, modifier = Modifier.weight(1f))
        }
    }
}
