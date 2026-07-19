package com.sechat.core.data

import com.sechat.core.data.dao.MessageDao
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {

    fun getMessages(sessionId: String): Flow<List<MessageEntity>> =
        messageDao.getMessages(sessionId)

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
}
