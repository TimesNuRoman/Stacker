package com.devtalk.messenger.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object InviteManager {

    private const val APP_LINK_BASE = "https://devtalk.app"
    private const val DEEP_LINK_BASE = "devtalk://profile"
    private const val PLAYSTORE_LINK = "https://play.google.com/store/apps/details?id=com.devtalk.messenger"

    // =============================================
    //  Link generation
    // =============================================
    fun getProfileDeepLink(username: String): String =
        "$DEEP_LINK_BASE/$username"

    fun getProfileWebLink(username: String): String =
        "$APP_LINK_BASE/u/$username"

    fun getAppDownloadLink(): String = PLAYSTORE_LINK

    fun getInviteText(username: String): String = buildString {
        append("Join me on DevTalk — encrypted, anonymous, ephemeral messenger.\n")
        append("No registration. No traces.\n\n")
        append("My handle: $username\n")
        append("Direct link: ${getProfileWebLink(username)}\n\n")
        append("Get the app: $PLAYSTORE_LINK")
    }

    fun getShortInviteText(username: String): String =
        "Add me on DevTalk: ${getProfileWebLink(username)}"

    // =============================================
    //  Sharing intents
    // =============================================
    fun shareText(context: Context, username: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "DevTalk — Anonymous Messenger")
            putExtra(Intent.EXTRA_TEXT, getInviteText(username))
        }
        context.startActivity(Intent.createChooser(intent, "Share DevTalk Profile"))
    }

    fun shareToSpecificApp(context: Context, username: String, packageName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getInviteText(username))
            setPackage(packageName)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareText(context, username)
        }
    }

    fun shareViaWhatsApp(context: Context, username: String) =
        shareToSpecificApp(context, username, "com.whatsapp")

    fun shareViaTelegram(context: Context, username: String) =
        shareToSpecificApp(context, username, "org.telegram.messenger")

    fun shareViaSms(context: Context, username: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", getShortInviteText(username))
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            shareText(context, username)
        }
    }

    fun shareViaEmail(context: Context, username: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, "Join DevTalk — Encrypted Messenger")
            putExtra(Intent.EXTRA_TEXT, getInviteText(username))
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            shareText(context, username)
        }
    }

    fun copyLinkToClipboard(context: Context, username: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("DevTalk Link", getProfileWebLink(username))
        )
        Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
    }

    fun copyHandleToClipboard(context: Context, username: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("DevTalk Handle", username))
        Toast.makeText(context, "Handle copied!", Toast.LENGTH_SHORT).show()
    }

    // =============================================
    //  Shareable profile card image
    // =============================================
    fun generateProfileCardBitmap(
        username: String,
        qrBitmap: Bitmap
    ): Bitmap {
        val width = 600
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply { color = Color.BLACK }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val greenPaint = Paint().apply {
            color = Color.parseColor("#00FF41")
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val dimGreenPaint = Paint().apply {
            color = Color.parseColor("#005500")
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val cyanPaint = Paint().apply {
            color = Color.parseColor("#00FFCC")
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#00FF41")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // Outer border
        canvas.drawRect(10f, 10f, width - 10f, height - 10f, borderPaint)
        canvas.drawRect(14f, 14f, width - 14f, height - 14f, borderPaint.apply {
            color = Color.parseColor("#003300")
        })

        // Header
        greenPaint.textSize = 28f
        greenPaint.isFakeBoldText = true
        canvas.drawText("☠  D E V T A L K  ☠", 120f, 60f, greenPaint)

        dimGreenPaint.textSize = 14f
        canvas.drawText("ENCRYPTED · ANONYMOUS · EPHEMERAL", 115f, 85f, dimGreenPaint)

        // Separator
        greenPaint.textSize = 14f
        canvas.drawText("═".repeat(42), 20f, 110f, greenPaint.apply {
            color = Color.parseColor("#003300")
        })

        // QR code
        val qrSize = 300
        val qrLeft = (width - qrSize) / 2f
        val qrTop = 130f
        val qrRect = RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize)

        // QR border glow
        val glowPaint = Paint().apply {
            color = Color.parseColor("#00FF41")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(qrLeft - 5, qrTop - 5, qrLeft + qrSize + 5, qrTop + qrSize + 5, glowPaint)
        canvas.drawBitmap(qrBitmap, null, qrRect, null)

        // Username
        greenPaint.color = Color.parseColor("#00FF41")
        greenPaint.textSize = 24f
        val handleText = "@ $username"
        val handleWidth = greenPaint.measureText(handleText)
        canvas.drawText(handleText, (width - handleWidth) / 2f, qrTop + qrSize + 50f, greenPaint)

        // Link
        cyanPaint.textSize = 16f
        val linkText = getProfileWebLink(username)
        val linkWidth = cyanPaint.measureText(linkText)
        canvas.drawText(linkText, (width - linkWidth) / 2f, qrTop + qrSize + 85f, cyanPaint)

        // Separator
        dimGreenPaint.textSize = 14f
        canvas.drawText("═".repeat(42), 20f, qrTop + qrSize + 115f, dimGreenPaint)

        // Instructions
        greenPaint.textSize = 15f
        greenPaint.isFakeBoldText = false
        val instructions = listOf(
            "1. Download DevTalk from Play Store",
            "2. Scan this QR or open the link",
            "3. Start encrypted conversation",
            "",
            "No email. No phone. No password.",
            "Exit = total wipe. No traces."
        )
        instructions.forEachIndexed { i, line ->
            val paint = if (line.startsWith("No") || line.startsWith("Exit"))
                Paint().apply {
                    color = Color.parseColor("#FF0040")
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                    textSize = 14f
                }
            else greenPaint

            canvas.drawText(line, 40f, qrTop + qrSize + 145f + i * 28f, paint)
        }

        // Footer
        dimGreenPaint.textSize = 12f
        canvas.drawText("devtalk.app — Messenger for hackers", 140f, height - 25f, dimGreenPaint)

        return bitmap
    }

    fun shareProfileCardImage(context: Context, username: String, qrBitmap: Bitmap) {
        val cardBitmap = generateProfileCardBitmap(username, qrBitmap)
        val file = File(context.cacheDir, "devtalk_profile_$username.png")
        FileOutputStream(file).use { out ->
            cardBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, getInviteText(username))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Profile Card"))
    }
}
