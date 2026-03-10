package com.devtalk.messenger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devtalk.messenger.data.model.*
import com.devtalk.messenger.data.repository.FirebaseRepository
import com.devtalk.messenger.data.repository.UserPreferences
import com.devtalk.messenger.webrtc.WebRtcManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: FirebaseRepository,
    private val preferences: UserPreferences,
    private val webRtcManager: WebRtcManager
) : ViewModel() {

    // === Navigation State ===
    sealed class Screen {
        object Welcome : Screen()
        object Main : Screen()
        object Profile : Screen()
        object QrScanner : Screen()
        object Search : Screen()
        object Invite : Screen()
        data class Call(val callId: String, val isOutgoing: Boolean) : Screen()
    }

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // === User State ===
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // === Contacts ===
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // === Chats ===
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // === Calls ===
    private val _incomingCall = MutableStateFlow<CallSignal?>(null)
    val incomingCall: StateFlow<CallSignal?> = _incomingCall.asStateFlow()

    // === Search ===
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _onlineUsers = MutableStateFlow<List<User>>(emptyList())
    val onlineUsers: StateFlow<List<User>> = _onlineUsers.asStateFlow()

    private val _recentUsers = MutableStateFlow<List<User>>(emptyList())
    val recentUsers: StateFlow<List<User>> = _recentUsers.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val callState = webRtcManager.callState
    val isMuted = webRtcManager.isMuted
    val isCameraOff = webRtcManager.isCameraOff
    val isSpeakerOn = webRtcManager.isSpeakerOn
    val callDuration = webRtcManager.callDuration

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            preferences.uid.first()?.let { uid ->
                try {
                    val user = repository.getUser(uid)
                    if (user != null) {
                        _currentUser.value = user
                        _currentScreen.value = Screen.Main
                        repository.updateUserStatus(uid, UserStatus.ONLINE)
                        startObserving(uid)
                    } else {
                        preferences.clear()
                    }
                } catch (e: Exception) {
                    preferences.clear()
                }
            }
        }
    }

    // === AUTH ===
    fun createAccount(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Check username availability
                if (!repository.isUsernameAvailable(username)) {
                    _error.value = "Username '$username' is already taken"
                    _isLoading.value = false
                    return@launch
                }

                // Sign in anonymously
                val uid = repository.signInAnonymously()

                // Create user
                val user = User(
                    uid = uid,
                    username = username,
                    displayName = username,
                    status = UserStatus.ONLINE,
                    profileLink = "devtalk://profile/$username"
                )
                repository.createUser(user)
                preferences.saveUser(uid, username, username)
                _currentUser.value = user
                _currentScreen.value = Screen.Main

                // Save username for widget
                updateWidgetUsername(username)

                // Initialize WebRTC
                webRtcManager.initialize()

                // Start observing
                startObserving(uid)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create account"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val uid = _currentUser.value?.uid ?: return@launch
                repository.updateUserStatus(uid, UserStatus.OFFLINE)
                repository.deleteUser(uid)
                preferences.clear()
                webRtcManager.release()
                _currentUser.value = null
                _contacts.value = emptyList()
                _chats.value = emptyList()
                _messages.value = emptyMap()
                _selectedChatId.value = null
                _currentScreen.value = Screen.Welcome
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // === NAVIGATION ===
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun navigateBack() {
        _currentScreen.value = Screen.Main
    }

    // === CONTACTS ===
    fun addContactByUsername(username: String) {
        viewModelScope.launch {
            try {
                val myUser = _currentUser.value ?: return@launch

                if (username == myUser.username) {
                    _error.value = "You can't add yourself"
                    return@launch
                }

                val targetUser = repository.getUserByUsername(username)
                if (targetUser == null) {
                    _error.value = "User '$username' not found"
                    return@launch
                }

                // Check if already a contact
                if (_contacts.value.any { it.uid == targetUser.uid }) {
                    _error.value = "Already in contacts"
                    return@launch
                }

                // Create chat
                val chatId = repository.createChat(myUser, targetUser)

                // Add as contact (both ways)
                val myContact = Contact(
                    uid = targetUser.uid,
                    username = targetUser.username,
                    displayName = targetUser.displayName,
                    chatId = chatId
                )
                val theirContact = Contact(
                    uid = myUser.uid,
                    username = myUser.username,
                    displayName = myUser.displayName,
                    chatId = chatId
                )
                repository.addContact(myUser.uid, myContact)
                repository.addContact(targetUser.uid, theirContact)

                // Send system message
                val msg = Message(
                    id = repository.generateMessageId(),
                    chatId = chatId,
                    senderId = "system",
                    senderName = "system",
                    content = "${myUser.username} connected with ${targetUser.username}",
                    type = MessageType.SYSTEM,
                    timestamp = System.currentTimeMillis()
                )
                repository.sendMessage(msg)

                // Navigate back and open the chat
                _selectedChatId.value = chatId
                _currentScreen.value = Screen.Main

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add contact"
            }
        }
    }

    fun onContactClick(contact: Contact) {
        if (contact.chatId.isNotEmpty()) {
            selectChat(contact.chatId)
        }
    }

    // === CHATS ===
    fun selectChat(chatId: String) {
        _selectedChatId.value = chatId
        // Start observing messages for this chat
        viewModelScope.launch {
            repository.observeMessages(chatId).collect { msgs ->
                _messages.value = _messages.value.toMutableMap().apply {
                    put(chatId, msgs)
                }
            }
        }
    }

    fun closeChat(chatId: String) {
        _messages.value = _messages.value.toMutableMap().apply { remove(chatId) }
        if (_selectedChatId.value == chatId) {
            _selectedChatId.value = _chats.value.firstOrNull { it.id != chatId }?.id
        }
    }

    fun sendMessage(chatId: String, content: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val message = Message(
                id = repository.generateMessageId(),
                chatId = chatId,
                senderId = user.uid,
                senderName = user.username,
                content = content,
                type = MessageType.TEXT,
                timestamp = System.currentTimeMillis()
            )
            repository.sendMessage(message)
        }
    }

    // === CALLS ===
    fun startCall(calleeUid: String, type: CallType) {
        viewModelScope.launch {
            val myUser = _currentUser.value ?: return@launch
            val calleeUser = repository.getUser(calleeUid) ?: return@launch

            val callId = webRtcManager.startCall(
                callerId = myUser.uid,
                callerName = myUser.username,
                calleeId = calleeUser.uid,
                calleeName = calleeUser.username,
                type = type
            )

            _currentScreen.value = Screen.Call(callId, isOutgoing = true)
        }
    }

    fun acceptCall() {
        viewModelScope.launch {
            _incomingCall.value?.let { call ->
                webRtcManager.acceptCall(call)
                _incomingCall.value = null
                _currentScreen.value = Screen.Call(call.id, isOutgoing = false)
            }
        }
    }

    fun rejectCall() {
        viewModelScope.launch {
            _incomingCall.value?.let { call ->
                webRtcManager.rejectCall(call.id)
                _incomingCall.value = null
            }
        }
    }

    fun endCall() {
        viewModelScope.launch {
            webRtcManager.endCall()
            _currentScreen.value = Screen.Main
        }
    }

    fun toggleMute() = webRtcManager.toggleMute()
    fun toggleCamera() = webRtcManager.toggleCamera()
    fun toggleSpeaker() = webRtcManager.toggleSpeaker()

    // === SEARCH & DISCOVERY ===
    fun searchUsers(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = repository.searchUsersByPrefix(query)
                _searchResults.value = results.filter { it.uid != _currentUser.value?.uid }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun loadOnlineUsers() {
        viewModelScope.launch {
            try {
                val users = repository.getOnlineUsers()
                _onlineUsers.value = users.filter { it.uid != _currentUser.value?.uid }
            } catch (_: Exception) {}
        }
    }

    fun loadRecentUsers() {
        viewModelScope.launch {
            try {
                val users = repository.getRecentUsers()
                _recentUsers.value = users.filter { it.uid != _currentUser.value?.uid }
            } catch (_: Exception) {}
        }
    }

    fun openSearch() {
        loadOnlineUsers()
        loadRecentUsers()
        _currentScreen.value = Screen.Search
    }

    fun openInvite() {
        _currentScreen.value = Screen.Invite
    }

    // === PROFILE ===
    fun updateStatus(status: UserStatus) {
        viewModelScope.launch {
            val uid = _currentUser.value?.uid ?: return@launch
            repository.updateUserStatus(uid, status)
            _currentUser.value = _currentUser.value?.copy(status = status)
        }
    }

    fun updateBio(bio: String) {
        viewModelScope.launch {
            // In a complete implementation, save bio to Firebase
            _currentUser.value = _currentUser.value?.copy(bio = bio)
        }
    }

    // === OBSERVERS ===
    private fun startObserving(uid: String) {
        // Observe contacts
        viewModelScope.launch {
            repository.observeContacts(uid).collect { contactList ->
                _contacts.value = contactList
            }
        }

        // Observe chats
        viewModelScope.launch {
            repository.observeChats(uid).collect { chatList ->
                _chats.value = chatList
            }
        }

        // Observe incoming calls
        viewModelScope.launch {
            repository.observeIncomingCalls(uid).collect { call ->
                if (call != null && call.status == CallStatus.RINGING) {
                    _incomingCall.value = call
                }
            }
        }

        // Observe user data
        viewModelScope.launch {
            repository.observeUser(uid).collect { user ->
                user?.let { _currentUser.value = it }
            }
        }
    }

    private fun updateWidgetUsername(username: String) {
        try {
            val prefs = appContext.getSharedPreferences("devtalk_widget", Context.MODE_PRIVATE)
            prefs.edit().putString("username", username).apply()
            val manager = AppWidgetManager.getInstance(appContext)
            val widget = ComponentName(appContext, com.devtalk.messenger.widget.QrWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(widget)
            ids.forEach { id ->
                com.devtalk.messenger.widget.QrWidgetProvider.updateWidget(appContext, manager, id)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            _currentUser.value?.uid?.let {
                repository.updateUserStatus(it, UserStatus.OFFLINE)
            }
        }
    }
}
