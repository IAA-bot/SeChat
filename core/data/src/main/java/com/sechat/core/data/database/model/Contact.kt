package com.sechat.core.data.database.model

data class Contact(
    val id: Long = 0,
    val publicKey: ByteArray,
    val displayName: String,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
