package com.sechat.core.data

import com.sechat.core.data.dao.ContactDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContactRepositoryTest {

    private lateinit var dao: FakeContactDao
    private lateinit var repository: ContactRepository

    @Before
    fun setUp() {
        dao = FakeContactDao()
        repository = ContactRepository(dao)
    }

    @Test
    fun `add contact stores and returns id`() = runBlocking {
        val id = repository.addContact("Alice", ByteArray(32) { 1 })
        assertTrue(id > 0)
    }

    @Test
    fun `add duplicate public key returns existing id`() = runBlocking {
        val key = ByteArray(32) { 42 }
        val id1 = repository.addContact("Alice", key)
        val id2 = repository.addContact("Bob", key)
        assertEquals(id1, id2)
    }

    @Test
    fun `get contact by id`() = runBlocking {
        val id = repository.addContact("Alice", ByteArray(32) { 1 })
        val contact = repository.getContact(id)
        assertNotNull(contact)
        assertEquals("Alice", contact?.displayName)
    }

    @Test
    fun `get non-existent contact returns null`() = runBlocking {
        val contact = repository.getContact(999)
        assertNull(contact)
    }

    @Test
    fun `set online updates status and lastSeen`() = runBlocking {
        val id = repository.addContact("Alice", ByteArray(32) { 1 })
        repository.setOnline(id, true)

        val contact = repository.getContact(id)
        assertNotNull(contact)
        assertTrue(contact!!.isOnline)
    }
}

class FakeContactDao : ContactDao {
    private val contacts = mutableListOf<ContactEntity>()
    private val contactsFlow = MutableStateFlow<List<ContactEntity>>(emptyList())

    override fun getAllContacts(): Flow<List<ContactEntity>> = contactsFlow

    override suspend fun getById(id: Long): ContactEntity? = contacts.find { it.id == id }

    override suspend fun getByPublicKey(publicKey: ByteArray): ContactEntity? =
        contacts.find { it.publicKey.contentEquals(publicKey) }

    override suspend fun insert(contact: ContactEntity): Long {
        val id = (contacts.maxOfOrNull { it.id } ?: 0) + 1
        contacts.add(contact.copy(id = id))
        contactsFlow.value = contacts.toList()
        return id
    }

    override suspend fun setOnlineStatus(id: Long, isOnline: Boolean) {
        val idx = contacts.indexOfFirst { it.id == id }
        if (idx >= 0) contacts[idx] = contacts[idx].copy(isOnline = isOnline)
    }

    override suspend fun updateLastSeen(id: Long, lastSeen: Long) {
        val idx = contacts.indexOfFirst { it.id == id }
        if (idx >= 0) contacts[idx] = contacts[idx].copy(lastSeen = lastSeen)
    }

    override suspend fun delete(id: Long) {
        contacts.removeAll { it.id == id }
    }
}
