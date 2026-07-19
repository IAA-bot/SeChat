package com.sechat.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sechat.core.data.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY last_seen DESC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE public_key = :publicKey")
    suspend fun getByPublicKey(publicKey: ByteArray): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Query("UPDATE contacts SET is_online = :isOnline WHERE id = :id")
    suspend fun setOnlineStatus(
        id: Long,
        isOnline: Boolean,
    )

    @Query("UPDATE contacts SET last_seen = :lastSeen WHERE id = :id")
    suspend fun updateLastSeen(
        id: Long,
        lastSeen: Long,
    )

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun delete(id: Long)
}
