package com.devtalk.messenger.data.model

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderEmoji: String = "👤",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val replyToId: String = "",
    val replyToName: String = "",
    val replyToPreview: String = "",
    val reactions: Map<String, String> = emptyMap(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "chatId" to chatId,
        "senderId" to senderId,
        "senderName" to senderName,
        "senderEmoji" to senderEmoji,
        "content" to content,
        "type" to type.name,
        "timestamp" to timestamp,
        "isRead" to isRead,
        "replyToId" to replyToId,
        "replyToName" to replyToName,
        "replyToPreview" to replyToPreview,
        "reactions" to reactions,
        "isEdited" to isEdited,
        "isDeleted" to isDeleted
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Message = Message(
            id = map["id"] as? String ?: "",
            chatId = map["chatId"] as? String ?: "",
            senderId = map["senderId"] as? String ?: "",
            senderName = map["senderName"] as? String ?: "",
            senderEmoji = map["senderEmoji"] as? String ?: "👤",
            content = map["content"] as? String ?: "",
            type = try {
                MessageType.valueOf(map["type"] as? String ?: "TEXT")
            } catch (_: Exception) {
                MessageType.TEXT
            },
            timestamp = (map["timestamp"] as? Long) ?: System.currentTimeMillis(),
            isRead = map["isRead"] as? Boolean ?: false,
            replyToId = map["replyToId"] as? String ?: "",
            replyToName = map["replyToName"] as? String ?: "",
            replyToPreview = map["replyToPreview"] as? String ?: "",
            reactions = (map["reactions"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                (k as? String)?.let { key -> (v as? String)?.let { value -> key to value } }
            }?.toMap() ?: emptyMap(),
            isEdited = map["isEdited"] as? Boolean ?: false,
            isDeleted = map["isDeleted"] as? Boolean ?: false
        )
    }

    fun toTerminalLine(): String {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
        return "[$time] $senderName@devtalk:~\$ $content"
    }
}

enum class MessageType {
    TEXT, CODE, IMAGE, SYSTEM, CALL_AUDIO, CALL_VIDEO
}
