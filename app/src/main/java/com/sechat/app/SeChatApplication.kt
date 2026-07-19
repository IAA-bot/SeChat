package com.sechat.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SeChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SeChatApplication)
            modules(appModule)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHAT_CHANNEL_ID,
            getString(R.string.chat_notification_channel),
            NotificationManager.IMPORTANCE_LOW
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
