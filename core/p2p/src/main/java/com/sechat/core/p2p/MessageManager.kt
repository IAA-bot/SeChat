package com.sechat.core.p2p

import com.sechat.core.crypto.IdentityManager
import com.sechat.core.crypto.SessionCipher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isSent: Boolean,
    val isEncrypted: Boolean = true
)

class MessageManager(
    private val connectionManager: ConnectionManager,
    private val sessionCipher: SessionCipher,
    private val identityManager: IdentityManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sessions = mutableMapOf<String, SessionCipher.Session>()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    fun startListening() {
        scope.launch {
            for (msg in connectionManager.incomingMessages) {
                when (msg.type) {
                    MessageType.SESSION_SETUP -> {
                        val session = buildReceiverSession(msg)
                        if (session != null) {
                            sessions[msg.senderId] = session
                        }
                    }
                    MessageType.CIPHERTEXT -> {
                        val decrypted = decryptMessage(msg)
                        if (decrypted != null) {
                            _messages.value = _messages.value + decrypted
                        }
                    }
                    else -> { }
                }
            }
        }
    }

    fun establishSession(peerId: String, remotePublicKey: PublicKey) {
        val identity = identityManager.getKeyPair() ?: return
        val session = sessionCipher.createSenderSession(identity.keyPair, remotePublicKey)
        sessions[peerId] = session

        scope.launch {
            connectionManager.send(
                peerId,
                WireMessage(MessageType.SESSION_SETUP, peerId, identity.publicKeyRaw)
            )
        }
    }

    fun sendMessage(peerId: String, text: String) {
        val session = sessions[peerId] ?: return
        val encrypted = session.encrypt(text.toByteArray(Charsets.UTF_8))
        val payload = serializeCipherPayload(encrypted)

        scope.launch {
            val sent = connectionManager.send(
                peerId,
                WireMessage(MessageType.CIPHERTEXT, peerId, payload)
            )
            if (sent) {
                _messages.value = _messages.value + ChatMessage(
                    id = "${peerId}-send-${System.currentTimeMillis()}",
                    senderId = peerId,
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    isSent = true
                )
            }
        }
    }

    private fun buildReceiverSession(msg: WireMessage): SessionCipher.Session? {
        val identity = identityManager.getKeyPair() ?: return null
        return try {
            val keyFactory = KeyFactory.getInstance("EC")
            val remotePublicKey = keyFactory.generatePublic(X509EncodedKeySpec(msg.payload))
            sessionCipher.createReceiverSession(identity.keyPair, remotePublicKey)
        } catch (_: Exception) { null }
    }

    private fun decryptMessage(msg: WireMessage): ChatMessage? {
        val session = sessions[msg.senderId] ?: return null
        return try {
            val (ciphertext, iv, counter) = deserializeCipherPayload(msg.payload)
            val plaintext = session.decrypt(ciphertext, iv, counter)
            ChatMessage(
                id = "${msg.senderId}-${counter}",
                senderId = msg.senderId,
                text = String(plaintext, Charsets.UTF_8),
                timestamp = System.currentTimeMillis(),
                isSent = false
            )
        } catch (_: Exception) { null }
    }

    private fun serializeCipherPayload(ct: SessionCipher.CipherText): ByteArray {
        val baos = ByteArrayOutputStream()
        DataOutputStream(baos).use { dos ->
            dos.writeLong(ct.messageCounter)
            dos.writeInt(ct.iv.size)
            dos.write(ct.iv)
            dos.writeInt(ct.ciphertext.size)
            dos.write(ct.ciphertext)
        }
        return baos.toByteArray()
    }

    private fun deserializeCipherPayload(data: ByteArray): Triple<ByteArray, ByteArray, Long> {
        val bais = ByteArrayInputStream(data)
        DataInputStream(bais).use { dis ->
            val counter = dis.readLong()
            val ivSize = dis.readInt()
            val iv = ByteArray(ivSize).apply { dis.readFully(this) }
            val ctSize = dis.readInt()
            val ct = ByteArray(ctSize).apply { dis.readFully(this) }
            return Triple(ct, iv, counter)
        }
    }
}
