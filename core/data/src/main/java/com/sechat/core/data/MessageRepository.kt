package com.sechat.core.data

import com.sechat.core.data.dao.MessageDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class StoredMessage(
    val id: Long,
    val sessionId: String,
    val sender: String,
    val timestamp: Long,
    val isSent: Boolean,
    val isEncrypted: Boolean = true
)

class MessageRepository(private val messageDao: MessageDao) {

    fun allStoredMessages(): Flow<List<StoredMessage>> =
        messageDao.getAllMessages().map { entities ->
            entities.map { it.toStoredMessage() }
        }

    fun getMessages(sessionId: String): Flow<List<StoredMessage>> =
        messageDao.getMessages(sessionId).map { entities ->
            entities.map { it.toStoredMessage() }
        }

    suspend fun saveMessage(
        sessionId: String,
        sender: String,
        ciphertext: ByteArray,
        isSent: Boolean
    ): Long {
        return messageDao.insert(
            MessageEntity(
                sessionId = sessionId,
                sender = sender,
                ciphertext = ciphertext,
                isSent = isSent
            )
        )
    }

    suspend fun markAsRead(sessionId: String) = messageDao.markAsRead(sessionId)
    suspend fun cleanEphemeral() = messageDao.deleteEphemeralMessages()

    private fun MessageEntity.toStoredMessage() = StoredMessage(
        id = id,
        sessionId = sessionId,
        sender = sender,
        timestamp = timestamp,
        isSent = isSent
    )
}
