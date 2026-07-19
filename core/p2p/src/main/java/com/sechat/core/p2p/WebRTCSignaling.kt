package com.sechat.core.p2p

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

enum class SignalingMessageType(val value: Int) {
    OFFER(10),
    ANSWER(11),
    ICE_CANDIDATE(12);

    companion object {
        fun fromValue(v: Int): SignalingMessageType =
            entries.firstOrNull { it.value == v }
                ?: throw IllegalArgumentException("Unknown signaling type: $v")
    }
}

data class SignalingPayload(
    val type: SignalingMessageType,
    val data: String,
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0
) {
    fun toWireMessage(peerId: String): WireMessage {
        val payload = buildString {
            append(type.value)
            append("|")
            append(data.length)
            append("|")
            append(data)
            if (type == SignalingMessageType.ICE_CANDIDATE) {
                append("|").append(sdpMid)
                append("|").append(sdpMLineIndex)
            }
        }.toByteArray()
        return WireMessage(MessageType.PREKEY_BUNDLE, peerId, payload)
    }

    companion object {
        fun fromWireMessage(msg: WireMessage): SignalingPayload? {
            return try {
                val str = String(msg.payload)
                val parts = str.split("|", limit = 3)
                val type = SignalingMessageType.fromValue(parts[0].toInt())
                val dataLen = parts[1].toInt()
                val data = parts[2].take(dataLen)
                if (type == SignalingMessageType.ICE_CANDIDATE) {
                    val iceParts = parts[2].substring(dataLen + 1).split("|")
                    SignalingPayload(type, data, iceParts[0], iceParts[1].toInt())
                } else {
                    SignalingPayload(type, data)
                }
            } catch (_: Exception) { null }
        }
    }
}

class WebRTCSignaling(private val webRTCManager: WebRTCManager) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun startListening(connectionManager: ConnectionManager) {
        scope.launch {
            for (msg in connectionManager.incomingMessages) {
                if (msg.type != MessageType.PREKEY_BUNDLE) continue
                val signal = SignalingPayload.fromWireMessage(msg) ?: continue
                when (signal.type) {
                    SignalingMessageType.OFFER -> {
                        webRTCManager.receiveOffer(signal.data, createCallback(msg.senderId, connectionManager))
                    }
                    SignalingMessageType.ANSWER -> {
                        webRTCManager.receiveAnswer(signal.data)
                    }
                    SignalingMessageType.ICE_CANDIDATE -> {
                        webRTCManager.addIceCandidate(signal.data, signal.sdpMid, signal.sdpMLineIndex)
                    }
                }
            }
        }
    }

    fun initiateConnection(peerId: String, connectionManager: ConnectionManager) {
        webRTCManager.createOffer(createCallback(peerId, connectionManager))
    }

    private fun createCallback(peerId: String, cm: ConnectionManager): WebRTCManager.SignalingCallback {
        return object : WebRTCManager.SignalingCallback {
            override fun onOfferCreated(sdp: String) {
                val payload = SignalingPayload(SignalingMessageType.OFFER, sdp)
                scope.launch { cm.send(peerId, payload.toWireMessage(peerId)) }
            }

            override fun onAnswerCreated(sdp: String) {
                val payload = SignalingPayload(SignalingMessageType.ANSWER, sdp)
                scope.launch { cm.send(peerId, payload.toWireMessage(peerId)) }
            }

            override fun onIceCandidateGenerated(candidate: String, sdpMid: String, sdpMLineIndex: Int) {
                val payload = SignalingPayload(
                    SignalingMessageType.ICE_CANDIDATE, candidate, sdpMid, sdpMLineIndex
                )
                scope.launch { cm.send(peerId, payload.toWireMessage(peerId)) }
            }
        }
    }
}
