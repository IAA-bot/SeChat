package com.sechat.core.p2p

import android.content.Context
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import java.nio.ByteBuffer
import java.util.concurrent.Executors

enum class WebRTCState {
    DISCONNECTED, CONNECTING, CONNECTED, FAILED
}

class WebRTCManager(private val context: Context) {

    private val thread = Executors.newSingleThreadExecutor()
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null

    private val _state = MutableStateFlow(WebRTCState.DISCONNECTED)
    val state = _state.asStateFlow()

    private val _incomingMessages = Channel<WireMessage>(Channel.BUFFERED)
    val incomingMessages = _incomingMessages

    private val stunServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
    )

    fun initialize() {
        thread.execute {
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials("")
                .createInitializationOptions()
                .let { PeerConnectionFactory.initialize(it) }

            factory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(null)
                .setVideoEncoderFactory(null)
                .createPeerConnectionFactory()
        }
    }

    fun connect(peerId: String, useTor: Boolean = false) {
        _state.value = WebRTCState.CONNECTING
        thread.execute {
            try {
                val rtcConfig = PeerConnection.RTCConfiguration(stunServers).apply {
                    sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                    if (useTor) {
                        tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
                        iceTransportsType = PeerConnection.IceTransportsType.RELAY
                    }
                }

                val constraints = MediaConstraints().apply {
                    optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
                }

                peerConnection = factory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
                    override fun onIceCandidate(candidate: IceCandidate) { }
                    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) { }
                    override fun onSignalingChange(state: PeerConnection.SignalingState) { }
                    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                        _state.value = when (state) {
                            PeerConnection.IceConnectionState.CONNECTED -> WebRTCState.CONNECTED
                            PeerConnection.IceConnectionState.DISCONNECTED,
                            PeerConnection.IceConnectionState.CLOSED -> WebRTCState.DISCONNECTED
                            PeerConnection.IceConnectionState.FAILED -> WebRTCState.FAILED
                            else -> _state.value
                        }
                    }
                    override fun onIceConnectionReceivingChange(receiving: Boolean) { }
                    override fun onIceGatheringChange(gatheringState: PeerConnection.IceGatheringState?) { }
                    override fun onAddStream(stream: org.webrtc.MediaStream) { }
                    override fun onRemoveStream(stream: org.webrtc.MediaStream) { }
                    override fun onRenegotiationNeeded() { }
                    override fun onDataChannel(channel: DataChannel) {
                        attachDataChannel(channel)
                    }
                    override fun onAddTrack(track: org.webrtc.RtpReceiver, streams: Array<out org.webrtc.MediaStream>) { }
                })

                val init = DataChannel.Init().apply {
                    ordered = true
                    negotiated = false
                }
                val dc = peerConnection?.createDataChannel("sechat", init)
                if (dc != null) attachDataChannel(dc)
            } catch (e: Exception) {
                _state.value = WebRTCState.FAILED
            }
        }
    }

    fun send(message: WireMessage): Boolean {
        val dc = dataChannel ?: return false
        val buf = ByteBuffer.wrap(message.serialize())
        return dc.send(DataChannel.Buffer(buf, false))
    }

    fun disconnect() {
        thread.execute {
            dataChannel?.close()
            peerConnection?.close()
            dataChannel = null
            peerConnection = null
            _state.value = WebRTCState.DISCONNECTED
        }
    }

    fun dispose() {
        disconnect()
        thread.execute {
            factory?.dispose()
            factory = null
        }
    }

    private fun attachDataChannel(dc: DataChannel) {
        dataChannel = dc
        dc.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previous: Long) { }
            override fun onStateChange() { }
            override fun onMessage(buffer: DataChannel.Buffer) {
                val bytes = ByteArray(buffer.data.remaining())
                buffer.data.get(bytes)
                val msg = WireMessage.deserialize(bytes)
                kotlinx.coroutines.runBlocking { _incomingMessages.send(msg) }
            }
        })
    }
}
