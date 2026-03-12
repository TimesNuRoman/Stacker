package com.devtalk.messenger.ui.screens

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
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.Bot
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography

private val sp = androidx.compose.ui.unit.sp

@Composable
fun BotCatalogScreen(
    onBack: () -> Unit,
    onCreateBot: () -> Unit,
    onBotClick: (Bot) -> Unit,
    onMyBots: () -> Unit,
    publicBots: List<Bot>,
    myBots: List<Bot>,
    isLoading: Boolean
) {
    var query by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(0) } // 0=catalog, 1=my bots

    val filteredBots = if (query.length >= 2) {
        val q = query.lowercase()
        publicBots.filter {
            it.name.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.tags.any { t -> t.lowercase().contains(q) }
        }
    } else publicBots

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
                    text = "[ BOT CATALOG ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
                IdeButton(
                    text = "+ CREATE",
                    onClick = onCreateBot,
                    icon = Icons.Default.Add,
                    color = IdeColors.accentCyan
                )
            }
            NeonDivider()

            // Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IdeTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "search bots...",
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Search
                )
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(IdeColors.bgSecondary)
            ) {
                listOf("CATALOG (${publicBots.size})", "MY BOTS (${myBots.size})").forEachIndexed { idx, title ->
                    Text(
                        text = "[$title]",
                        style = IdeTypography.codeSmall.copy(
                            color = if (activeTab == idx) IdeColors.accentGreen else IdeColors.textComment
                        ),
                        modifier = Modifier
                            .clickable { activeTab = idx }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
            NeonDivider()

            // Content
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                if (activeTab == 0) {
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("[LOADING BOTS...]", style = IdeTypography.code.copy(color = IdeColors.accentCyan))
                                BlinkingCursor(color = IdeColors.accentCyan)
                            }
                        }
                    } else if (filteredBots.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🤖", style = IdeTypography.glitch.copy(fontSize = 40.sp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("[NO BOTS FOUND]", style = IdeTypography.code.copy(color = IdeColors.textComment))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Be the first to create a bot!",
                                    style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                IdeButton(
                                    text = "CREATE BOT",
                                    onClick = onCreateBot,
                                    icon = Icons.Default.Add,
                                    color = IdeColors.accentGreen
                                )
                            }
                        }
                    } else {
                        items(filteredBots) { bot ->
                            BotCard(bot = bot, onClick = { onBotClick(bot) })
                        }
                    }
                } else {
                    // My bots
                    if (myBots.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("[NO BOTS YET]", style = IdeTypography.code.copy(color = IdeColors.textComment))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Create your first bot!", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                                Spacer(modifier = Modifier.height(12.dp))
                                IdeButton(
                                    text = "CREATE BOT",
                                    onClick = onCreateBot,
                                    icon = Icons.Default.Add,
                                    color = IdeColors.accentGreen
                                )
                            }
                        }
                    } else {
                        items(myBots) { bot ->
                            BotCard(bot = bot, onClick = { onBotClick(bot) }, showStats = true)
                        }
                    }
                }
            }

            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = "BOTS", icon = Icons.Default.SmartToy, color = IdeColors.accentGreen),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(text = "${publicBots.size} PUBLIC", color = IdeColors.accentCyan),
                    StatusBarItem(text = "${myBots.size} YOURS", color = IdeColors.accentPurple)
                )
            )
        }

        CrtOverlay()
    }
}

@Composable
private fun BotCard(bot: Bot, onClick: () -> Unit, showStats: Boolean = false) {
    HackerPanel(
        borderColor = IdeColors.accentCyan.copy(alpha = 0.3f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(bot.avatarEmoji, style = IdeTypography.glitch.copy(fontSize = 28.sp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bot.name, style = IdeTypography.code.copy(color = IdeColors.accentGreen))
                Text(bot.description.take(60), style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("by ${bot.creatorName}", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                    if (bot.tags.isNotEmpty()) {
                        Text(
                            text = bot.tags.take(3).joinToString(" ") { "#$it" },
                            style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple, fontSize = 9.sp)
                        )
                    }
                }
                if (showStats) {
                    Text(
                        text = "${bot.addedByCount} users · ${bot.usageCount} msgs · ${bot.rules.size} rules",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp)
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = IdeColors.accentGreen, modifier = Modifier.size(18.dp))
        }
    }
}
