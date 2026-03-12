package com.devtalk.messenger.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.*
import com.devtalk.messenger.bot.BotEngine
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import java.util.UUID

private val sp = androidx.compose.ui.unit.sp

@Composable
fun BotBuilderScreen(
    onBack: () -> Unit,
    onSave: (Bot) -> Unit,
    existingBot: Bot? = null
) {
    val isEditing = existingBot != null
    var step by remember { mutableIntStateOf(if (isEditing) 1 else 0) }
    // 0 = template pick, 1 = name & info, 2 = rules builder, 3 = test & publish

    var selectedTemplate by remember { mutableStateOf<BotTemplate?>(null) }
    var botName by remember { mutableStateOf(existingBot?.name ?: "") }
    var botDescription by remember { mutableStateOf(existingBot?.description ?: "") }
    var botEmoji by remember { mutableStateOf(existingBot?.avatarEmoji ?: "🤖") }
    var botWelcome by remember { mutableStateOf(existingBot?.welcomeMessage ?: "") }
    var botFallback by remember { mutableStateOf(existingBot?.fallbackMessage ?: "") }
    var rules by remember { mutableStateOf(existingBot?.rules ?: emptyList()) }
    var isPublic by remember { mutableStateOf(existingBot?.isPublic ?: true) }
    var tags by remember { mutableStateOf(existingBot?.tags?.joinToString(", ") ?: "") }

    val stepTitles = listOf("TEMPLATE", "INFO", "RULES", "PUBLISH")
    val totalSteps = if (isEditing) 3 else 4

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
                    onClick = { if (step > 0 && !isEditing || step > 1) step-- else onBack() },
                    tint = IdeColors.accentGreen
                )
                Text(
                    text = "[ BOT BUILDER :: ${if (isEditing) "EDIT" else "CREATE"} ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
            }
            NeonDivider()

            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeColors.bgSecondary)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val visibleSteps = if (isEditing) stepTitles.drop(1) else stepTitles
                val currentIdx = if (isEditing) step - 1 else step
                visibleSteps.forEachIndexed { index, title ->
                    val isCurrent = index == currentIdx
                    val isDone = index < currentIdx
                    Text(
                        text = if (isDone) "[$title ✓]" else if (isCurrent) "[$title]" else title,
                        style = IdeTypography.codeSmall.copy(
                            color = when {
                                isCurrent -> IdeColors.accentGreen
                                isDone -> IdeColors.accentCyan
                                else -> IdeColors.textComment
                            },
                            letterSpacing = 1.sp
                        )
                    )
                    if (index < visibleSteps.lastIndex) {
                        Text(" → ", style = IdeTypography.codeSmall.copy(color = IdeColors.border))
                    }
                }
            }
            NeonDivider()

            // Content
            when (step) {
                0 -> TemplatePickStep(
                    onSelect = { template ->
                        selectedTemplate = template
                        botName = ""
                        botEmoji = template.emoji
                        botDescription = template.description
                        botWelcome = template.presetWelcome
                        botFallback = template.presetFallback
                        rules = template.presetRules
                        tags = template.presetTags.joinToString(", ")
                        step = 1
                    }
                )
                1 -> InfoStep(
                    name = botName,
                    onNameChange = { botName = it },
                    description = botDescription,
                    onDescriptionChange = { botDescription = it },
                    emoji = botEmoji,
                    onEmojiChange = { botEmoji = it },
                    welcome = botWelcome,
                    onWelcomeChange = { botWelcome = it },
                    fallback = botFallback,
                    onFallbackChange = { botFallback = it },
                    onNext = { step = 2 },
                    isValid = botName.length >= 3
                )
                2 -> RulesStep(
                    rules = rules,
                    onRulesChange = { rules = it },
                    onNext = { step = 3 }
                )
                3 -> PublishStep(
                    botName = botName,
                    botEmoji = botEmoji,
                    botDescription = botDescription,
                    rules = rules,
                    isPublic = isPublic,
                    onPublicChange = { isPublic = it },
                    tags = tags,
                    onTagsChange = { tags = it },
                    botWelcome = botWelcome,
                    botFallback = botFallback,
                    onPublish = {
                        val bot = (existingBot ?: Bot()).copy(
                            name = botName.trim(),
                            description = botDescription.trim(),
                            avatarEmoji = botEmoji,
                            welcomeMessage = botWelcome.trim(),
                            fallbackMessage = botFallback.trim(),
                            rules = rules,
                            isPublic = isPublic,
                            templateId = selectedTemplate?.id ?: existingBot?.templateId ?: "",
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        )
                        onSave(bot)
                    }
                )
            }

            // Status bar
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = "BOT BUILDER", icon = Icons.Default.SmartToy, color = IdeColors.accentGreen),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(text = "${rules.size} RULES", color = IdeColors.accentCyan),
                    StatusBarItem(
                        text = if (botName.length >= 3) "VALID" else "NEED NAME",
                        color = if (botName.length >= 3) IdeColors.accentGreen else IdeColors.accentRed
                    )
                )
            )
        }

        CrtOverlay()
    }
}

