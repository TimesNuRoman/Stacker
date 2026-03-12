package com.devtalk.messenger.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devtalk.messenger.data.model.CallStatus
import com.devtalk.messenger.data.model.CallType
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    callerName: String,
    calleeName: String,
    callType: CallType,
    callStatus: CallStatus,
    isOutgoing: Boolean,
    isMuted: Boolean,
    isCameraOff: Boolean,
    isSpeakerOn: Boolean,
    callDuration: Long,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit
) {
    var dotsCount by remember { mutableIntStateOf(0) }

    // Pulse animation for the ring indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(callStatus) {
        if (callStatus == CallStatus.RINGING) {
            while (true) {
                delay(400)
                dotsCount = (dotsCount + 1) % 4
            }
        }
    }

    val dots = ".".repeat(dotsCount)
    val remoteName = if (isOutgoing) calleeName else callerName

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        // Matrix rain
        MatrixRain(alpha = 0.03f, density = 12)

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
                Text(
                    text = if (callType == CallType.VIDEO) "📹" else "📡",
                    style = IdeTypography.code
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "[ ${callType.name} CHANNEL :: $remoteName ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
            }
            NeonDivider()

            // Tab
            IdeTabBar(
                tabs = listOf(
                    TabItem(
                        "#$remoteName.${callType.name.lowercase()}",
                        icon = if (callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Phone
                    )
                ),
                selectedIndex = 0,
                onTabSelected = {}
            )

            // Call content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Signal visualization
                Box(
                    modifier = Modifier
                        .size((120 * pulseScale).dp)
                        .clip(CircleShape)
                        .drawBehind {
                            // Outer pulse glow
                            drawCircle(
                                color = IdeColors.accentGreen.copy(alpha = pulseAlpha * 0.15f),
                                radius = size.minDimension / 2
                            )
                            // Neon ring
                            drawCircle(
                                color = IdeColors.accentGreen.copy(alpha = pulseAlpha * 0.6f),
                                radius = size.minDimension / 2,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                            )
                            // Inner ring
                            drawCircle(
                                color = IdeColors.accentCyan.copy(alpha = 0.3f),
                                radius = size.minDimension / 3,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (callType == CallType.VIDEO) "📹" else "📡",
                            style = IdeTypography.glitch.copy(fontSize = 32.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Call info panel
                HackerPanel(
                    modifier = Modifier.padding(horizontal = 40.dp),
                    borderColor = when (callStatus) {
                        CallStatus.RINGING -> IdeColors.accentYellow
                        CallStatus.ACCEPTED -> IdeColors.accentGreen
                        else -> IdeColors.accentRed
                    }
                ) {
                    Text(
                        text = "╔════════════════════════════╗",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                    )
                    Text(
                        text = "║  ${callType.name} CHANNEL ACTIVE      ║",
                        style = IdeTypography.codeSmall.copy(
                            color = if (callStatus == CallStatus.ACCEPTED) IdeColors.accentGreen
                            else IdeColors.accentYellow
                        )
                    )
                    Text(
                        text = "╠════════════════════════════╣",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                    )
                    Text(
                        text = "║ TARGET : $remoteName",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                    )

                    val statusText = when (callStatus) {
                        CallStatus.RINGING -> if (isOutgoing) "CALLING$dots" else "INCOMING$dots"
                        CallStatus.ACCEPTED -> "CONNECTED ✓"
                        CallStatus.REJECTED -> "REJECTED ✗"
                        CallStatus.ENDED -> "TERMINATED"
                        CallStatus.BUSY -> "TARGET BUSY"
                    }
                    val statusColor = when (callStatus) {
                        CallStatus.RINGING -> IdeColors.accentYellow
                        CallStatus.ACCEPTED -> IdeColors.accentGreen
                        else -> IdeColors.accentRed
                    }

                    Text(
                        text = "║ STATUS : $statusText",
                        style = IdeTypography.codeSmall.copy(color = statusColor)
                    )

                    if (callStatus == CallStatus.ACCEPTED) {
                        Text(
                            text = "║ TIME   : ${formatDuration(callDuration)}",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.accentPurple)
                        )
                        Text(
                            text = "║ CRYPTO : E2E / AES-256",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                        )
                    }

                    Text(
                        text = "╚════════════════════════════╝",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.border)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Call controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CallControlButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (isMuted) "UNMUTE" else "MUTE",
                        isActive = isMuted,
                        activeColor = IdeColors.accentRed,
                        inactiveColor = IdeColors.accentGreen,
                        onClick = onToggleMute
                    )

                    if (callType == CallType.VIDEO) {
                        CallControlButton(
                            icon = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            label = if (isCameraOff) "CAM ON" else "CAM OFF",
                            isActive = isCameraOff,
                            activeColor = IdeColors.accentRed,
                            inactiveColor = IdeColors.accentCyan,
                            onClick = onToggleCamera
                        )
                    }

                    CallControlButton(
                        icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        label = if (isSpeakerOn) "SPK OFF" else "SPK ON",
                        isActive = isSpeakerOn,
                        activeColor = IdeColors.accentCyan,
                        inactiveColor = IdeColors.textSecondary,
                        onClick = onToggleSpeaker
                    )

                    CallControlButton(
                        icon = Icons.Default.CallEnd,
                        label = "END",
                        isActive = true,
                        activeColor = IdeColors.accentRed,
                        inactiveColor = IdeColors.accentRed,
                        isEndCall = true,
                        onClick = onEndCall
                    )
                }
            }

            // Status bar
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(
                        text = "${callType.name} CHANNEL",
                        icon = if (callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Phone,
                        color = IdeColors.accentGreen
                    ),
                    StatusBarItem(
                        text = callStatus.name,
                        color = when (callStatus) {
                            CallStatus.RINGING -> IdeColors.accentYellow
                            CallStatus.ACCEPTED -> IdeColors.accentGreen
                            else -> IdeColors.accentRed
                        }
                    ),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(
                        text = "E2E",
                        color = IdeColors.accentCyan
                    ),
                    StatusBarItem(
                        text = remoteName,
                        color = IdeColors.accentGreen
                    )
                )
            )
        }

        CrtOverlay()
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color = IdeColors.textSecondary,
    isEndCall: Boolean = false,
    onClick: () -> Unit
) {
    val color = if (isActive || isEndCall) activeColor else inactiveColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(if (isEndCall) 56.dp else 48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.08f))
                .drawBehind {
                    // Neon ring glow
                    drawCircle(
                        color = color.copy(alpha = 0.4f),
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f.dp.toPx())
                    )
                    drawCircle(
                        color = color.copy(alpha = 0.1f),
                        radius = size.minDimension / 2 + 4.dp.toPx()
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = IdeTypography.codeSmall.copy(
                color = color.copy(alpha = 0.8f),
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

private val sp = androidx.compose.ui.unit.sp
