package com.sechat.core.p2p

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

class P2PForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return Notification.Builder(this, CHAT_CHANNEL_ID)
            .setContentTitle("SeChat")
            .setContentText("Connected \u2014 receiving encrypted messages")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHAT_CHANNEL_ID = "chat_messages"
    }
}
