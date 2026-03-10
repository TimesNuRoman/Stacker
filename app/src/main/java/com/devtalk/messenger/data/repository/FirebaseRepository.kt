package com.devtalk.messenger.data.repository

import com.devtalk.messenger.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseRepository @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val usersRef = db.getReference("users")
    private val chatsRef = db.getReference("chats")
    private val messagesRef = db.getReference("messages")
    private val contactsRef = db.getReference("contacts")
    private val callsRef = db.getReference("calls")

    // ===== AUTH =====
    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: throw Exception("Auth failed")
    }

    fun getCurrentUid(): String? = auth.currentUser?.uid

    // ===== USERS =====
    suspend fun isUsernameAvailable(username: String): Boolean {
        val snapshot = usersRef.orderByChild("username")
            .equalTo(username)
            .get()
            .await()
        return !snapshot.exists()
    }

    suspend fun createUser(user: User) {
        usersRef.child(user.uid).setValue(user.toMap()).await()
    }

    suspend fun getUser(uid: String): User? {
        val snapshot = usersRef.child(uid).get().await()
        @Suppress("UNCHECKED_CAST")
        return snapshot.value?.let { User.fromMap(it as Map<String, Any?>) }
    }

    suspend fun getUserByUsername(username: String): User? {
        val snapshot = usersRef.orderByChild("username")
            .equalTo(username)
            .get()
            .await()
        if (!snapshot.exists()) return null
        val first = snapshot.children.firstOrNull() ?: return null
        @Suppress("UNCHECKED_CAST")
        return User.fromMap(first.value as Map<String, Any?>)
    }

    suspend fun searchUsersByPrefix(prefix: String, limit: Int = 20): List<User> {
        if (prefix.length < 2) return emptyList()
        val endStr = prefix + "\uf8ff"
        val snapshot = usersRef.orderByChild("username")
            .startAt(prefix)
            .endAt(endStr)
            .limitToFirst(limit)
            .get()
            .await()
        return snapshot.children.mapNotNull { child ->
            @Suppress("UNCHECKED_CAST")
            (child.value as? Map<String, Any?>)?.let { User.fromMap(it) }
        }
    }

    suspend fun getOnlineUsers(limit: Int = 50): List<User> {
        val snapshot = usersRef.orderByChild("status")
            .equalTo(UserStatus.ONLINE.name)
            .limitToFirst(limit)
            .get()
            .await()
        return snapshot.children.mapNotNull { child ->
            @Suppress("UNCHECKED_CAST")
            (child.value as? Map<String, Any?>)?.let { User.fromMap(it) }
        }
    }

    suspend fun getRecentUsers(limit: Int = 20): List<User> {
        val snapshot = usersRef.orderByChild("createdAt")
            .limitToLast(limit)
            .get()
            .await()
        return snapshot.children.mapNotNull { child ->
            @Suppress("UNCHECKED_CAST")
            (child.value as? Map<String, Any?>)?.let { User.fromMap(it) }
        }.sortedByDescending { it.createdAt }
    }

    suspend fun updateUserStatus(uid: String, status: UserStatus) {
        usersRef.child(uid).child("status").setValue(status.name).await()
        usersRef.child(uid).child("lastSeen").setValue(System.currentTimeMillis()).await()
    }

    suspend fun deleteUser(uid: String) {
        // Delete contacts
        contactsRef.child(uid).removeValue().await()
        // Delete user data
        usersRef.child(uid).removeValue().await()
        // Delete auth
        auth.currentUser?.delete()?.await()
    }

    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val listener = usersRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                @Suppress("UNCHECKED_CAST")
                val user = snapshot.value?.let { User.fromMap(it as Map<String, Any?>) }
                trySend(user)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        })
        awaitClose { usersRef.child(uid).removeEventListener(listener) }
    }

    // ===== CONTACTS =====
    suspend fun addContact(myUid: String, contact: Contact) {
        contactsRef.child(myUid).child(contact.uid).setValue(contact.toMap()).await()
    }

    suspend fun removeContact(myUid: String, contactUid: String) {
        contactsRef.child(myUid).child(contactUid).removeValue().await()
    }

    fun observeContacts(uid: String): Flow<List<Contact>> = callbackFlow {
        val listener = contactsRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contacts = snapshot.children.mapNotNull { child ->
                    @Suppress("UNCHECKED_CAST")
                    (child.value as? Map<String, Any?>)?.let { Contact.fromMap(it) }
                }
                trySend(contacts)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        })
        awaitClose { contactsRef.child(uid).removeEventListener(listener) }
    }

    // ===== CHATS =====
    suspend fun createChat(user1: User, user2: User): String {
        val chatId = generateChatId(user1.uid, user2.uid)
        val chat = Chat(
            id = chatId,
            participants = listOf(user1.uid, user2.uid),
            participantNames = mapOf(user1.uid to user1.username, user2.uid to user2.username),
            createdAt = System.currentTimeMillis()
        )
        chatsRef.child(chatId).setValue(chat.toMap()).await()
        return chatId
    }

    suspend fun getChat(chatId: String): Chat? {
        val snapshot = chatsRef.child(chatId).get().await()
        @Suppress("UNCHECKED_CAST")
        return snapshot.value?.let { Chat.fromMap(it as Map<String, Any?>) }
    }

    fun observeChats(uid: String): Flow<List<Chat>> = callbackFlow {
        val listener = chatsRef.orderByChild("lastMessageTime")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chats = snapshot.children.mapNotNull { child ->
                        @Suppress("UNCHECKED_CAST")
                        (child.value as? Map<String, Any?>)?.let { Chat.fromMap(it) }
                    }.filter { uid in it.participants }
                        .sortedByDescending { it.lastMessageTime }
                    trySend(chats)
                }
                override fun onCancelled(error: DatabaseError) { close(error.toException()) }
            })
        awaitClose { chatsRef.removeEventListener(listener) }
    }

    // ===== MESSAGES =====
    suspend fun sendMessage(message: Message) {
        messagesRef.child(message.chatId).child(message.id).setValue(message.toMap()).await()
        // Update chat last message
        chatsRef.child(message.chatId).updateChildren(mapOf(
            "lastMessage" to message.content,
            "lastMessageTime" to message.timestamp,
            "lastMessageSender" to message.senderName
        )).await()
    }

    fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = messagesRef.child(chatId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull { child ->
                        @Suppress("UNCHECKED_CAST")
                        (child.value as? Map<String, Any?>)?.let { Message.fromMap(it) }
                    }.sortedBy { it.timestamp }
                    trySend(messages)
                }
                override fun onCancelled(error: DatabaseError) { close(error.toException()) }
            })
        awaitClose { messagesRef.child(chatId).removeEventListener(listener) }
    }

    // ===== CALLS =====
    suspend fun createCall(call: CallSignal) {
        callsRef.child(call.id).setValue(call.toMap()).await()
    }

    suspend fun updateCallStatus(callId: String, status: CallStatus) {
        callsRef.child(callId).child("status").setValue(status.name).await()
    }

    suspend fun updateCallAnswer(callId: String, answer: String) {
        callsRef.child(callId).child("answer").setValue(answer).await()
        callsRef.child(callId).child("status").setValue(CallStatus.ACCEPTED.name).await()
    }

    suspend fun addIceCandidate(callId: String, candidate: String, isCallerSide: Boolean) {
        val path = if (isCallerSide) "callerCandidates" else "calleeCandidates"
        callsRef.child(callId).child(path).push().setValue(candidate).await()
    }

    fun observeCall(callId: String): Flow<CallSignal?> = callbackFlow {
        val listener = callsRef.child(callId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                @Suppress("UNCHECKED_CAST")
                val call = snapshot.value?.let { CallSignal.fromMap(it as Map<String, Any?>) }
                trySend(call)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        })
        awaitClose { callsRef.child(callId).removeEventListener(listener) }
    }

    fun observeIncomingCalls(uid: String): Flow<CallSignal?> = callbackFlow {
        val listener = callsRef.orderByChild("calleeId").equalTo(uid)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    @Suppress("UNCHECKED_CAST")
                    val call = (snapshot.value as? Map<String, Any?>)?.let { CallSignal.fromMap(it) }
                    if (call?.status == CallStatus.RINGING) {
                        trySend(call)
                    }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) { close(error.toException()) }
            })
        awaitClose { callsRef.removeEventListener(listener) }
    }

    suspend fun deleteCall(callId: String) {
        callsRef.child(callId).removeValue().await()
    }

    // ===== UTILS =====
    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    fun generateMessageId(): String = UUID.randomUUID().toString()
    fun generateCallId(): String = UUID.randomUUID().toString()
}
