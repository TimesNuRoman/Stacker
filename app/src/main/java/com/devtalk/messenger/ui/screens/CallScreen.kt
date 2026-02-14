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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
    var dotsCount by remember { mutableStateOf(0) }

    // Animated dots for connecting
    LaunchedEffect(callStatus) {
        if (callStatus == CallStatus.RINGING) {
            while (true) {
                delay(500)
                dotsCount = (dotsCount + 1) % 4
            }
        }
    }

    val dots = ".".repeat(dotsCount)
    val remoteName = if (isOutgoing) calleeName else callerName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(IdeColors.bgToolbar)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Phone,
                contentDescription = null,
                tint = IdeColors.accentGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DevTalk — ${callType.name} Call",
                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary),
                modifier = Modifier.weight(1f)
            )
        }
        Divider(color = IdeColors.border, thickness = 1.dp)

        // Tab bar
        IdeTabBar(
            tabs = listOf(
                TabItem(
                    "$remoteName.call",
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
                .fillMaxWidth()
                .background(IdeColors.bgEditor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Terminal-style call info
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(IdeColors.bgSecondary)
                    .border(1.dp, IdeColors.border, RoundedCornerShape(3.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ASCII art phone/video icon
                if (callType == CallType.VIDEO) {
                    Text(
                        text = """
                            ╔═══════════════╗
                            ║   📹 VIDEO    ║
                            ║               ║
                            ║    ┌─────┐    ║
                            ║    │ 👤  │    ║
                            ║    └─────┘    ║
                            ║               ║
                            ╚═══════════════╝
                        """.trimIndent(),
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentBlue)
                    )
                } else {
                    Text(
                        text = """
                            ╔═══════════════╗
                            ║   📞 AUDIO    ║
                            ║               ║
                            ║    ╭─────╮    ║
                            ║    │ 🎙️  │    ║
                            ║    ╰─────╯    ║
                            ║               ║
                            ╚═══════════════╝
                        """.trimIndent(),
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Call code representation
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = IdeColors.textKeyword)) { append("fun ") }
                        withStyle(SpanStyle(color = IdeColors.textFunction)) { append("call") }
                        withStyle(SpanStyle(color = IdeColors.textPrimary)) { append("(") }
                        withStyle(SpanStyle(color = IdeColors.textString)) { append("\"$remoteName\"") }
                        withStyle(SpanStyle(color = IdeColors.textPrimary)) { append(") {") }
                    },
                    style = IdeTypography.codeLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status
                val statusText = when (callStatus) {
                    CallStatus.RINGING -> if (isOutgoing) "    // Calling$dots" else "    // Incoming$dots"
                    CallStatus.ACCEPTED -> "    // Connected ✓"
                    CallStatus.REJECTED -> "    // Call rejected ✗"
                    CallStatus.ENDED -> "    // Call ended"
                    CallStatus.BUSY -> "    // User busy"
                }
                val statusColor = when (callStatus) {
                    CallStatus.RINGING -> IdeColors.accentYellow
                    CallStatus.ACCEPTED -> IdeColors.accentGreen
                    CallStatus.REJECTED, CallStatus.ENDED -> IdeColors.accentRed
                    CallStatus.BUSY -> IdeColors.accentOrange
                }

                Text(
                    text = statusText,
                    style = IdeTypography.code.copy(color = statusColor)
                )

                if (callStatus == CallStatus.ACCEPTED) {
                    Text(
                        text = "    duration = ${formatDuration(callDuration)}",
                        style = IdeTypography.code.copy(color = IdeColors.textNumber)
                    )
                }

                Text(
                    text = "}",
                    style = IdeTypography.codeLarge.copy(color = IdeColors.textKeyword)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Call controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute button
                CallControlButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (isMuted) "unmute()" else "mute()",
                    isActive = isMuted,
                    activeColor = IdeColors.accentRed,
                    onClick = onToggleMute
                )

                // Camera toggle (video call only)
                if (callType == CallType.VIDEO) {
                    CallControlButton(
                        icon = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        label = if (isCameraOff) "camOn()" else "camOff()",
                        isActive = isCameraOff,
                        activeColor = IdeColors.accentRed,
                        onClick = onToggleCamera
                    )
                }

                // Speaker
                CallControlButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    label = if (isSpeakerOn) "speakerOff()" else "speakerOn()",
                    isActive = isSpeakerOn,
                    activeColor = IdeColors.accentBlue,
                    onClick = onToggleSpeaker
                )

                // End call
                CallControlButton(
                    icon = Icons.Default.CallEnd,
                    label = "endCall()",
                    isActive = true,
                    activeColor = IdeColors.accentRed,
                    isEndCall = true,
                    onClick = onEndCall
                )
            }
        }

        // Status bar
        Divider(color = IdeColors.border, thickness = 1.dp)
        IdeStatusBar(
            items = listOf(
                StatusBarItem(
                    text = "${callType.name.lowercase()} call",
                    icon = if (callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Phone,
                    color = IdeColors.accentGreen
                ),
                StatusBarItem(
                    text = callStatus.name.lowercase(),
                    color = when (callStatus) {
                        CallStatus.RINGING -> IdeColors.accentYellow
                        CallStatus.ACCEPTED -> IdeColors.accentGreen
                        else -> IdeColors.accentRed
                    }
                ),
                StatusBarItem(text = "", fillWeight = true),
                StatusBarItem(
                    text = remoteName,
                    color = IdeColors.textPrimary
                )
            )
        )
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    isEndCall: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(if (isEndCall) 56.dp else 48.dp)
                .clip(CircleShape)
                .background(
                    if (isActive || isEndCall) activeColor.copy(alpha = 0.2f) else IdeColors.bgInput
                )
                .border(
                    1.dp,
                    if (isActive || isEndCall) activeColor else IdeColors.border,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive || isEndCall) activeColor else IdeColors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = IdeTypography.codeSmall.copy(
                color = if (isActive || isEndCall) activeColor else IdeColors.textComment,
                fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
