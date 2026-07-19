package com.sechat.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "public_key") val publicKey: ByteArray,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "first_seen") val firstSeen: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_seen") val lastSeen: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_online") val isOnline: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContactEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