// === STEP 0: Template Pick ===
@Composable
private fun TemplatePickStep(onSelect: (BotTemplate) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "[CHOOSE A TEMPLATE TO START]",
                style = IdeTypography.code.copy(color = IdeColors.accentCyan),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Pick a template — you can customize everything after.",
                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val categories = BotTemplates.categories
        categories.forEach { category ->
            item {
                Text(
                    text = "── $category ──",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(BotTemplates.all.filter { it.category == category }) { template ->
                HackerPanel(
                    borderColor = IdeColors.accentCyan.copy(alpha = 0.4f),
                    modifier = Modifier.clickable { onSelect(template) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = template.emoji,
                            style = IdeTypography.glitch.copy(fontSize = 28.sp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.name,
                                style = IdeTypography.code.copy(color = IdeColors.accentGreen)
                            )
                            Text(
                                text = template.description,
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                            )
                            Text(
                                text = "${template.presetRules.size} rules included",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = IdeColors.accentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// === STEP 1: Name & Info ===
@Composable
private fun InfoStep(
    name: String, onNameChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    emoji: String, onEmojiChange: (String) -> Unit,
    welcome: String, onWelcomeChange: (String) -> Unit,
    fallback: String, onFallbackChange: (String) -> Unit,
    onNext: () -> Unit,
    isValid: Boolean
) {
    val emojiOptions = listOf("🤖", "👾", "🎱", "🧠", "💪", "🎲", "❓", "🔧", "☠", "👽", "🦾", "📡", "🛡", "⚡", "🔮", "🎯")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "[BOT IDENTITY]",
                style = IdeTypography.code.copy(color = IdeColors.accentCyan),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Emoji picker
        item {
            Text("Avatar:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                emojiOptions.forEach { e ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (emoji == e) IdeColors.bgSelection else IdeColors.bgInput)
                            .then(
                                if (emoji == e) Modifier.neonBorder(IdeColors.accentGreen)
                                else Modifier.neonBorder(IdeColors.border)
                            )
                            .clickable { onEmojiChange(e) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(e, style = IdeTypography.code.copy(fontSize = 20.sp))
                    }
                }
            }
        }

        // Name
        item {
            Text("Bot name (3+ chars):", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            Spacer(modifier = Modifier.height(4.dp))
            IdeTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = "my_cool_bot",
                prefix = "$emoji "
            )
        }

        // Description
        item {
            Text("Description:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            Spacer(modifier = Modifier.height(4.dp))
            IdeTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = "What does this bot do?"
            )
        }

        // Welcome message
        item {
            Text("Welcome message (when user starts chat):", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            Spacer(modifier = Modifier.height(4.dp))
            IdeTextField(
                value = welcome,
                onValueChange = onWelcomeChange,
                placeholder = "Hi! I'm a bot. Type /help!",
                singleLine = false
            )
        }

        // Fallback
        item {
            Text("Fallback (when bot doesn't understand):", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            Spacer(modifier = Modifier.height(4.dp))
            IdeTextField(
                value = fallback,
                onValueChange = onFallbackChange,
                placeholder = "I don't understand. Try /help"
            )
        }

        // Next button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            IdeButton(
                text = "NEXT → RULES",
                onClick = onNext,
                icon = Icons.Default.ArrowForward,
                color = IdeColors.accentGreen,
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// === STEP 2: Rules Builder ===
@Composable
private fun RulesStep(
    rules: List<BotRule>,
    onRulesChange: (List<BotRule>) -> Unit,
    onNext: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRuleIndex by remember { mutableIntStateOf(-1) }

    if (showAddDialog || editingRuleIndex >= 0) {
        val existingRule = if (editingRuleIndex >= 0) rules.getOrNull(editingRuleIndex) else null
        RuleEditorDialog(
            existingRule = existingRule,
            onSave = { rule ->
                if (editingRuleIndex >= 0) {
                    onRulesChange(rules.toMutableList().also { it[editingRuleIndex] = rule })
                } else {
                    onRulesChange(rules + rule)
                }
                showAddDialog = false
                editingRuleIndex = -1
            },
            onDismiss = {
                showAddDialog = false
                editingRuleIndex = -1
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[RULES: ${rules.size}]",
                    style = IdeTypography.code.copy(color = IdeColors.accentCyan)
                )
                IdeButton(
                    text = "+ ADD RULE",
                    onClick = { showAddDialog = true },
                    icon = Icons.Default.Add,
                    color = IdeColors.accentCyan
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rules define how your bot responds to messages.",
                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
            )
        }

        itemsIndexed(rules) { index, rule ->
            HackerPanel(
                borderColor = if (rule.isEnabled) IdeColors.accentGreen.copy(alpha = 0.4f) else IdeColors.border
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TRIGGER: ${rule.triggerType.label()}",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                        )
                        Text(
                            text = rule.getDisplayTrigger(),
                            style = IdeTypography.code.copy(color = IdeColors.accentGreen)
                        )
                        Text(
                            text = "→ ${rule.responseType.label()} (${rule.responses.size} resp.)",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                        )
                        if (rule.responses.isNotEmpty()) {
                            Text(
                                text = "  \"${rule.responses.first().take(50)}${if (rule.responses.first().length > 50) "..." else ""}\"",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IdeIconButton(
                            icon = Icons.Default.Edit,
                            contentDescription = "Edit",
                            onClick = { editingRuleIndex = index },
                            tint = IdeColors.accentCyan
                        )
                        IdeIconButton(
                            icon = Icons.Default.Delete,
                            contentDescription = "Delete",
                            onClick = {
                                onRulesChange(rules.toMutableList().also { it.removeAt(index) })
                            },
                            tint = IdeColors.accentRed
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            IdeButton(
                text = "NEXT → TEST & PUBLISH",
                onClick = onNext,
                icon = Icons.Default.ArrowForward,
                color = IdeColors.accentGreen,
                enabled = rules.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// === Rule editor dialog ===
@Composable
private fun RuleEditorDialog(
    existingRule: BotRule?,
    onSave: (BotRule) -> Unit,
    onDismiss: () -> Unit
) {
    var triggerType by remember { mutableStateOf(existingRule?.triggerType ?: TriggerType.COMMAND) }
    var triggerValue by remember { mutableStateOf(existingRule?.triggerValue ?: "") }
    var responseType by remember { mutableStateOf(existingRule?.responseType ?: ResponseType.TEXT) }
    var responses by remember { mutableStateOf(existingRule?.responses ?: listOf("")) }
    var newResponse by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(IdeColors.bgPrimary)
                .neonBorder(IdeColors.accentCyan)
                .padding(0.dp)
                .heightIn(max = 500.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeColors.bgToolbar)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingRule != null) "[EDIT RULE]" else "[NEW RULE]",
                    style = IdeTypography.code.copy(color = IdeColors.accentCyan)
                )
            }
            NeonDivider(color = IdeColors.accentCyan)

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Trigger type
                Text("When:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TriggerType.values().forEach { tt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (triggerType == tt) IdeColors.bgSelection else IdeColors.bgInput)
                                .neonBorder(if (triggerType == tt) IdeColors.accentGreen else IdeColors.border)
                                .clickable { triggerType = tt }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tt.label(),
                                style = IdeTypography.codeSmall.copy(
                                    color = if (triggerType == tt) IdeColors.accentGreen else IdeColors.textComment,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Trigger value
                if (triggerType != TriggerType.ANY && triggerType != TriggerType.START) {
                    Text("Trigger:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                    IdeTextField(
                        value = triggerValue,
                        onValueChange = { triggerValue = it },
                        placeholder = triggerType.hint(),
                        prefix = if (triggerType == TriggerType.COMMAND) "/" else ""
                    )
                }

                // Response type
                Text("Reply with:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ResponseType.values().forEach { rt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (responseType == rt) IdeColors.bgSelection else IdeColors.bgInput)
                                .neonBorder(if (responseType == rt) IdeColors.accentGreen else IdeColors.border)
                                .clickable { responseType = rt }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = rt.label(),
                                style = IdeTypography.codeSmall.copy(
                                    color = if (responseType == rt) IdeColors.accentGreen else IdeColors.textComment,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Responses list
                Text("Responses:", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
                responses.forEachIndexed { idx, resp ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("${idx + 1}.", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                        IdeTextField(
                            value = resp,
                            onValueChange = { newVal ->
                                responses = responses.toMutableList().also { it[idx] = newVal }
                            },
                            placeholder = "Response text...",
                            modifier = Modifier.weight(1f)
                        )
                        if (responses.size > 1) {
                            IdeIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Remove",
                                onClick = { responses = responses.toMutableList().also { it.removeAt(idx) } },
                                tint = IdeColors.accentRed
                            )
                        }
                    }
                }

                // Add response
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IdeTextField(
                        value = newResponse,
                        onValueChange = { newResponse = it },
                        placeholder = "Add another response...",
                        modifier = Modifier.weight(1f)
                    )
                    IdeButton(
                        text = "+",
                        onClick = {
                            if (newResponse.isNotBlank()) {
                                responses = responses + newResponse
                                newResponse = ""
                            }
                        },
                        color = IdeColors.accentCyan,
                        enabled = newResponse.isNotBlank()
                    )
                }

                // Save
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    IdeButton(text = "CANCEL", onClick = onDismiss, color = IdeColors.textComment)
                    IdeButton(
                        text = "SAVE RULE",
                        onClick = {
                            val validResponses = responses.filter { it.isNotBlank() }
                            if (validResponses.isNotEmpty()) {
                                onSave(
                                    BotRule(
                                        id = existingRule?.id ?: UUID.randomUUID().toString().take(8),
                                        triggerType = triggerType,
                                        triggerValue = triggerValue.trim(),
                                        responseType = responseType,
                                        responses = validResponses,
                                        isEnabled = true
                                    )
                                )
                            }
                        },
                        icon = Icons.Default.Check,
                        color = IdeColors.accentGreen,
                        enabled = responses.any { it.isNotBlank() }
                    )
                }
            }
        }
    }
}

// === STEP 3: Test & Publish ===
@Composable
private fun PublishStep(
    botName: String,
    botEmoji: String,
    botDescription: String,
    rules: List<BotRule>,
    isPublic: Boolean,
    onPublicChange: (Boolean) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    botWelcome: String,
    botFallback: String,
    onPublish: () -> Unit
) {
    val testBot = Bot(
        name = botName,
        avatarEmoji = botEmoji,
        description = botDescription,
        rules = rules,
        welcomeMessage = botWelcome,
        fallbackMessage = botFallback
    )
    val errors = BotEngine.validateBot(testBot)
    var testInput by remember { mutableStateOf("") }
    var testMessages by remember { mutableStateOf(listOf<Pair<Boolean, String>>()) } // isUser, text

    // Auto-show welcome
    LaunchedEffect(Unit) {
        val welcome = BotEngine.processMessage(testBot, "/start", isFirstMessage = true)
        if (welcome != null) {
            testMessages = listOf(false to welcome)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Summary
        item {
            HackerPanel(borderColor = IdeColors.accentGreen) {
                Text(
                    text = "╔══ BOT SUMMARY ═══════════════════╗",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                )
                Text(
                    text = "║ $botEmoji $botName",
                    style = IdeTypography.code.copy(color = IdeColors.accentGreen)
                )
                Text(
                    text = "║ $botDescription",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                )
                Text(
                    text = "║ ${rules.size} rules configured",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                )
                val cmds = BotEngine.getCommandList(testBot)
                if (cmds.isNotEmpty()) {
                    Text(
                        text = "║ Commands: ${cmds.joinToString(", ") { it.first }}",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                    )
                }
                Text(
                    text = "╚══════════════════════════════════╝",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                )
            }
        }

        // Errors
        if (errors.isNotEmpty()) {
            item {
                HackerPanel(borderColor = IdeColors.accentRed) {
                    Text("[ERRORS]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed))
                    errors.forEach { err ->
                        Text("  ✗ $err", style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed))
                    }
                }
            }
        }

        // Test console
        item {
            Text(
                text = "[TEST YOUR BOT]",
                style = IdeTypography.code.copy(color = IdeColors.accentCyan),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        item {
            HackerPanel(borderColor = IdeColors.border) {
                Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                    testMessages.forEach { (isUser, msg) ->
                        Text(
                            text = if (isUser) "> $msg" else "$botEmoji $msg",
                            style = IdeTypography.codeSmall.copy(
                                color = if (isUser) IdeColors.accentGreen else IdeColors.accentCyan
                            ),
                            modifier = Modifier.padding(vertical = 2.dp)
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
                        placeholder = "type a test message...",
                        modifier = Modifier.weight(1f)
                    )
                    IdeButton(
                        text = "TEST",
                        onClick = {
                            if (testInput.isNotBlank()) {
                                testMessages = testMessages + (true to testInput)
                                val response = BotEngine.processMessage(testBot, testInput)
                                if (response != null) {
                                    testMessages = testMessages + (false to response)
                                }
                                testInput = ""
                            }
                        },
                        enabled = testInput.isNotBlank(),
                        color = IdeColors.accentCyan
                    )
                }
            }
        }

        // Visibility
        item {
            HackerPanel(borderColor = IdeColors.accentPurple) {
                Text("[VISIBILITY]", style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPublic) IdeColors.bgSelection else IdeColors.bgInput)
                            .neonBorder(if (isPublic) IdeColors.accentGreen else IdeColors.border)
                            .clickable { onPublicChange(true) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("PUBLIC", style = IdeTypography.codeSmall.copy(color = if (isPublic) IdeColors.accentGreen else IdeColors.textComment))
                            Text("Anyone can find & use", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (!isPublic) IdeColors.bgSelection else IdeColors.bgInput)
                            .neonBorder(if (!isPublic) IdeColors.accentYellow else IdeColors.border)
                            .clickable { onPublicChange(false) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("PRIVATE", style = IdeTypography.codeSmall.copy(color = if (!isPublic) IdeColors.accentYellow else IdeColors.textComment))
                            Text("Only via direct link", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment, fontSize = 9.sp))
                        }
                    }
                }
            }
        }

        // Tags
        item {
            Text("Tags (comma-separated):", style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary))
            IdeTextField(
                value = tags,
                onValueChange = onTagsChange,
                placeholder = "fun, game, quiz"
            )
        }

        // Publish button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            IdeButton(
                text = "⚡ DEPLOY BOT",
                onClick = onPublish,
                icon = Icons.Default.RocketLaunch,
                color = IdeColors.accentGreen,
                enabled = errors.isEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
