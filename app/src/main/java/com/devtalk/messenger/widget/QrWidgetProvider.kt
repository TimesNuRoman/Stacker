package com.devtalk.messenger.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.devtalk.messenger.MainActivity
import com.devtalk.messenger.R
import com.devtalk.messenger.util.QrCodeUtils

/**
 * Home screen widget that shows the user's QR code for quick sharing.
 * Others can scan it directly from the home screen.
 */
class QrWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("devtalk_widget", Context.MODE_PRIVATE)
            val username = prefs.getString("username", null) ?: return

            val views = RemoteViews(context.packageName, R.layout.widget_qr)

            // Generate QR
            val profileLink = QrCodeUtils.generateProfileLink(username)
            val qrBitmap = QrCodeUtils.generateQrBitmap(
                profileLink, 256,
                fgColor = android.graphics.Color.parseColor("#00FF41"),
                bgColor = android.graphics.Color.parseColor("#000000")
            )

            views.setImageViewBitmap(R.id.widget_qr_image, qrBitmap)
            views.setTextViewText(R.id.widget_username, "@ $username")

            // Click opens the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
