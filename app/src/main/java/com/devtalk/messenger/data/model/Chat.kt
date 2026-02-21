package com.devtalk.messenger.data.model

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSender: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "participants" to participants,
        "participantNames" to participantNames,
        "lastMessage" to lastMessage,
        "lastMessageTime" to lastMessageTime,
        "lastMessageSender" to lastMessageSender,
        "unreadCount" to unreadCount,
        "createdAt" to createdAt
    )

    fun getOtherParticipant(myUid: String): String =
        participants.firstOrNull { it != myUid } ?: ""

    fun getOtherName(myUid: String): String =
        participantNames[getOtherParticipant(myUid)] ?: "unknown"

    fun getTabTitle(myUid: String): String =
        "${getOtherName(myUid)}.chat"

    companion object {
        fun fromMap(map: Map<String, Any?>): Chat = Chat(
            id = map["id"] as? String ?: "",
            participants = (map["participants"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            participantNames = (map["participantNames"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                (k as? String)?.let { key -> (v as? String)?.let { value -> key to value } }
            }?.toMap() ?: emptyMap(),
            lastMessage = map["lastMessage"] as? String ?: "",
            lastMessageTime = (map["lastMessageTime"] as? Number)?.toLong() ?: 0L,
            lastMessageSender = map["lastMessageSender"] as? String ?: "",
            unreadCount = (map["unreadCount"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                (k as? String)?.let { key -> (v as? Number)?.toInt()?.let { value -> key to value } }
            }?.toMap() ?: emptyMap(),
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
