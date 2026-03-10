package com.devtalk.messenger.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val status: UserStatus = UserStatus.ONLINE,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val bio: String = "// no bio yet",
    val profileLink: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "username" to username,
        "displayName" to displayName,
        "status" to status.name,
        "createdAt" to createdAt,
        "lastSeen" to lastSeen,
        "bio" to bio,
        "profileLink" to profileLink
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            uid = map["uid"] as? String ?: "",
            username = map["username"] as? String ?: "",
            displayName = map["displayName"] as? String ?: "",
            status = try {
                UserStatus.valueOf(map["status"] as? String ?: "OFFLINE")
            } catch (_: Exception) {
                UserStatus.OFFLINE
            },
            createdAt = (map["createdAt"] as? Long) ?: System.currentTimeMillis(),
            lastSeen = (map["lastSeen"] as? Long) ?: System.currentTimeMillis(),
            bio = map["bio"] as? String ?: "// no bio yet",
            profileLink = map["profileLink"] as? String ?: ""
        )
    }
}

enum class UserStatus {
    ONLINE, AWAY, DO_NOT_DISTURB, OFFLINE;

    fun toDisplayString(): String = when (this) {
        ONLINE -> "● ACTIVE"
        AWAY -> "◐ GHOST"
        DO_NOT_DISTURB -> "○ STEALTH"
        OFFLINE -> "○ DARK"
    }

    fun toIdeString(): String = when (this) {
        ONLINE -> "ONLINE"
        AWAY -> "GHOST MODE"
        DO_NOT_DISTURB -> "STEALTH"
        OFFLINE -> "DARK"
    }
}
