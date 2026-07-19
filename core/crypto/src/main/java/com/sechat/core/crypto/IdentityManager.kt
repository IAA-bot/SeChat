package com.sechat.core.crypto

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom

class IdentityManager {
    private var currentIdentity: SechatIdentity? = null

    data class SechatIdentity(
        val keyPair: KeyPair,
        val publicKeyRaw: ByteArray,
        val fingerprint: String,
    )

    fun generateIdentity(): SechatIdentity {
        val generator = KeyPairGenerator.getInstance(EC_ALGORITHM)
        generator.initialize(EC_KEY_SIZE, SecureRandom())
        val keyPair = generator.generateKeyPair()
        val raw = keyPair.public.encoded
        val fp = fingerprint(raw)
        return SechatIdentity(keyPair, raw, fp).also { currentIdentity = it }
    }

    fun getKeyPair(): SechatIdentity? = currentIdentity

    fun hasIdentity(): Boolean = currentIdentity != null

    fun deleteIdentity() {
        currentIdentity = null
    }

    fun verifyFingerprint(
        rawKey: ByteArray,
        expectedFingerprint: String,
    ): Boolean {
        return fingerprint(rawKey) == expectedFingerprint
    }

    companion object {
        const val EC_ALGORITHM = "EC"
        const val EC_KEY_SIZE = 256
        const val FINGERPRINT_LENGTH = 8

        fun fingerprint(rawKey: ByteArray): String {
            val hash = MessageDigest.getInstance("SHA-256").digest(rawKey)
            return hash.take(FINGERPRINT_LENGTH).joinToString("") { "%02x".format(it) }
        }

        fun fingerprintDisplay(rawKey: ByteArray): String {
            return fingerprint(rawKey).chunked(4).joinToString(" ")
        }
    }
}
