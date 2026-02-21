package com.devtalk.messenger.data.model

data class Contact(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val chatId: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "username" to username,
        "displayName" to displayName,
        "addedAt" to addedAt,
        "chatId" to chatId
    )

    fun toTreeEntry(): String = "├── $username"

    companion object {
        fun fromMap(map: Map<String, Any?>): Contact = Contact(
            uid = map["uid"] as? String ?: "",
            username = map["username"] as? String ?: "",
            displayName = map["displayName"] as? String ?: "",
            addedAt = (map["addedAt"] as? Long) ?: System.currentTimeMillis(),
            chatId = map["chatId"] as? String ?: ""
        )
    }
}
