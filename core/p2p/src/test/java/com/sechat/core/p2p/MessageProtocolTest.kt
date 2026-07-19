package com.sechat.core.p2p

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageProtocolTest {
    @Test
    fun `wire message serialization roundtrip`() {
        val original =
            WireMessage(
                type = MessageType.CIPHERTEXT,
                senderId = "alice",
                payload = byteArrayOf(0x01, 0x02, 0x03),
            )

        val data = original.serialize()
        val restored = WireMessage.deserialize(data)

        assertEquals(original.type, restored.type)
        assertEquals(original.senderId, restored.senderId)
        assertArrayEquals(original.payload, restored.payload)
    }

    @Test
    fun `session setup message serialization`() {
        val original =
            WireMessage(
                type = MessageType.SESSION_SETUP,
                senderId = "bob",
                payload = ByteArray(32) { it.toByte() },
            )

        val restored = WireMessage.deserialize(original.serialize())

        assertEquals(MessageType.SESSION_SETUP, restored.type)
        assertEquals("bob", restored.senderId)
        assertEquals(32, restored.payload.size)
    }

    @Test
    fun `ping message roundtrip`() {
        val msg = WireMessage(MessageType.PING, "alice", ByteArray(0))
        val restored = WireMessage.deserialize(msg.serialize())

        assertEquals(MessageType.PING, restored.type)
        assertTrue(restored.payload.isEmpty())
    }

    @Test
    fun `large payload serialization`() {
        val payload = ByteArray(65536) { (it % 256).toByte() }
        val msg = WireMessage(MessageType.CIPHERTEXT, "alice", payload)

        val restored = WireMessage.deserialize(msg.serialize())

        assertEquals(payload.size, restored.payload.size)
        assertArrayEquals(payload, restored.payload)
    }

    @Test
    fun `message types map correctly`() {
        assertEquals(1, MessageType.PREKEY_BUNDLE.value)
        assertEquals(2, MessageType.CIPHERTEXT.value)
        assertEquals(3, MessageType.SESSION_SETUP.value)
        assertEquals(4, MessageType.PING.value)
        assertEquals(5, MessageType.PONG.value)
    }

    @Test
    fun `sender id with special characters`() {
        val msg =
            WireMessage(
                type = MessageType.CIPHERTEXT,
                senderId = "user@device:123",
                payload = byteArrayOf(),
            )
        val restored = WireMessage.deserialize(msg.serialize())
        assertEquals("user@device:123", restored.senderId)
    }
}
