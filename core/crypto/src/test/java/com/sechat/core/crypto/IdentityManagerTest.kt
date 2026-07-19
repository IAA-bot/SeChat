package com.sechat.core.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdentityManagerTest {

    private val manager = IdentityManager()

    @Test
    fun `generate identity creates key pair`() {
        manager.deleteIdentity()
        val identity = manager.generateIdentity()

        assertNotNull(identity.keyPair.public)
        assertNotNull(identity.keyPair.private)
        assertTrue(identity.publicKeyRaw.isNotEmpty())
        assertTrue(identity.fingerprint.isNotEmpty())
    }

    @Test
    fun `fingerprint is consistent for same key`() {
        manager.deleteIdentity()
        val identity = manager.generateIdentity()

        val fp1 = IdentityManager.fingerprint(identity.publicKeyRaw)
        val fp2 = IdentityManager.fingerprint(identity.publicKeyRaw)

        assertEquals(fp1, fp2)
    }

    @Test
    fun `fingerprint is different for different keys`() {
        manager.deleteIdentity()
        val id1 = manager.generateIdentity()
        val manager2 = IdentityManager()
        val id2 = manager2.generateIdentity()

        val fp1 = IdentityManager.fingerprint(id1.publicKeyRaw)
        val fp2 = IdentityManager.fingerprint(id2.publicKeyRaw)

        assertTrue(fp1 != fp2)
    }

    @Test
    fun `fingerprint display is formatted correctly`() {
        manager.deleteIdentity()
        val identity = manager.generateIdentity()
        val display = IdentityManager.fingerprintDisplay(identity.publicKeyRaw)

        assertTrue(display.contains(" "))
        assertEquals(19, display.length)
    }

    @Test
    fun `verify fingerprint returns true for matching key`() {
        manager.deleteIdentity()
        val identity = manager.generateIdentity()

        val result = manager.verifyFingerprint(identity.publicKeyRaw, identity.fingerprint)
        assertTrue(result)
    }

    @Test
    fun `fingerprint has correct length`() {
        manager.deleteIdentity()
        val identity = manager.generateIdentity()

        assertEquals(IdentityManager.FINGERPRINT_LENGTH * 2, identity.fingerprint.length)
    }
}
