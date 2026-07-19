package com.sechat.core.data.database.model

data class Message(
    val id: Long = 0,
    val senderPublicKey: ByteArray,
    val recipientPublicKey: ByteArray,
    val ciphertext: ByteArray,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isEphemeral: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false
        return id == other.id && timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
