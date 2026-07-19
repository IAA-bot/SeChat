package com.sechat.core.p2p

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

enum class MessageType(val value: Int) {
    PREKEY_BUNDLE(1),
    CIPHERTEXT(2),
    SESSION_SETUP(3),
    PING(4),
    PONG(5),
    ;

    companion object {
        fun fromValue(v: Int): MessageType =
            entries.firstOrNull { it.value == v }
                ?: throw IllegalArgumentException("Unknown type: $v")
    }
}

data class WireMessage(
    val type: MessageType,
    val senderId: String,
    val payload: ByteArray,
) {
    fun serialize(): ByteArray {
        val baos = ByteArrayOutputStream()
        DataOutputStream(baos).use { dos ->
            dos.writeInt(type.value)
            dos.writeUTF(senderId)
            dos.writeInt(payload.size)
            dos.write(payload)
        }
        return baos.toByteArray()
    }

    companion object {
        fun deserialize(data: ByteArray): WireMessage {
            val bais = ByteArrayInputStream(data)
            DataInputStream(bais).use { dis ->
                val type = MessageType.fromValue(dis.readInt())
                val senderId = dis.readUTF()
                val size = dis.readInt()
                val payload = ByteArray(size).apply { dis.readFully(this) }
                return WireMessage(type, senderId, payload)
            }
        }
    }
}
