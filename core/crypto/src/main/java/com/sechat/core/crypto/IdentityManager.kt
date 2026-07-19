package com.sechat.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

class IdentityManager {

    private val keyStoreAlias = "sechat_identity_key"

    fun generateIdentity(): KeyPair {
        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_ED25519,
            "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            keyStoreAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build()
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    fun getKeyPair(): KeyPair? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = keyStore.getEntry(keyStoreAlias, null) as? KeyStore.PrivateKeyEntry
            ?: return null
        return KeyPair(entry.certificate.publicKey, entry.privateKey)
    }

    fun getPublicKey(): PublicKey? = getKeyPair()?.public
    fun getPrivateKey(): PrivateKey? = getKeyPair()?.private

    fun hasIdentity(): Boolean {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return keyStore.containsAlias(keyStoreAlias)
    }

    fun deleteIdentity() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        keyStore.deleteEntry(keyStoreAlias)
    }

    fun getFingerprint(publicKey: PublicKey): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(publicKey.encoded)
        return hash.take(8).joinToString("") { "%02x".format(it) }
    }
}
