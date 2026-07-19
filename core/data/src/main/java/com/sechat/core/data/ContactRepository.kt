package com.sechat.core.data

import com.sechat.core.data.dao.ContactDao
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun addContact(displayName: String, publicKey: ByteArray): Long {
        val existing = contactDao.getByPublicKey(publicKey)
        if (existing != null) return existing.id

        return contactDao.insert(
            ContactEntity(
                publicKey = publicKey,
                displayName = displayName,
                firstSeen = System.currentTimeMillis()
            )
        )
    }

    suspend fun getContact(id: Long): ContactEntity? = contactDao.getById(id)

    suspend fun setOnline(id: Long, online: Boolean) {
        contactDao.setOnlineStatus(id, online)
        if (online) contactDao.updateLastSeen(id, System.currentTimeMillis())
    }

    suspend fun deleteContact(id: Long) = contactDao.delete(id)
}
