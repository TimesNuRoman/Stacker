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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import com.devtalk.messenger.util.UsernameGenerator
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onCreateAccount: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var username by remember { mutableStateOf("") }
    var bootLines by remember { mutableStateOf(listOf<String>()) }
    var showInput by remember { mutableStateOf(false) }
    var showSkull by remember { mutableStateOf(false) }
    var phase by remember { mutableIntStateOf(0) }
    var suggestedNames by remember { mutableStateOf(UsernameGenerator.generateBatch(6)) }

    // Boot sequence
    LaunchedEffect(Unit) {
        delay(400)
        showSkull = true
        delay(1000)
        phase = 1

        val lines = listOf(
            "[SYS] DevTalk Secure Shell v1.0.0",
            "[SYS] ████████████████████████████",
            "",
            "[INIT] Routing through Tor nodes...",
            "[INIT] Node 1: 185.220.101.██  OK",
            "[INIT] Node 2: 51.15.███.███   OK",
            "[CRYPT] AES-256-GCM initialized",
            "[CRYPT] RSA-4096 keypair... READY",
            "[NET] P2P mesh network... ONLINE",
            "",
            "[SYS] Choose your handle ↓",
        )
        for (line in lines) {
            bootLines = bootLines + line
            delay(if (line.isEmpty()) 50 else 60)
        }
        delay(150)
        showInput = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        MatrixRain(alpha = 0.04f, density = 15)

        Column(modifier = Modifier.fillMaxSize()) {
            IdeToolbar(title = "DEVTALK :: SECURE INIT")
            IdeTabBar(
                tabs = listOf(TabItem("init.sh", icon = Icons.Default.Terminal)),
                selectedIndex = 0,
                onTabSelected = {}
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                // ASCII Logo
                AnimatedVisibility(
                    visible = showSkull,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlitchText(
                            text = "D E V T A L K",
                            style = IdeTypography.glitch.copy(letterSpacing = 6.sp),
                            glitchIntensity = 0.15f
                        )
                        Text(
                            text = "ENCRYPTED · ANONYMOUS · EPHEMERAL",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan, letterSpacing = 2.sp)
                        )
                    }
                }

                // Boot log
                AnimatedVisibility(visible = phase >= 1, enter = fadeIn()) {
                    Column {
                        bootLines.forEachIndexed { index, line ->
                            val color = when {
                                line.startsWith("[WARN]") -> IdeColors.accentYellow
                                line.startsWith("[CRYPT]") -> IdeColors.accentCyan
                                line.contains("OK") || line.contains("READY") || line.contains("ONLINE") -> IdeColors.accentGreen
                                line.startsWith("[NET]") -> IdeColors.accentPurple
                                line.contains("█") -> IdeColors.accentGreen
                                else -> IdeColors.textSecondary
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                                Box(
                                    modifier = Modifier.width(36.dp).background(IdeColors.gutter).padding(horizontal = 4.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) { Text("${index + 1}", style = IdeTypography.lineNumber) }
                                Box(modifier = Modifier.width(1.dp).heightIn(min = 16.dp).background(IdeColors.border))
                                Text(" $line", style = IdeTypography.codeSmall.copy(color = color), modifier = Modifier.padding(top = 1.dp))
                            }
                        }
                    }
                }

                // Input section
                AnimatedVisibility(
                    visible = showInput,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        // === QUICK PICK: suggested names ===
                        HackerPanel(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            borderColor = IdeColors.accentCyan
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "[QUICK START — tap to pick]",
                                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan),
                                    modifier = Modifier.weight(1f)
                                )
                                IdeIconButton(
                                    icon = Icons.Default.Refresh,
                                    contentDescription = "Regenerate",
                                    onClick = { suggestedNames = UsernameGenerator.generateBatch(6) },
                                    tint = IdeColors.accentCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // 2 rows x 3 columns of suggested handles
                            for (row in suggestedNames.chunked(3)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    row.forEach { name ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    if (username == name) IdeColors.bgSelection
                                                    else IdeColors.bgInput
                                                )
                                                .neonBorder(
                                                    if (username == name) IdeColors.accentGreen
                                                    else IdeColors.border
                                                )
                                                .clickable { username = name }
                                                .padding(horizontal = 8.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = name,
                                                style = IdeTypography.codeSmall.copy(
                                                    color = if (username == name) IdeColors.accentGreen
                                                    else IdeColors.textSecondary
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // === OR TYPE YOUR OWN ===
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(">>>", style = IdeTypography.code.copy(color = IdeColors.accentGreen))
                            IdeTextField(
                                value = username,
                                onValueChange = { newVal ->
                                    username = newVal.lowercase().filter {
                                        it.isLetterOrDigit() || it == '_' || it == '-'
                                    }
                                },
                                placeholder = "or type your handle...",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // === GO BUTTON ===
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            IdeButton(
                                text = if (isLoading) "CONNECTING..." else "▶ LAUNCH SESSION",
                                onClick = { onCreateAccount(username) },
                                icon = if (isLoading) Icons.Default.HourglassEmpty else Icons.Default.PlayArrow,
                                color = IdeColors.accentGreen,
                                enabled = username.length >= 3 && !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Error
                        error?.let {
                            Text(
                                text = "[ERROR] $it",
                                style = IdeTypography.code.copy(color = IdeColors.accentRed),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        if (isLoading) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("[CONN] Establishing secure tunnel...", style = IdeTypography.code.copy(color = IdeColors.accentCyan))
                                BlinkingCursor(color = IdeColors.accentCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rules
                        HackerPanel(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            borderColor = IdeColors.border
                        ) {
                            Text("┌─── RULES ───────────────────────────┐", style = IdeTypography.codeSmall.copy(color = IdeColors.border))
                            Text("│ ► 3+ chars (a-z 0-9 _ -)            │", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                            Text("│ ► Unique handle across network       │", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                            Text("│ ► No email / phone / password        │", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                            Text("│ ► EXIT = wipe. No recovery.          │", style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed))
                            Text("└──────────────────────────────────────┘", style = IdeTypography.codeSmall.copy(color = IdeColors.border))
                        }
                    }
                }
            }

            IdeStatusBar(
                items = listOf(
                    StatusBarItem(text = "SECURE", icon = Icons.Default.Lock, color = IdeColors.accentGreen),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(
                        text = if (username.length >= 3) "VALID" else "MIN 3",
                        color = if (username.length >= 3) IdeColors.accentGreen else IdeColors.accentRed
                    ),
                    StatusBarItem(text = "AES-256", color = IdeColors.textComment)
                )
            )
        }

        CrtOverlay()
    }
}

private val sp = androidx.compose.ui.unit.sp
