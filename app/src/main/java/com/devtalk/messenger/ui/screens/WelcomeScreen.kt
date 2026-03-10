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
import androidx.compose.ui.graphics.Brush
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
    var bootLines by remember { mutableStateOf(listOf<String>()) }
    var showInput by remember { mutableStateOf(false) }
    var showSkull by remember { mutableStateOf(false) }
    var phase by remember { mutableIntStateOf(0) }

    // Boot sequence animation
    LaunchedEffect(Unit) {
        delay(600)

        // Phase 1: Skull
        showSkull = true
        delay(1500)
        phase = 1

        // Phase 2: Boot lines
        val lines = listOf(
            "[SYS] DevTalk Secure Shell v1.0.0",
            "[SYS] ████████████████████████████",
            "",
            "[INIT] Routing through Tor nodes...",
            "[INIT] Node 1: 185.220.101.██  OK",
            "[INIT] Node 2: 51.15.███.███   OK",
            "[INIT] Node 3: 198.98.██.███   OK",
            "[CRYPT] AES-256-GCM initialized",
            "[CRYPT] RSA-4096 keypair generated",
            "[CRYPT] Perfect forward secrecy... ACTIVE",
            "",
            "[NET] WebRTC tunnel... READY",
            "[NET] P2P mesh network... ONLINE",
            "[NET] Signal encrypted... ✓",
            "",
            "[WARN] No identity found.",
            "[WARN] Anonymous session required.",
            "[SYS] Choose your handle. No traces.",
            "[SYS] Exit = total wipe. No recovery.",
        )
        for (line in lines) {
            bootLines = bootLines + line
            delay(if (line.isEmpty()) 60 else 80)
        }
        delay(200)
        showInput = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        // Matrix rain background
        MatrixRain(alpha = 0.04f, density = 15)

        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar
            IdeToolbar(title = "DEVTALK :: SECURE INIT")

            // Tab bar
            IdeTabBar(
                tabs = listOf(
                    TabItem("init.sh", icon = Icons.Default.Terminal),
                ),
                selectedIndex = 0,
                onTabSelected = {}
            )

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                // Skull ASCII art
                AnimatedVisibility(
                    visible = showSkull,
                    enter = fadeIn(animationSpec = tween(800))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlitchText(
                            text = """
    ██████╗ ███████╗██╗   ██╗████████╗ █████╗ ██╗     ██╗  ██╗
    ██╔══██╗██╔════╝██║   ██║╚══██╔══╝██╔══██╗██║     ██║ ██╔╝
    ██║  ██║█████╗  ██║   ██║   ██║   ███████║██║     █████╔╝ 
    ██║  ██║██╔══╝  ╚██╗ ██╔╝   ██║   ██╔══██║██║     ██╔═██╗ 
    ██████╔╝███████╗ ╚████╔╝    ██║   ██║  ██║███████╗██║  ██╗
    ╚═════╝ ╚══════╝  ╚═══╝     ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝
                            """.trimIndent(),
                            style = IdeTypography.ascii.copy(
                                color = IdeColors.accentGreen,
                                fontSize = 6.sp
                            ),
                            glitchIntensity = 0.15f
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄",
                            style = IdeTypography.ascii.copy(color = IdeColors.accentGreen.copy(alpha = 0.4f))
                        )
                        Text(
                            text = "ENCRYPTED  ·  ANONYMOUS  ·  EPHEMERAL",
                            style = IdeTypography.codeSmall.copy(
                                color = IdeColors.accentCyan,
                                letterSpacing = 3.sp
                            )
                        )
                    }
                }

                // Boot log lines
                AnimatedVisibility(
                    visible = phase >= 1,
                    enter = fadeIn()
                ) {
                    Column {
                        bootLines.forEachIndexed { index, line ->
                            val color = when {
                                line.startsWith("[WARN]") -> IdeColors.accentYellow
                                line.startsWith("[CRYPT]") -> IdeColors.accentCyan
                                line.contains("OK") || line.contains("✓") ||
                                        line.contains("ACTIVE") || line.contains("READY") ||
                                        line.contains("ONLINE") -> IdeColors.accentGreen
                                line.startsWith("[SYS]") && line.contains("█") -> IdeColors.accentGreen
                                line.startsWith("[SYS]") -> IdeColors.textPrimary
                                line.startsWith("[NET]") -> IdeColors.accentPurple
                                line.startsWith("[INIT]") -> IdeColors.textSecondary
                                else -> IdeColors.textPrimary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                            ) {
                                // Line number gutter
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .background(IdeColors.gutter)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
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
                                        .heightIn(min = 18.dp)
                                        .background(IdeColors.border)
                                )
                                Text(
                                    text = " $line",
                                    style = IdeTypography.codeSmall.copy(color = color),
                                    modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Input section
                AnimatedVisibility(
                    visible = showInput,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        // Prompt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = IdeColors.accentRed)) { append("root") }
                                    withStyle(SpanStyle(color = IdeColors.textPrimary)) { append("@") }
                                    withStyle(SpanStyle(color = IdeColors.accentGreen)) { append("devtalk") }
                                    withStyle(SpanStyle(color = IdeColors.textPrimary)) { append(":") }
                                    withStyle(SpanStyle(color = IdeColors.accentCyan)) { append("~") }
                                    withStyle(SpanStyle(color = IdeColors.textPrimary)) { append("# ") }
                                    withStyle(SpanStyle(color = IdeColors.accentYellow)) { append("set_identity") }
                                },
                                style = IdeTypography.code
                            )
                            BlinkingCursor()
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
                                text = ">>>",
                                style = IdeTypography.code.copy(color = IdeColors.accentGreen)
                            )
                            IdeTextField(
                                value = username,
                                onValueChange = { newVal ->
                                    username = newVal.lowercase().filter {
                                        it.isLetterOrDigit() || it == '_' || it == '-'
                                    }
                                },
                                placeholder = "enter_handle",
                                modifier = Modifier.weight(1f)
                            )
                            IdeButton(
                                text = "EXEC",
                                onClick = { onCreateAccount(username) },
                                icon = Icons.Default.PlayArrow,
                                color = IdeColors.accentGreen,
                                enabled = username.length >= 3 && !isLoading
                            )
                        }

                        // Error
                        error?.let {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "[ERROR] $it",
                                    style = IdeTypography.code.copy(color = IdeColors.accentRed)
                                )
                            }
                        }

                        // Loading
                        if (isLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "[CONN] Establishing secure tunnel...",
                                    style = IdeTypography.code.copy(color = IdeColors.accentCyan)
                                )
                                BlinkingCursor(color = IdeColors.accentCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rules panel
                        HackerPanel(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            borderColor = IdeColors.accentGreen
                        ) {
                            Text(
                                text = "┌─────────────────────────────────────┐",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                            )
                            Text(
                                text = "│  PROTOCOL RULES                     │",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                            )
                            Text(
                                text = "├─────────────────────────────────────┤",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                            )
                            Text(
                                text = "│  ► Handle: 3+ chars (a-z 0-9 _ -)  │",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                            )
                            Text(
                                text = "│  ► Must be unique across network    │",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                            )
                            Text(
                                text = "│  ► No email. No phone. No password. │",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                            )
                            Text(
                                text = "│  ► Session = volatile memory only   │",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.accentYellow)
                            )
                            Text(
                                text = "│  ► EXIT = TOTAL WIPE. NO RECOVERY. │",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.accentRed)
                            )
                            Text(
                                text = "│  ► Share QR / link to connect       │",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary)
                            )
                            Text(
                                text = "└─────────────────────────────────────┘",
                                style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                            )
                        }
                    }
                }
            }

            // Status bar
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(
                        text = "SECURE",
                        icon = Icons.Default.Lock,
                        color = IdeColors.accentGreen
                    ),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(
                        text = if (username.length >= 3) "HANDLE VALID" else "MIN 3 CHARS",
                        color = if (username.length >= 3) IdeColors.accentGreen else IdeColors.accentRed
                    ),
                    StatusBarItem(
                        text = "E2E",
                        color = IdeColors.accentCyan
                    ),
                    StatusBarItem(
                        text = "AES-256",
                        color = IdeColors.textComment
                    )
                )
            )
        }

        // CRT scanline overlay
        CrtOverlay()
    }
}

private val sp = androidx.compose.ui.unit.sp
