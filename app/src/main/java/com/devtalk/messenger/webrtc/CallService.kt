package com.devtalk.messenger.webrtc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service for handling WebRTC calls.
 * Keeps the call alive when the app is in background.
 */
class CallService : Service() {

    companion object {
        const val CHANNEL_ID = "devtalk_call_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.devtalk.messenger.START_CALL"
        const val ACTION_STOP = "com.devtalk.messenger.STOP_CALL"
        const val EXTRA_CALL_TYPE = "call_type"
        const val EXTRA_REMOTE_NAME = "remote_name"

        fun start(context: Context, callType: String, remoteName: String) {
            val intent = Intent(context, CallService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CALL_TYPE, callType)
                putExtra(EXTRA_REMOTE_NAME, remoteName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "Audio"
                val remoteName = intent.getStringExtra(EXTRA_REMOTE_NAME) ?: "Unknown"
                startForeground(NOTIFICATION_ID, createNotification(callType, remoteName))
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DevTalk Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active call notifications"
                setShowBadge(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(callType: String, remoteName: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DevTalk $callType Call")
            .setContentText("In call with $remoteName")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
    }
}
