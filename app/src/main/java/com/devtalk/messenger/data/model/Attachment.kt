package com.devtalk.messenger.data.model

data class Attachment(
    val id: String = "",
    val type: AttachmentType = AttachmentType.FILE,
    val fileName: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val duration: Long = 0,
    val localUri: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "type" to type.name,
        "fileName" to fileName,
        "fileSize" to fileSize,
        "mimeType" to mimeType,
        "url" to url,
        "thumbnailUrl" to thumbnailUrl,
        "width" to width,
        "height" to height,
        "duration" to duration
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Attachment = Attachment(
            id = map["id"] as? String ?: "",
            type = try {
                AttachmentType.valueOf(map["type"] as? String ?: "FILE")
            } catch (_: Exception) { AttachmentType.FILE },
            fileName = map["fileName"] as? String ?: "",
            fileSize = (map["fileSize"] as? Number)?.toLong() ?: 0,
            mimeType = map["mimeType"] as? String ?: "",
            url = map["url"] as? String ?: "",
            thumbnailUrl = map["thumbnailUrl"] as? String ?: "",
            width = (map["width"] as? Number)?.toInt() ?: 0,
            height = (map["height"] as? Number)?.toInt() ?: 0,
            duration = (map["duration"] as? Number)?.toLong() ?: 0
        )

        fun fromMimeType(mime: String): AttachmentType = when {
            mime.startsWith("image/") -> AttachmentType.IMAGE
            mime.startsWith("video/") -> AttachmentType.VIDEO
            mime.startsWith("audio/") -> AttachmentType.AUDIO
            mime.endsWith("/pdf") -> AttachmentType.DOCUMENT
            mime.contains("text/") || mime.contains("json") ||
                mime.contains("xml") || mime.contains("javascript") ||
                mime.contains("python") || mime.contains("kotlin") ||
                mime.contains("java") || mime.contains("html") ||
                mime.contains("css") || mime.contains("yaml") ||
                mime.contains("markdown") -> AttachmentType.CODE
            mime.contains("zip") || mime.contains("rar") ||
                mime.contains("tar") || mime.contains("gz") ||
                mime.contains("7z") -> AttachmentType.ARCHIVE
            else -> AttachmentType.FILE
        }
    }

    fun getIcon(): String = when (type) {
        AttachmentType.IMAGE -> "🖼"
        AttachmentType.VIDEO -> "🎬"
        AttachmentType.AUDIO -> "🎵"
        AttachmentType.DOCUMENT -> "📄"
        AttachmentType.CODE -> "📝"
        AttachmentType.ARCHIVE -> "📦"
        AttachmentType.FILE -> "📎"
    }

    fun getSizeString(): String {
        return when {
            fileSize < 1024 -> "${fileSize}B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024}KB"
            fileSize < 1024 * 1024 * 1024 -> "${"%.1f".format(fileSize / (1024.0 * 1024.0))}MB"
            else -> "${"%.1f".format(fileSize / (1024.0 * 1024.0 * 1024.0))}GB"
        }
    }

    fun getExtension(): String = fileName.substringAfterLast('.', "")
}

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, CODE, ARCHIVE, FILE;

    fun label(): String = when (this) {
        IMAGE -> "Image"
        VIDEO -> "Video"
        AUDIO -> "Audio"
        DOCUMENT -> "Document"
        CODE -> "Code"
        ARCHIVE -> "Archive"
        FILE -> "File"
    }
}
