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
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED,
}

class WebRTCManager(private val context: Context) {
    private val thread = Executors.newSingleThreadExecutor()
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private var pendingIceCandidates = mutableListOf<IceCandidate>()

    private val _state = MutableStateFlow(WebRTCState.DISCONNECTED)
    val state = _state.asStateFlow()

    private val _incomingMessages = Channel<WireMessage>(Channel.BUFFERED)
    val incomingMessages = _incomingMessages

    private val stunServers =
        listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        )

    private var signalingCallback: SignalingCallback? = null

    interface SignalingCallback {
        fun onOfferCreated(sdp: String)

        fun onAnswerCreated(sdp: String)

        fun onIceCandidateGenerated(
            candidate: String,
            sdpMid: String,
            sdpMLineIndex: Int,
        )
    }

    fun initialize() {
        thread.execute {
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials("")
                .createInitializationOptions()
                .let { PeerConnectionFactory.initialize(it) }

            factory =
                PeerConnectionFactory.builder()
                    .setVideoDecoderFactory(null)
                    .setVideoEncoderFactory(null)
                    .createPeerConnectionFactory()
        }
    }

    fun createOffer(callback: SignalingCallback) {
        signalingCallback = callback
        _state.value = WebRTCState.CONNECTING
        thread.execute {
            peerConnection = createPeerConnection()
            val dc =
                peerConnection?.createDataChannel(
                    "sechat",
                    DataChannel.Init().apply {
                        ordered = true
                        negotiated = false
                    },
                )
            if (dc != null) attachDataChannel(dc)

            val constraints =
                MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
                }

            peerConnection?.createOffer(
                object : org.webrtc.SdpObserver {
                    override fun onCreateSuccess(desc: SessionDescription) {
                        peerConnection?.setLocalDescription(
                            object : org.webrtc.SdpObserver {
                                override fun onSetSuccess() {
                                    callback.onOfferCreated(desc.description)
                                }

                                override fun onSetFailure(msg: String) {
                                    _state.value = WebRTCState.FAILED
                                }

                                override fun onCreateSuccess(desc: SessionDescription) {}

                                override fun onCreateFailure(msg: String) {}
                            },
                            desc,
                        )
                    }

                    override fun onSetSuccess() {}

                    override fun onSetFailure(msg: String) {
                        _state.value = WebRTCState.FAILED
                    }

                    override fun onCreateFailure(msg: String) {
                        _state.value = WebRTCState.FAILED
                    }
                },
                constraints,
            )
        }
    }

    fun receiveOffer(
        sdp: String,
        callback: SignalingCallback,
    ) {
        signalingCallback = callback
        thread.execute {
            peerConnection = createPeerConnection()
            val desc = SessionDescription(SessionDescription.Type.OFFER, sdp)
            peerConnection?.setRemoteDescription(
                object : org.webrtc.SdpObserver {
                    override fun onSetSuccess() {
                        val constraints =
                            MediaConstraints().apply {
                                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
                                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
                            }
                        peerConnection?.createAnswer(
                            object : org.webrtc.SdpObserver {
                                override fun onCreateSuccess(desc: SessionDescription) {
                                    peerConnection?.setLocalDescription(
                                        object : org.webrtc.SdpObserver {
                                            override fun onSetSuccess() {
                                                callback.onAnswerCreated(desc.description)
                                            }

                                            override fun onSetFailure(msg: String) {
                                                _state.value = WebRTCState.FAILED
                                            }

                                            override fun onCreateSuccess(desc: SessionDescription) {}

                                            override fun onCreateFailure(msg: String) {}
                                        },
                                        desc,
                                    )
                                }

                                override fun onSetSuccess() {}

                                override fun onSetFailure(msg: String) {
                                    _state.value = WebRTCState.FAILED
                                }

                                override fun onCreateFailure(msg: String) {
                                    _state.value = WebRTCState.FAILED
                                }
                            },
                            constraints,
                        )
                    }

                    override fun onSetFailure(msg: String) {
                        _state.value = WebRTCState.FAILED
                    }

                    override fun onCreateSuccess(desc: SessionDescription) {}

                    override fun onCreateFailure(msg: String) {}
                },
                desc,
            )
        }
    }

    fun receiveAnswer(sdp: String) {
        thread.execute {
            val desc = SessionDescription(SessionDescription.Type.ANSWER, sdp)
            peerConnection?.setRemoteDescription(
                object : org.webrtc.SdpObserver {
                    override fun onSetSuccess() {
                        flushPendingCandidates()
                    }

                    override fun onSetFailure(msg: String) {
                        _state.value = WebRTCState.FAILED
                    }

                    override fun onCreateSuccess(desc: SessionDescription) {}

                    override fun onCreateFailure(msg: String) {}
                },
                desc,
            )
        }
    }

    fun addIceCandidate(
        candidate: String,
        sdpMid: String,
        sdpMLineIndex: Int,
    ) {
        thread.execute {
            val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)
            if (peerConnection?.remoteDescription != null) {
                peerConnection?.addIceCandidate(iceCandidate)
            } else {
                pendingIceCandidates.add(iceCandidate)
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
            pendingIceCandidates.clear()
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

    private fun createPeerConnection(): PeerConnection? {
        val rtcConfig =
            PeerConnection.RTCConfiguration(stunServers).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                iceCandidatePoolSize = 1
            }

        return factory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    signalingCallback?.onIceCandidateGenerated(
                        candidate.sdp,
                        candidate.sdpMid ?: "",
                        candidate.sdpMLineIndex,
                    )
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}

                override fun onSignalingChange(state: PeerConnection.SignalingState) {}

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    _state.value =
                        when (state) {
                            PeerConnection.IceConnectionState.CONNECTED -> WebRTCState.CONNECTED
                            PeerConnection.IceConnectionState.DISCONNECTED,
                            PeerConnection.IceConnectionState.CLOSED,
                            -> WebRTCState.DISCONNECTED
                            PeerConnection.IceConnectionState.FAILED -> WebRTCState.FAILED
                            else -> _state.value
                        }
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {}

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

                override fun onAddStream(stream: org.webrtc.MediaStream) {}

                override fun onRemoveStream(stream: org.webrtc.MediaStream) {}

                override fun onRenegotiationNeeded() {}

                override fun onDataChannel(channel: DataChannel) {
                    attachDataChannel(channel)
                }

                override fun onAddTrack(
                    track: org.webrtc.RtpReceiver,
                    streams: Array<out org.webrtc.MediaStream>,
                ) {}
            },
        )
    }

    private fun attachDataChannel(dc: DataChannel) {
        dataChannel = dc
        dc.registerObserver(
            object : DataChannel.Observer {
                override fun onBufferedAmountChange(previous: Long) {}

                override fun onStateChange() {}

                override fun onMessage(buffer: DataChannel.Buffer) {
                    val bytes = ByteArray(buffer.data.remaining())
                    buffer.data.get(bytes)
                    val msg = WireMessage.deserialize(bytes)
                    kotlinx.coroutines.runBlocking { _incomingMessages.send(msg) }
                }
            },
        )
    }

    private fun flushPendingCandidates() {
        pendingIceCandidates.forEach { peerConnection?.addIceCandidate(it) }
        pendingIceCandidates.clear()
    }
}
