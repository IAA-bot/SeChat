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

    fun createSenderSession(localKeyPair: KeyPair, remotePublicKey: PublicKey): Session {
        val sharedSecret = ecdh(localKeyPair, remotePublicKey)
        val chainKey = hkdf(sharedSecret, "SeChat.SenderChain")
        return Session(chainKey, localKeyPair, remotePublicKey)
    }

    fun createReceiverSession(localKeyPair: KeyPair, remotePublicKey: PublicKey): Session {
        val sharedSecret = ecdh(localKeyPair, remotePublicKey)
        val chainKey = hkdf(sharedSecret, "SeChat.ReceiverChain")
        return Session(chainKey, localKeyPair, remotePublicKey)
    }

    data class Session(
        val chainKey: ByteArray,
        val localKeyPair: KeyPair,
        val remotePublicKey: PublicKey
    ) {
        fun encrypt(plaintext: ByteArray): EncryptedMessage {
            val messageKey = hkdf(chainKey, "SeChat.MessageKey")
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
            val iv = ByteArray(GCM_IV_LENGTH).also {
                java.security.SecureRandom().nextBytes(it)
            }
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(messageKey, "AES"), GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
            val ciphertext = cipher.doFinal(plaintext)
            return EncryptedMessage(iv, ciphertext, chainKey)
        }

        fun decrypt(encrypted: EncryptedMessage): ByteArray {
            val messageKey = hkdf(encrypted.chainKeyUsed, "SeChat.MessageKey")
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(messageKey, "AES"), GCMParameterSpec(GCM_TAG_LENGTH * 8, encrypted.iv))
            return cipher.doFinal(encrypted.ciphertext)
        }
    }

    data class EncryptedMessage(
        val iv: ByteArray,
        val ciphertext: ByteArray,
        val chainKeyUsed: ByteArray
    )

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

        fun hkdf(input: ByteArray, info: String): ByteArray {
            val mac = javax.crypto.Mac.getInstance("HmacSHA256")
            val salt = "SeChat2024".toByteArray()
            mac.init(javax.crypto.spec.SecretKeySpec(salt, "HmacSHA256"))
            val prk = mac.doFinal(input)
            mac.init(javax.crypto.spec.SecretKeySpec(prk, "HmacSHA256"))
            mac.update(info.toByteArray())
            mac.update(1)
            return mac.doFinal().copyOf(32)
        }
    }
}
