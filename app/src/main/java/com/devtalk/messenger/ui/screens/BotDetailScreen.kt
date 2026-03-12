package com.devtalk.messenger.ui.screens

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
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.bot.BotEngine
import com.devtalk.messenger.data.model.Bot
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography

private val sp = androidx.compose.ui.unit.sp

@Composable
fun BotDetailScreen(
    bot: Bot,
    onBack: () -> Unit,
    onAddToChat: (Bot) -> Unit,
    onEdit: ((Bot) -> Unit)? = null,
    onDelete: ((Bot) -> Unit)? = null,
    isOwner: Boolean = false
) {
    var testInput by remember { mutableStateOf("") }
    var testMessages by remember { mutableStateOf(listOf<Pair<Boolean, String>>()) }

    LaunchedEffect(bot) {
        val welcome = BotEngine.processMessage(bot, "/start", isFirstMessage = true)
        if (welcome != null) testMessages = listOf(false to welcome)
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
                    text = "[ BOT :: ${bot.name} ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
                if (isOwner) {
                    IdeIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit",
                        onClick = { onEdit?.invoke(bot) },
                        tint = IdeColors.accentCyan
                    )
                    IdeIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete",
                        onClick = { onDelete?.invoke(bot) },
                        tint = IdeColors.accentRed
                    )
                }
            }
            NeonDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bot identity
                HackerPanel(borderColor = IdeColors.accentGreen) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(bot.avatarEmoji, style = IdeTypography.glitch.copy(fontSize = 40.sp))
                        Column {
                            Text(bot.name, style = IdeTypography.codeLarge.copy(color = IdeColors.accentGreen))
                            Text(bot.description, style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                            Text("by ${bot.creatorName}", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${bot.addedByCount}", style = IdeTypography.code.copy(color = IdeColors.accentCyan))
                            Text("users", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${bot.usageCount}", style = IdeTypography.code.copy(color = IdeColors.accentPurple))
                            Text("messages", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${bot.rules.size}", style = IdeTypography.code.copy(color = IdeColors.accentGreen))
                            Text("rules", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp))
                        }
                    }
                }

                // Add button
                IdeButton(
                    text = "⚡ START CHAT WITH BOT",
                    onClick = { onAddToChat(bot) },
                    icon = Icons.Default.SmartToy,
                    color = IdeColors.accentGreen,
                    modifier = Modifier.fillMaxWidth()
                )

                // Commands list
                val commands = BotEngine.getCommandList(bot)
                if (commands.isNotEmpty()) {
                    HackerPanel(borderColor = IdeColors.accentCyan) {
                        Text("[COMMANDS]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan))
                        Spacer(modifier = Modifier.height(4.dp))
                        commands.forEach { (cmd, desc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(cmd, style = IdeTypography.code.copy(color = IdeColors.accentGreen))
                                Text("— ${desc.take(40)}", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                            }
                        }
                    }
                }

                // Tags
                if (bot.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        bot.tags.forEach { tag ->
                            Text(
                                text = "#$tag",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(IdeColors.accentPurple.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Test console
                HackerPanel(borderColor = IdeColors.border) {
                    Text("[TRY IT]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan))
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(modifier = Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
                        testMessages.forEach { (isUser, msg) ->
                            Text(
                                text = if (isUser) "> $msg" else "${bot.avatarEmoji} $msg",
                                style = IdeTypography.codeSmall.copy(
                                    color = if (isUser) IdeColors.accentGreen else IdeColors.accentCyan
                                ),
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(">", style = IdeTypography.code.copy(color = IdeColors.accentGreen))
                        IdeTextField(
                            value = testInput,
                            onValueChange = { testInput = it },
                            placeholder = "try a command...",
                            modifier = Modifier.weight(1f)
                        )
                        IdeButton(
                            text = "GO",
                            onClick = {
                                if (testInput.isNotBlank()) {
                                    testMessages = testMessages + (true to testInput)
                                    val resp = BotEngine.processMessage(bot, testInput)
                                    if (resp != null) testMessages = testMessages + (false to resp)
                                    testInput = ""
                                }
                            },
                            enabled = testInput.isNotBlank(),
                            color = IdeColors.accentCyan
                        )
                    }
                }
            }

            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = bot.name, icon = Icons.Default.SmartToy, color = IdeColors.accentGreen),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(text = "${bot.rules.size} RULES", color = IdeColors.accentCyan)
                )
            )
        }

        CrtOverlay()
    }
}
