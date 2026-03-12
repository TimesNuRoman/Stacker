package com.devtalk.messenger.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.devtalk.messenger.data.model.Attachment
import com.devtalk.messenger.data.model.AttachmentType
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    data class FileInfo(
        val name: String,
        val size: Long,
        val mimeType: String
    )

    fun getFileInfo(uri: Uri): FileInfo {
        var name = "file"
        var size = 0L
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: "file"
                if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
            }
        }
        return FileInfo(name, size, mimeType)
    }

    suspend fun uploadFile(
        chatId: String,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Attachment {
        val info = getFileInfo(uri)
        val fileId = UUID.randomUUID().toString().take(12)
        val ext = info.name.substringAfterLast('.', "bin")
        val path = "chats/$chatId/$fileId.$ext"
        val ref = storageRef.child(path)

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot read file")

        val uploadTask = ref.putStream(inputStream)

        uploadTask.addOnProgressListener { snapshot ->
            val progress = snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount
            onProgress(progress)
        }

        uploadTask.await()
        val downloadUrl = ref.downloadUrl.await().toString()

        val type = Attachment.fromMimeType(info.mimeType)

        return Attachment(
            id = fileId,
            type = type,
            fileName = info.name,
            fileSize = info.size,
            mimeType = info.mimeType,
            url = downloadUrl,
            localUri = uri.toString()
        )
    }

    suspend fun uploadMultiple(
        chatId: String,
        uris: List<Uri>,
        onProgress: (Int, Float) -> Unit = { _, _ -> }
    ): List<Attachment> {
        return uris.mapIndexed { index, uri ->
            uploadFile(chatId, uri) { progress ->
                onProgress(index, progress)
            }
        }
    }

    suspend fun deleteFile(url: String) {
        try {
            storage.getReferenceFromUrl(url).delete().await()
        } catch (_: Exception) {}
    }
}
