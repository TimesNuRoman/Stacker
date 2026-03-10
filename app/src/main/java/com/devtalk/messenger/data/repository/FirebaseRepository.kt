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
    private val botsRef = db.getReference("bots")
    private val wallRef = db.getReference("wall")
    private val wallCommentsRef = db.getReference("wall_comments")

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

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>) {
        usersRef.child(uid).updateChildren(updates).await()
    }

    suspend fun updateUserAvatar(uid: String, emoji: String, ascii: String, color: String) {
        usersRef.child(uid).updateChildren(mapOf(
            "avatarEmoji" to emoji,
            "avatarAscii" to ascii,
            "avatarColor" to color
        )).await()
    }

    suspend fun updateUserBio(uid: String, bio: String) {
        usersRef.child(uid).child("bio").setValue(bio).await()
    }

    suspend fun deleteUser(uid: String) {
        contactsRef.child(uid).removeValue().await()
        wallRef.child(uid).removeValue().await()
        usersRef.child(uid).removeValue().await()
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

    suspend fun deleteMessage(chatId: String, messageId: String) {
        messagesRef.child(chatId).child(messageId).updateChildren(mapOf(
            "isDeleted" to true,
            "content" to "[message deleted]"
        )).await()
    }

    suspend fun editMessage(chatId: String, messageId: String, newContent: String) {
        messagesRef.child(chatId).child(messageId).updateChildren(mapOf(
            "content" to newContent,
            "isEdited" to true
        )).await()
    }

    suspend fun addReaction(chatId: String, messageId: String, uid: String, emoji: String) {
        messagesRef.child(chatId).child(messageId)
            .child("reactions").child(uid).setValue(emoji).await()
    }

    suspend fun removeReaction(chatId: String, messageId: String, uid: String) {
        messagesRef.child(chatId).child(messageId)
            .child("reactions").child(uid).removeValue().await()
    }

    suspend fun setTyping(chatId: String, uid: String, isTyping: Boolean) {
        chatsRef.child(chatId).child("typing").child(uid).setValue(
            if (isTyping) System.currentTimeMillis() else null
        ).await()
    }

    fun observeTyping(chatId: String): Flow<Map<String, Long>> = callbackFlow {
        val listener = chatsRef.child(chatId).child("typing")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val typing = snapshot.children.mapNotNull { child ->
                        child.key?.let { k ->
                            (child.value as? Long)?.let { v -> k to v }
                        }
                    }.toMap()
                    trySend(typing)
                }
                override fun onCancelled(error: DatabaseError) { close(error.toException()) }
            })
        awaitClose { chatsRef.child(chatId).child("typing").removeEventListener(listener) }
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

    // ===== BOTS =====
    suspend fun createBot(bot: Bot) {
        botsRef.child(bot.id).setValue(bot.toMap()).await()
    }

    suspend fun updateBot(bot: Bot) {
        botsRef.child(bot.id).setValue(bot.toMap()).await()
    }

    suspend fun deleteBot(botId: String) {
        botsRef.child(botId).removeValue().await()
    }

    suspend fun getBot(botId: String): Bot? {
        val snapshot = botsRef.child(botId).get().await()
        @Suppress("UNCHECKED_CAST")
        return snapshot.value?.let { Bot.fromMap(it as Map<String, Any?>) }
    }

    suspend fun getPublicBots(limit: Int = 50): List<Bot> {
        val snapshot = botsRef.orderByChild("isPublic")
            .equalTo(true)
            .limitToFirst(limit)
            .get()
            .await()
        return snapshot.children.mapNotNull { child ->
            @Suppress("UNCHECKED_CAST")
            (child.value as? Map<String, Any?>)?.let { Bot.fromMap(it) }
        }.sortedByDescending { it.addedByCount }
    }

    suspend fun searchBots(query: String): List<Bot> {
        val allPublic = getPublicBots(200)
        val q = query.lowercase()
        return allPublic.filter {
            it.name.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.tags.any { tag -> tag.lowercase().contains(q) }
        }
    }

    suspend fun getMyBots(uid: String): List<Bot> {
        val snapshot = botsRef.orderByChild("creatorUid")
            .equalTo(uid)
            .get()
            .await()
        return snapshot.children.mapNotNull { child ->
            @Suppress("UNCHECKED_CAST")
            (child.value as? Map<String, Any?>)?.let { Bot.fromMap(it) }
        }.sortedByDescending { it.createdAt }
    }

    suspend fun incrementBotUsage(botId: String) {
        botsRef.child(botId).child("usageCount")
            .setValue(ServerValue.increment(1)).await()
    }

    suspend fun incrementBotAdded(botId: String) {
        botsRef.child(botId).child("addedByCount")
            .setValue(ServerValue.increment(1)).await()
    }

    fun generateBotId(): String = "bot_${UUID.randomUUID().toString().take(12)}"

    // ===== WALL =====
    suspend fun createWallPost(post: WallPost) {
        wallRef.child(post.ownerUid).child(post.id).setValue(post.toMap()).await()
    }

    suspend fun deleteWallPost(ownerUid: String, postId: String) {
        wallRef.child(ownerUid).child(postId).removeValue().await()
        wallCommentsRef.child(postId).removeValue().await()
    }

    fun observeWallPosts(ownerUid: String): Flow<List<WallPost>> = callbackFlow {
        val listener = wallRef.child(ownerUid)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = snapshot.children.mapNotNull { child ->
                        @Suppress("UNCHECKED_CAST")
                        (child.value as? Map<String, Any?>)?.let { WallPost.fromMap(it) }
                    }.sortedByDescending { it.timestamp }
                    trySend(posts)
                }
                override fun onCancelled(error: DatabaseError) { close(error.toException()) }
            })
        awaitClose { wallRef.child(ownerUid).removeEventListener(listener) }
    }

    suspend fun toggleWallPostLike(ownerUid: String, postId: String, likerUid: String) {
        val ref = wallRef.child(ownerUid).child(postId).child("likes").child(likerUid)
        val snapshot = ref.get().await()
        val current = snapshot.getValue(Boolean::class.java) ?: false
        ref.setValue(!current).await()
    }

    suspend fun addWallComment(comment: WallComment) {
        wallCommentsRef.child(comment.postId).child(comment.id).setValue(comment.toMap()).await()
        wallRef.child(comment.authorUid).child(comment.postId)
            .child("commentsCount").setValue(ServerValue.increment(1)).await()
    }

    fun observeWallComments(postId: String): Flow<List<WallComment>> = callbackFlow {
        val listener = wallCommentsRef.child(postId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = snapshot.children.mapNotNull { child ->
                        @Suppress("UNCHECKED_CAST")
                        (child.value as? Map<String, Any?>)?.let { WallComment.fromMap(it) }
                    }.sortedBy { it.timestamp }
                    trySend(comments)
                }
                override fun onCancelled(error: DatabaseError) { close(error.toException()) }
            })
        awaitClose { wallCommentsRef.child(postId).removeEventListener(listener) }
    }

    fun generatePostId(): String = "post_${UUID.randomUUID().toString().take(12)}"
    fun generateCommentId(): String = "cmt_${UUID.randomUUID().toString().take(12)}"

    // ===== UTILS =====
    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    fun generateMessageId(): String = UUID.randomUUID().toString()
    fun generateCallId(): String = UUID.randomUUID().toString()
}
