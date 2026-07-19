package com.sechat.core.p2p

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TransportMode { TCP_LAN, WEBRTC, TOR }

enum class TransportState { DISCONNECTED, LISTENING, CONNECTING, CONNECTED, FAILED }

class TransportManager(
    private val context: Context,
    private val tcpManager: ConnectionManager,
    private val webRTCManager: WebRTCManager,
    val torManager: TorProxyManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var signaling: WebRTCSignaling? = null

    private var currentMode = TransportMode.TCP_LAN

    private val _state = MutableStateFlow(TransportState.DISCONNECTED)
    val state = _state.asStateFlow()

    val incomingMessages get() =
        when (currentMode) {
            TransportMode.TCP_LAN -> tcpManager.incomingMessages
            TransportMode.WEBRTC -> webRTCManager.incomingMessages
            TransportMode.TOR -> tcpManager.incomingMessages
        }

    fun switchMode(mode: TransportMode) {
        disconnect()
        currentMode = mode
        when (mode) {
            TransportMode.TCP_LAN -> tcpManager.startListening()
            TransportMode.WEBRTC -> {
                webRTCManager.initialize()
                signaling = WebRTCSignaling(webRTCManager)
                signaling?.startListening(tcpManager)
            }
            TransportMode.TOR -> {
                if (torManager.isOrbotInstalled()) {
                    java.net.ProxySelector.setDefault(torManager.createTorProxySelector())
                    tcpManager.startListening()
                }
            }
        }
    }

    suspend fun connect(
        peerId: String,
        host: String,
        port: Int,
    ): Boolean {
        return when (currentMode) {
            TransportMode.TCP_LAN -> tcpManager.connect(peerId, host, port)
            TransportMode.WEBRTC -> {
                val connected = tcpManager.connect(peerId, host, port)
                if (connected) signaling?.initiateConnection(peerId, tcpManager)
                true
            }
            TransportMode.TOR -> {
                java.net.ProxySelector.setDefault(torManager.createTorProxySelector())
                tcpManager.connect(peerId, host, port)
            }
        }
    }

    suspend fun send(
        peerId: String,
        message: WireMessage,
    ): Boolean {
        return when (currentMode) {
            TransportMode.TCP_LAN, TransportMode.TOR -> tcpManager.send(peerId, message)
            TransportMode.WEBRTC -> {
                val sent = webRTCManager.send(message)
                if (!sent) tcpManager.send(peerId, message)
                true
            }
        }
    }

    fun disconnect() {
        tcpManager.disconnectAll()
        webRTCManager.disconnect()
        _state.value = TransportState.DISCONNECTED
    }

    val isTorAvailable: Boolean get() = torManager.isOrbotInstalled()
}
