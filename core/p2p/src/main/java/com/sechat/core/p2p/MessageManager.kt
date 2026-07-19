package com.sechat.core.p2p

import com.sechat.core.crypto.IdentityManager
import com.sechat.core.crypto.SessionCipher
import com.sechat.core.data.MessageRepository
import com.sechat.core.data.StoredMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    val isEncrypted: Boolean = true,
)

fun StoredMessage.toChatMessage(): ChatMessage =
    ChatMessage(
        id = "$sessionId-$id",
        senderId = sender,
        text = "\uD83D\uDD12 Encrypted message",
        timestamp = timestamp,
        isSent = isSent,
        isEncrypted = true,
    )

class MessageManager(
    private val connectionManager: ConnectionManager,
    private val sessionCipher: SessionCipher,
    private val identityManager: IdentityManager,
    private val messageRepository: MessageRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sessions = mutableMapOf<String, SessionCipher.Session>()
    private val liveMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    val messages: StateFlow<List<ChatMessage>> =
        combine(
            messageRepository.allStoredMessages(),
            liveMessages,
        ) { stored, live ->
            stored.map { it.toChatMessage() } + live
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

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
                    MessageType.CIPHERTEXT -> decryptAndSave(msg)
                    else -> { }
                }
            }
        }
    }

    fun establishSession(
        peerId: String,
        remotePublicKey: PublicKey,
    ) {
        val identity = identityManager.getKeyPair() ?: return
        val session = sessionCipher.createSenderSession(identity.keyPair, remotePublicKey)
        sessions[peerId] = session
        scope.launch {
            connectionManager.send(
                peerId,
                WireMessage(MessageType.SESSION_SETUP, peerId, identity.publicKeyRaw),
            )
        }
    }

    fun sendMessage(
        peerId: String,
        text: String,
    ) {
        val session = sessions[peerId] ?: return
        val identity = identityManager.getKeyPair() ?: return
        val encrypted = session.encrypt(text.toByteArray(Charsets.UTF_8))
        val payload = serializePayload(encrypted)
        scope.launch {
            val sent =
                connectionManager.send(
                    peerId,
                    WireMessage(MessageType.CIPHERTEXT, peerId, payload),
                )
            if (sent) {
                messageRepository.saveMessage(peerId, identity.fingerprint, encrypted.ciphertext, true)
            } else {
                liveMessages.value = liveMessages.value +
                    ChatMessage(
                        id = "$peerId-fail-${System.currentTimeMillis()}",
                        senderId = peerId, text = text,
                        timestamp = System.currentTimeMillis(), isSent = true,
                    )
            }
        }
    }

    private fun buildReceiverSession(msg: WireMessage): SessionCipher.Session? {
        val identity = identityManager.getKeyPair() ?: return null
        return try {
            val kf = KeyFactory.getInstance("EC")
            val remote = kf.generatePublic(X509EncodedKeySpec(msg.payload))
            sessionCipher.createReceiverSession(identity.keyPair, remote)
        } catch (_: Exception) {
            null
        }
    }

    private fun decryptAndSave(msg: WireMessage) {
        val session = sessions[msg.senderId] ?: return
        try {
            val (ct, iv, counter) = deserializePayload(msg.payload)
            val text = String(session.decrypt(ct, iv, counter), Charsets.UTF_8)
            liveMessages.value = liveMessages.value +
                ChatMessage(
                    id = "${msg.senderId}-$counter",
                    senderId = msg.senderId, text = text,
                    timestamp = System.currentTimeMillis(), isSent = false,
                )
        } catch (_: Exception) {
        }
    }

    private fun serializePayload(ct: SessionCipher.CipherText): ByteArray {
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

    private fun deserializePayload(data: ByteArray): Triple<ByteArray, ByteArray, Long> {
        val bais = ByteArrayInputStream(data)
        DataInputStream(bais).use { dis ->
            val counter = dis.readLong()
            val iv = ByteArray(dis.readInt()).apply { dis.readFully(this) }
            val ct = ByteArray(dis.readInt()).apply { dis.readFully(this) }
            return Triple(ct, iv, counter)
        }
    }
}
