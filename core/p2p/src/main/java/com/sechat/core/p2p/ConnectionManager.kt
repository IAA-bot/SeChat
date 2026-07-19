package com.sechat.core.p2p

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

enum class ConnectionState {
    DISCONNECTED,
    LISTENING,
    CONNECTING,
    CONNECTED,
    FAILED
}

data class PeerConnection(
    val peerId: String,
    val host: String,
    val port: Int,
    val socket: Socket? = null
)

class ConnectionManager(private val localPort: Int = 0) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var listenJob: Job? = null

    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    val state: Flow<ConnectionState> = _state.asStateFlow()

    private val activeConnections = ConcurrentHashMap<String, Socket>()
    private val _incomingMessages = Channel<WireMessage>(Channel.BUFFERED)
    val incomingMessages: Channel<WireMessage> = _incomingMessages

    fun startListening(port: Int = localPort): Int {
        serverSocket = ServerSocket(port)
        val actualPort = serverSocket!!.localPort
        _state.value = ConnectionState.LISTENING

        listenJob = scope.launch {
            while (isActive) {
                try {
                    val client = serverSocket?.accept() ?: break
                    handleConnection(client)
                } catch (_: Exception) {
                    break
                }
            }
        }
        return actualPort
    }

    fun stopListening() {
        listenJob?.cancel()
        serverSocket?.close()
        serverSocket = null
        if (_state.value == ConnectionState.LISTENING) {
            _state.value = ConnectionState.DISCONNECTED
        }
    }

    suspend fun connect(peerId: String, host: String, port: Int): Boolean {
        _state.value = ConnectionState.CONNECTING
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 5000)
            activeConnections[peerId] = socket
            handleConnection(socket)
            _state.value = ConnectionState.CONNECTED
            true
        } catch (e: Exception) {
            _state.value = ConnectionState.FAILED
            false
        }
    }

    suspend fun send(peerId: String, message: WireMessage): Boolean {
        val socket = activeConnections[peerId] ?: return false
        return try {
            val output = socket.getOutputStream()
            val data = message.serialize()
            output.write(data)
            output.flush()
            true
        } catch (_: Exception) {
            activeConnections.remove(peerId)
            false
        }
    }

    fun disconnect(peerId: String) {
        activeConnections.remove(peerId)?.close()
    }

    fun disconnectAll() {
        activeConnections.forEach { (_, socket) -> socket.close() }
        activeConnections.clear()
        stopListening()
        _state.value = ConnectionState.DISCONNECTED
    }

    private fun handleConnection(socket: Socket) {
        scope.launch {
            try {
                val input = socket.getInputStream()
                val reader = MessageStreamReader(input)
                while (isActive) {
                    val message = reader.readMessage()
                    if (message != null) {
                        activeConnections[message.senderId] = socket
                        _incomingMessages.send(message)
                    } else break
                }
            } catch (_: Exception) { }
        }
    }
}

private class MessageStreamReader(private val input: InputStream) {
    fun readMessage(): WireMessage? {
        return try {
            val sizeBuf = ByteArray(4)
            if (input.read(sizeBuf) != 4) return null
            val size = java.nio.ByteBuffer.wrap(sizeBuf).getInt()
            if (size <= 0 || size > 1024 * 1024) return null
            val data = ByteArray(size).apply { input.readFully(this) }
            WireMessage.deserialize(data)
        } catch (_: Exception) {
            null
        }
    }

    private fun InputStream.readFully(buffer: ByteArray) {
        var offset = 0
        while (offset < buffer.size) {
            val read = read(buffer, offset, buffer.size - offset)
            if (read == -1) throw java.io.EOFException()
            offset += read
        }
    }
}
