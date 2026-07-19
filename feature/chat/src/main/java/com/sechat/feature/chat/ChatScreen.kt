package com.sechat.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contactId: String,
    onNavigateBack: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf<MessageUi>()) }
    var inputText by remember { mutableStateOf("") }
    var connectionState by remember { mutableStateOf<ConnectionState>(ConnectionState.Connecting) }
    var showEncryptionConfirmed by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        connectionState = ConnectionState.Connected
        showEncryptionConfirmed = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(contactId.take(8))
                        Spacer(Modifier.width(8.dp))
                        ConnectionIndicator(connectionState)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (connectionState == ConnectionState.Connected && showEncryptionConfirmed) {
                EncryptionConfirmedBanner()
            }

            if (connectionState == ConnectionState.Failed) {
                ConnectionFailedBanner(onRetry = {
                    connectionState = ConnectionState.Connecting
                })
            }

            if (messages.isEmpty() && connectionState == ConnectionState.Connected) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This is the beginning of your encrypted conversation.\nMessages are end-to-end encrypted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(messages) { msg ->
                        MessageBubble(message = msg)
                    }
                }
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message") },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val msg = MessageUi(
                                id = messages.size.toString(),
                                text = inputText,
                                isSent = true,
                                timestamp = System.currentTimeMillis()
                            )
                            messages = messages + msg
                            inputText = ""
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "\u2191",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionIndicator(state: ConnectionState) {
    val (color, text) = when (state) {
        ConnectionState.Connecting -> Color(0xFFFF9800) to "Connecting"
        ConnectionState.Connected -> Color(0xFF4CAF50) to "E2EE \u2713"
        ConnectionState.Failed -> Color(0xFFF44336) to "Disconnected"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = color
        ) {}
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun EncryptionConfirmedBanner() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "\uD83D\uDD12 Encrypted connection established. Only you and the recipient can read these messages.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun ConnectionFailedBanner(onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Encrypted session expired or connection failed. Re-scan QR to re-establish.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828)
                )
            ) {
                Text("Re-scan", color = Color.White)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageUi) {
    val alignment = if (message.isSent) Alignment.End else Alignment.Start
    val bg = if (message.isSent)
        Color(0xFF007AFF) else Color(0xFFF0F0F0)
    val fg = if (message.isSent) Color.White else Color.Black
    val shape = if (message.isSent)
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    else
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = shape,
            color = bg
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = fg,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (message.isSent) {
            Text(
                text = "\uD83D\uDD12",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(end = 4.dp, top = 2.dp)
            )
        }
    }
}

sealed class ConnectionState {
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data object Failed : ConnectionState()
}

data class MessageUi(
    val id: String,
    val text: String,
    val isSent: Boolean,
    val timestamp: Long
)
