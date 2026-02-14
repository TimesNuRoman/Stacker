package com.devtalk.messenger.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onCreateAccount: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var username by remember { mutableStateOf("") }
    var showCursor by remember { mutableStateOf(true) }
    var bootLines by remember { mutableStateOf(listOf<String>()) }
    var showInput by remember { mutableStateOf(false) }

    // Blinking cursor effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            showCursor = !showCursor
        }
    }

    // Boot sequence animation
    LaunchedEffect(Unit) {
        val lines = listOf(
            "DevTalk Messenger v1.0.0",
            "Copyright (c) 2026 DevTalk Project",
            "",
            "Initializing secure connection...",
            "Loading encryption modules... OK",
            "WebRTC engine... READY",
            "Peer-to-peer tunneling... ACTIVE",
            "",
            "// No registration required.",
            "// Pick a unique handle. That's it.",
            "// Exit = account deleted. No traces.",
            ""
        )
        for (line in lines) {
            bootLines = bootLines + line
            delay(if (line.isEmpty()) 100 else 120)
        }
        showInput = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        // Title bar
        IdeToolbar(title = "DevTalk — Initialize Session")

        // Tab bar
        IdeTabBar(
            tabs = listOf(
                TabItem("welcome.sh", icon = Icons.Default.Terminal),
            ),
            selectedIndex = 0,
            onTabSelected = {}
        )

        // Main content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            bootLines.forEachIndexed { index, line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
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
                            text = "${index + 1}",
                            style = IdeTypography.lineNumber
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .heightIn(min = 20.dp)
                            .background(IdeColors.border)
                    )

                    val style = when {
                        line.startsWith("//") -> IdeTypography.comment
                        line.contains("OK") || line.contains("READY") || line.contains("ACTIVE") ->
                            IdeTypography.code.copy(color = IdeColors.accentGreen)
                        line.startsWith("DevTalk") -> IdeTypography.code.copy(color = IdeColors.textKeyword)
                        line.startsWith("Copyright") -> IdeTypography.code.copy(color = IdeColors.textComment)
                        else -> IdeTypography.code
                    }

                    Text(
                        text = line,
                        style = style,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
            }

            // Input section
            AnimatedVisibility(
                visible = showInput,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Prompt line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .background(IdeColors.gutter)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "${bootLines.size + 1}",
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
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = IdeColors.accentGreen)) {
                                    append("guest")
                                }
                                withStyle(SpanStyle(color = IdeColors.textPrimary)) {
                                    append("@")
                                }
                                withStyle(SpanStyle(color = IdeColors.textNumber)) {
                                    append("devtalk")
                                }
                                withStyle(SpanStyle(color = IdeColors.textPrimary)) {
                                    append(":~$ ")
                                }
                                withStyle(SpanStyle(color = IdeColors.textKeyword)) {
                                    append("set_username ")
                                }
                            },
                            style = IdeTypography.code,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    // Username input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = ">",
                            style = IdeTypography.code.copy(color = IdeColors.accentGreen)
                        )
                        IdeTextField(
                            value = username,
                            onValueChange = { newVal ->
                                username = newVal.lowercase().filter { it.isLetterOrDigit() || it == '_' || it == '-' }
                            },
                            placeholder = "enter_username",
                            modifier = Modifier.weight(1f)
                        )
                        IdeButton(
                            text = "▶ Run",
                            onClick = { onCreateAccount(username) },
                            icon = Icons.Default.PlayArrow,
                            color = IdeColors.accentGreen,
                            enabled = username.length >= 3 && !isLoading
                        )
                    }

                    // Error display
                    error?.let {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "// ERROR: $it",
                                style = IdeTypography.code.copy(color = IdeColors.accentRed)
                            )
                        }
                    }

                    // Loading indicator
                    if (isLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Connecting to network...${if (showCursor) "█" else " "}",
                                style = IdeTypography.code.copy(color = IdeColors.textComment)
                            )
                        }
                    }

                    // Rules / hints
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(IdeColors.bgSecondary)
                            .border(1.dp, IdeColors.border, RoundedCornerShape(3.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "/**",
                            style = IdeTypography.comment
                        )
                        Text(
                            text = " * Rules:",
                            style = IdeTypography.comment
                        )
                        Text(
                            text = " * - Username must be 3+ characters (a-z, 0-9, _, -)",
                            style = IdeTypography.comment
                        )
                        Text(
                            text = " * - Username must be unique across the network",
                            style = IdeTypography.comment
                        )
                        Text(
                            text = " * - No password, no email, no phone. Just a handle.",
                            style = IdeTypography.comment
                        )
                        Text(
                            text = " * - Closing the app = session destroyed",
                            style = IdeTypography.comment
                        )
                        Text(
                            text = " * - Share your QR / link to connect with others",
                            style = IdeTypography.comment
                        )
                        Text(
                            text = " */",
                            style = IdeTypography.comment
                        )
                    }
                }
            }
        }

        // Status bar
        IdeStatusBar(
            items = listOf(
                StatusBarItem(
                    text = "main",
                    icon = Icons.Default.AccountTree,
                    color = IdeColors.accentBlue
                ),
                StatusBarItem(text = "", fillWeight = true),
                StatusBarItem(
                    text = if (username.length >= 3) "✓ valid username" else "⚠ min 3 chars",
                    color = if (username.length >= 3) IdeColors.accentGreen else IdeColors.textComment
                ),
                StatusBarItem(
                    text = "UTF-8",
                    color = IdeColors.textSecondary
                ),
                StatusBarItem(
                    text = "Kotlin",
                    color = IdeColors.textSecondary
                )
            )
        )
    }
}
