package com.sechat.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SeChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            startKoin {
                androidContext(this@SeChatApplication)
                modules(appModule)
            }
            createNotificationChannel()
        } catch (e: Exception) {
            Log.e("SeChat", "Failed to initialize: ${e.message}", e)
        }
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                CHAT_CHANNEL_ID,
                resources.getString(R.string.chat_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Incoming encrypted chat messages"
            }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHAT_CHANNEL_ID = "chat_messages"
    }
}
