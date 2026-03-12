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
        fgColor: Int = Color.parseColor("#00FF41"),
        bgColor: Int = Color.parseColor("#000000")
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
