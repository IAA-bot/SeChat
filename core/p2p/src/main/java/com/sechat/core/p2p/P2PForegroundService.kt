package com.sechat.core.p2p

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat

class P2PForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification(intent)
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(intent: Intent?): Notification {
        val status = intent?.getStringExtra(EXTRA_STATUS) ?: "Connected"
        val channelId = CHAT_CHANNEL_ID

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, channelId)
            .setContentTitle("SeChat")
            .setContentText("P2P $status — receiving encrypted messages")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHAT_CHANNEL_ID = "chat_messages"
        const val EXTRA_STATUS = "connection_status"

        fun start(context: Context, status: String = "Connected") {
            val intent = Intent(context, P2PForegroundService::class.java).apply {
                putExtra(EXTRA_STATUS, status)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun updateStatus(context: Context, status: String) {
            val intent = Intent(context, P2PForegroundService::class.java).apply {
                putExtra(EXTRA_STATUS, status)
            }
            context.startService(intent)
        }
    }
}
