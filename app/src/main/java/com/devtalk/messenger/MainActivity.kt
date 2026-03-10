package com.devtalk.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

                // Handle deep links
                LaunchedEffect(Unit) {
                    handleDeepLink(intent, viewModel)
                }

                DevTalkNavHost(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleDeepLink(intent: android.content.Intent?, viewModel: MainViewModel) {
        val uri = intent?.data ?: return
        val username = when {
            // devtalk://profile/username
            uri.scheme == "devtalk" && uri.host == "profile" ->
                uri.pathSegments?.firstOrNull()
            // https://devtalk.app/u/username
            uri.host == "devtalk.app" && uri.pathSegments?.firstOrNull() == "u" ->
                uri.pathSegments?.getOrNull(1)
            else -> null
        }
        username?.let {
            viewModel.addContactByUsername(it)
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
    val searchResults by viewModel.searchResults.collectAsState()
    val onlineUsers by viewModel.onlineUsers.collectAsState()
    val recentUsers by viewModel.recentUsers.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val publicBots by viewModel.publicBots.collectAsState()
    val myBots by viewModel.myBots.collectAsState()
    val selectedBot by viewModel.selectedBot.collectAsState()
    val editingBot by viewModel.editingBot.collectAsState()
    val isBotLoading by viewModel.isBotLoading.collectAsState()
    val wallPosts by viewModel.wallPosts.collectAsState()
    val wallComments by viewModel.wallComments.collectAsState()
    val viewingUser by viewModel.viewingUser.collectAsState()
    val viewingWallPosts by viewModel.viewingWallPosts.collectAsState()
    val viewingWallComments by viewModel.viewingWallComments.collectAsState()

    val pendingAttachments by viewModel.pendingAttachments.collectAsState()
    val uploadProgressMap by viewModel.uploadProgress.collectAsState()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val chatId = viewModel.selectedChatId.value ?: return@rememberLauncherForActivityResult
        uris.forEach { uri -> viewModel.uploadAndAttachFile(chatId, uri) }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

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
                        onOpenSearch = { viewModel.openSearch() },
                        onOpenInvite = { viewModel.openInvite() },
                        onOpenBots = { viewModel.openBotCatalog() },
                        onStartCall = { uid, type -> viewModel.startCall(uid, type) },
                        onLogout = { showLogoutDialog = true },
                        onContactClick = { viewModel.onContactClick(it) },
                        incomingCall = incomingCall,
                        onAcceptCall = { viewModel.acceptCall() },
                        onRejectCall = { viewModel.rejectCall() },
                        pendingAttachments = pendingAttachments,
                        uploadProgress = uploadProgressMap,
                        onAttachClick = {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        onRemoveAttachment = { viewModel.removePendingAttachment(it) }
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
                        onBioChange = { viewModel.updateBio(it) },
                        onAvatarChange = { emoji, ascii, color -> viewModel.updateAvatar(emoji, ascii, color) },
                        onOpenInvite = { viewModel.openInvite() },
                        wallPosts = wallPosts,
                        wallComments = wallComments,
                        onWallPost = { content, type -> viewModel.postToWall(content, type) },
                        onWallLike = { viewModel.likeWallPost(it) },
                        onWallComment = { post, text -> viewModel.commentOnWallPost(post, text) },
                        onWallDelete = { viewModel.deleteWallPost(it) },
                        onLoadComments = { viewModel.loadWallComments(it) }
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

            is MainViewModel.Screen.Search -> {
                UserSearchScreen(
                    onBack = { viewModel.navigateBack() },
                    onAddUser = { username -> viewModel.addContactByUsername(username) },
                    onOpenScanner = { viewModel.navigateTo(MainViewModel.Screen.QrScanner) },
                    onOpenInvite = { viewModel.openInvite() },
                    searchResults = searchResults,
                    onlineUsers = onlineUsers,
                    recentUsers = recentUsers,
                    onSearchQuery = { viewModel.searchUsers(it) },
                    isSearching = isSearching,
                    currentUid = currentUser?.uid ?: "",
                    existingContactUids = contacts.map { it.uid }.toSet()
                )
            }

            is MainViewModel.Screen.Invite -> {
                InviteScreen(
                    username = currentUser?.username ?: "",
                    onBack = { viewModel.navigateBack() }
                )
            }

            is MainViewModel.Screen.BotCatalog -> {
                BotCatalogScreen(
                    onBack = { viewModel.navigateBack() },
                    onCreateBot = { viewModel.openBotBuilder() },
                    onBotClick = { bot -> viewModel.openBotDetail(bot.id) },
                    onMyBots = {},
                    publicBots = publicBots,
                    myBots = myBots,
                    isLoading = isBotLoading
                )
            }

            is MainViewModel.Screen.BotBuilder -> {
                BotBuilderScreen(
                    onBack = { viewModel.navigateBack() },
                    onSave = { bot -> viewModel.saveBot(bot) }
                )
            }

            is MainViewModel.Screen.BotEdit -> {
                BotBuilderScreen(
                    onBack = { viewModel.navigateBack() },
                    onSave = { bot -> viewModel.saveBot(bot) },
                    existingBot = editingBot
                )
            }

            is MainViewModel.Screen.BotDetail -> {
                selectedBot?.let { bot ->
                    BotDetailScreen(
                        bot = bot,
                        onBack = { viewModel.navigateBack() },
                        onAddToChat = { viewModel.addBotToChat(it) },
                        onEdit = { viewModel.openBotEdit(it) },
                        onDelete = { viewModel.deleteBot(it) },
                        isOwner = bot.creatorUid == (currentUser?.uid ?: "")
                    )
                }
            }

            is MainViewModel.Screen.UserProfile -> {
                val viewUser = viewingUser
                currentUser?.let { me ->
                    viewUser?.let { user ->
                        UserProfileScreen(
                            user = user,
                            currentUser = me,
                            onBack = { viewModel.navigateBack() },
                            onMessage = {
                                val contact = contacts.find { it.uid == user.uid }
                                if (contact != null && contact.chatId.isNotEmpty()) {
                                    viewModel.selectChat(contact.chatId)
                                    viewModel.navigateBack()
                                } else {
                                    viewModel.addContactByUsername(user.username)
                                }
                            },
                            onCall = { type -> viewModel.startCall(user.uid, type) },
                            isContact = contacts.any { it.uid == user.uid },
                            onAddContact = { viewModel.addContactByUsername(user.username) },
                            wallPosts = viewingWallPosts,
                            wallComments = viewingWallComments,
                            onWallPost = { content, type -> viewModel.postToUserWall(user.uid, content, type) },
                            onWallLike = { viewModel.likeWallPost(it) },
                            onWallComment = { post, text -> viewModel.commentOnWallPost(post, text) },
                            onLoadComments = { viewModel.loadWallComments(it) }
                        )
                    }
                }
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
                    LaunchedEffect(Unit) {
                        viewModel.navigateBack()
                    }
                }
            }
        }
    }
}
