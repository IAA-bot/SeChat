package com.sechat.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator

class SessionCipherTest {
    private val cipher = SessionCipher()
    private val keyGen = KeyPairGenerator.getInstance("EC").apply { initialize(256) }

    @Test
    fun `encrypt and decrypt roundtrip`() {
        val aliceKeys = keyGen.generateKeyPair()
        val bobKeys = keyGen.generateKeyPair()

        val aliceSession = cipher.createSenderSession(aliceKeys, bobKeys.public)
        val bobSession = cipher.createReceiverSession(bobKeys, aliceKeys.public)

        val plaintext = "Hello Bob!".toByteArray()
        val encrypted = aliceSession.encrypt(plaintext)
        val decrypted = bobSession.decrypt(encrypted.ciphertext, encrypted.iv, encrypted.messageCounter)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `multiple messages increment counter`() {
        val aliceKeys = keyGen.generateKeyPair()
        val bobKeys = keyGen.generateKeyPair()

        val aliceSession = cipher.createSenderSession(aliceKeys, bobKeys.public)
        val bobSession = cipher.createReceiverSession(bobKeys, aliceKeys.public)

        val msg1 = aliceSession.encrypt("First".toByteArray())
        val msg2 = aliceSession.encrypt("Second".toByteArray())

        assertTrue(msg2.messageCounter > msg1.messageCounter)

        val dec1 = bobSession.decrypt(msg1.ciphertext, msg1.iv, msg1.messageCounter)
        val dec2 = bobSession.decrypt(msg2.ciphertext, msg2.iv, msg2.messageCounter)

        assertArrayEquals("First".toByteArray(), dec1)
        assertArrayEquals("Second".toByteArray(), dec2)
    }

    @Test
    fun `different keys produce different ciphertexts`() {
        val aliceKeys = keyGen.generateKeyPair()
        val bobKeys = keyGen.generateKeyPair()
        val eveKeys = keyGen.generateKeyPair()

        val aliceBob = cipher.createSenderSession(aliceKeys, bobKeys.public)
        val aliceEve = cipher.createSenderSession(aliceKeys, eveKeys.public)

        val plaintext = "Secret".toByteArray()
        val forBob = aliceBob.encrypt(plaintext)
        val forEve = aliceEve.encrypt(plaintext)

        assertTrue(forBob.ciphertext.contentEquals(forEve.ciphertext).not())
    }

    @Test
    fun `tampered ciphertext fails decryption`() {
        val aliceKeys = keyGen.generateKeyPair()
        val bobKeys = keyGen.generateKeyPair()

        val aliceSession = cipher.createSenderSession(aliceKeys, bobKeys.public)
        val bobSession = cipher.createReceiverSession(bobKeys, aliceKeys.public)

        val encrypted = aliceSession.encrypt("Test".toByteArray())
        val tampered = encrypted.ciphertext.copyOf().also { it[0] = it[0].inc() }

        var threw = false
        try {
            bobSession.decrypt(tampered, encrypted.iv, encrypted.messageCounter)
        } catch (_: Exception) {
            threw = true
        }
        assertTrue(threw)
    }
}
