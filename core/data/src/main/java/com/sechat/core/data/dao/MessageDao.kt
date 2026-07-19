package com.sechat.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sechat.core.data.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getMessages(sessionId: String): Flow<List<MessageEntity>>

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Query("UPDATE messages SET is_read = 1 WHERE session_id = :sessionId AND is_read = 0")
    suspend fun markAsRead(sessionId: String)

    @Query("DELETE FROM messages WHERE is_ephemeral = 1")
    suspend fun deleteEphemeralMessages()

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: Long)
}
