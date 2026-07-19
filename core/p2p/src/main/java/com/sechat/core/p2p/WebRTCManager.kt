package com.sechat.core.p2p

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class WebRTCState {
    DISCONNECTED, CONNECTING, CONNECTED, FAILED
}

class WebRTCManager {

    private val _state = MutableStateFlow(WebRTCState.DISCONNECTED)
    val state = _state.asStateFlow()

    private val _incomingMessages = Channel<WireMessage>(Channel.BUFFERED)
    val incomingMessages = _incomingMessages

    fun initialize() {
        _state.value = WebRTCState.CONNECTING
        // Requires org.webrtc:google-webrtc dependency
        // PeerConnectionFactory, ICE/STUN, DataChannel
        _state.value = WebRTCState.DISCONNECTED
    }

    fun connect(peerId: String, useTor: Boolean = false) {
        _state.value = WebRTCState.CONNECTING
        // STUN hole punching via WebRTC ICE
        // When Tor enabled: TCP-only candidates
        _state.value = WebRTCState.CONNECTED
    }

    fun send(message: WireMessage): Boolean {
        // DataChannel.send()
        return false
    }

    fun disconnect() {
        _state.value = WebRTCState.DISCONNECTED
    }

    fun dispose() {
        disconnect()
    }
}
