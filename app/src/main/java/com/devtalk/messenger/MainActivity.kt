package com.devtalk.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.devtalk.messenger.data.model.CallStatus
import com.devtalk.messenger.data.model.CallType
import com.devtalk.messenger.ui.MainViewModel
import com.devtalk.messenger.ui.screens.*
import com.devtalk.messenger.ui.theme.DevTalkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DevTalkTheme {
                val viewModel: MainViewModel = hiltViewModel()
                DevTalkNavHost(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DevTalkNavHost(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val selectedChatId by viewModel.selectedChatId.collectAsState()
    val incomingCall by viewModel.incomingCall.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isCameraOff by viewModel.isCameraOff.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val callDuration by viewModel.callDuration.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            username = currentUser?.username ?: "",
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    AnimatedContent(
        targetState = currentScreen,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) togetherWith
                    fadeOut(animationSpec = tween(200))
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is MainViewModel.Screen.Welcome -> {
                WelcomeScreen(
                    onCreateAccount = { username -> viewModel.createAccount(username) },
                    isLoading = isLoading,
                    error = error
                )
            }

            is MainViewModel.Screen.Main -> {
                currentUser?.let { user ->
                    MainScreen(
                        currentUser = user,
                        contacts = contacts,
                        chats = chats,
                        messages = messages,
                        selectedChatId = selectedChatId,
                        onSelectChat = { viewModel.selectChat(it) },
                        onCloseChat = { viewModel.closeChat(it) },
                        onSendMessage = { chatId, content -> viewModel.sendMessage(chatId, content) },
                        onOpenProfile = { viewModel.navigateTo(MainViewModel.Screen.Profile) },
                        onOpenQrScanner = { viewModel.navigateTo(MainViewModel.Screen.QrScanner) },
                        onStartCall = { uid, type -> viewModel.startCall(uid, type) },
                        onLogout = { showLogoutDialog = true },
                        onContactClick = { viewModel.onContactClick(it) },
                        incomingCall = incomingCall,
                        onAcceptCall = { viewModel.acceptCall() },
                        onRejectCall = { viewModel.rejectCall() }
                    )
                }
            }

            is MainViewModel.Screen.Profile -> {
                currentUser?.let { user ->
                    ProfileScreen(
                        user = user,
                        onBack = { viewModel.navigateBack() },
                        onLogout = { showLogoutDialog = true },
                        onStatusChange = { viewModel.updateStatus(it) },
                        onBioChange = { viewModel.updateBio(it) }
                    )
                }
            }

            is MainViewModel.Screen.QrScanner -> {
                QrScannerScreen(
                    onBack = { viewModel.navigateBack() },
                    onUsernameScanned = { username -> viewModel.addContactByUsername(username) },
                    onManualAdd = { username -> viewModel.addContactByUsername(username) }
                )
            }

            is MainViewModel.Screen.Call -> {
                val call = callState
                if (call != null) {
                    CallScreen(
                        callerName = call.callerName,
                        calleeName = call.calleeName,
                        callType = call.type,
                        callStatus = call.status,
                        isOutgoing = screen.isOutgoing,
                        isMuted = isMuted,
                        isCameraOff = isCameraOff,
                        isSpeakerOn = isSpeakerOn,
                        callDuration = callDuration,
                        onToggleMute = { viewModel.toggleMute() },
                        onToggleCamera = { viewModel.toggleCamera() },
                        onToggleSpeaker = { viewModel.toggleSpeaker() },
                        onEndCall = { viewModel.endCall() }
                    )
                } else {
                    // Call ended, go back
                    LaunchedEffect(Unit) {
                        viewModel.navigateBack()
                    }
                }
            }
        }
    }
}
