package com.devtalk.messenger.data.model

data class WallPost(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorEmoji: String = "👤",
    val ownerUid: String = "",
    val content: String = "",
    val type: WallPostType = WallPostType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Map<String, Boolean> = emptyMap(),
    val commentsCount: Int = 0
) {
    val likeCount: Int get() = likes.count { it.value }

    fun isLikedBy(uid: String): Boolean = likes[uid] == true

    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "authorUid" to authorUid,
        "authorName" to authorName,
        "authorEmoji" to authorEmoji,
        "ownerUid" to ownerUid,
        "content" to content,
        "type" to type.name,
        "timestamp" to timestamp,
        "likes" to likes,
        "commentsCount" to commentsCount
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): WallPost = WallPost(
            id = map["id"] as? String ?: "",
            authorUid = map["authorUid"] as? String ?: "",
            authorName = map["authorName"] as? String ?: "",
            authorEmoji = map["authorEmoji"] as? String ?: "👤",
            ownerUid = map["ownerUid"] as? String ?: "",
            content = map["content"] as? String ?: "",
            type = try {
                WallPostType.valueOf(map["type"] as? String ?: "TEXT")
            } catch (_: Exception) { WallPostType.TEXT },
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            likes = (map["likes"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                (k as? String)?.let { key -> (v as? Boolean)?.let { value -> key to value } }
            }?.toMap() ?: emptyMap(),
            commentsCount = (map["commentsCount"] as? Number)?.toInt() ?: 0
        )
    }
}

data class WallComment(
    val id: String = "",
    val postId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorEmoji: String = "👤",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "postId" to postId,
        "authorUid" to authorUid,
        "authorName" to authorName,
        "authorEmoji" to authorEmoji,
        "content" to content,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): WallComment = WallComment(
            id = map["id"] as? String ?: "",
            postId = map["postId"] as? String ?: "",
            authorUid = map["authorUid"] as? String ?: "",
            authorName = map["authorName"] as? String ?: "",
            authorEmoji = map["authorEmoji"] as? String ?: "👤",
            content = map["content"] as? String ?: "",
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

enum class WallPostType {
    TEXT, CODE, ASCII_ART
}
