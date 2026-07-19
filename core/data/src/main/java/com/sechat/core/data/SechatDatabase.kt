package com.sechat.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sechat.core.data.dao.ContactDao
import com.sechat.core.data.dao.MessageDao

@Database(
    entities = [ContactEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SechatDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao

    companion object {
        private const val DB_NAME = "sechat.db"

        fun create(context: Context): SechatDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SechatDatabase::class.java,
                DB_NAME
            ).build()
        }
    }
}
