package com.sechat.core.crypto

import java.security.KeyFactory
import java.security.KeyPair
import java.security.MessageDigest
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class SessionCipher {

    data class Session(
        val sharedSecret: ByteArray,
        val localKeyPair: KeyPair,
        val remotePublicKey: PublicKey
    ) {
        private var sendCounter: Long = 0
        private var recvCounter: Long = 0

        fun encrypt(plaintext: ByteArray): CipherText {
            val key = deriveKey(sharedSecret, sendCounter)
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
            val iv = ByteArray(GCM_IV_LENGTH).also {
                java.security.SecureRandom().nextBytes(it)
            }
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
            val ciphertext = cipher.doFinal(plaintext)
            sendCounter++
            return CipherText(ciphertext, iv, sendCounter - 1)
        }

        fun decrypt(ciphertext: ByteArray, iv: ByteArray, counter: Long): ByteArray {
            val key = deriveKey(sharedSecret, counter)
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
            val plaintext = cipher.doFinal(ciphertext)
            recvCounter = maxOf(recvCounter, counter + 1)
            return plaintext
        }
    }

    data class CipherText(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val messageCounter: Long
    )

    fun createSenderSession(localKeyPair: KeyPair, remotePublicKey: PublicKey): Session {
        val sharedSecret = ecdh(localKeyPair, remotePublicKey)
        return Session(sharedSecret, localKeyPair, remotePublicKey)
    }

    fun createReceiverSession(localKeyPair: KeyPair, remotePublicKey: PublicKey): Session {
        val sharedSecret = ecdh(localKeyPair, remotePublicKey)
        return Session(sharedSecret, localKeyPair, remotePublicKey)
    }

    private fun ecdh(keyPair: KeyPair, publicKey: PublicKey): ByteArray {
        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(keyPair.private)
        ka.doPhase(publicKey, true)
        return ka.generateSecret()
    }

    companion object {
        const val AES_GCM_NO_PADDING = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_LENGTH = 16

        fun deriveKey(sharedSecret: ByteArray, counter: Long): ByteArray {
            val input = sharedSecret + longToBytes(counter)
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(input).copyOf(16)
        }

        private fun longToBytes(value: Long): ByteArray {
            return byteArrayOf(
                (value shr 56).toByte(), (value shr 48).toByte(),
                (value shr 40).toByte(), (value shr 32).toByte(),
                (value shr 24).toByte(), (value shr 16).toByte(),
                (value shr 8).toByte(), value.toByte()
            )
        }
    }
}
