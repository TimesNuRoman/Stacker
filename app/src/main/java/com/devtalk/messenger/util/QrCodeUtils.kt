package com.devtalk.messenger.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeUtils {
    private const val SCHEME = "devtalk"
    private const val HOST = "profile"

    fun generateProfileLink(username: String): String {
        return "$SCHEME://$HOST/$username"
    }

    fun parseProfileLink(link: String): String? {
        if (link.startsWith("$SCHEME://$HOST/")) {
            return link.removePrefix("$SCHEME://$HOST/")
        }
        return null
    }

    fun generateQrBitmap(
        content: String,
        size: Int = 512,
        fgColor: Int = Color.parseColor("#A9B7C6"),  // IDE text color
        bgColor: Int = Color.parseColor("#1E1F22")    // IDE bg color
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bitMatrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) fgColor else bgColor)
            }
        }
        return bitmap
    }
}
