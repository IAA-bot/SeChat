package com.sechat.feature.identity

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sechat.core.crypto.IdentityManager
import com.sechat.core.crypto.QrCodeEncoder
import com.sechat.core.p2p.ConnectionManager
import com.sechat.core.p2p.ConnectionState
import org.koin.java.KoinJavaComponent.get

@Composable
fun IdentityScreen(
    onNavigateToContacts: () -> Unit,
    onNavigateToScanner: () -> Unit,
) {
    val context = LocalContext.current
    val identityManager = remember { get<IdentityManager>(IdentityManager::class.java) }
    val connectionManager = remember { get<ConnectionManager>(ConnectionManager::class.java) }
    val connectionState by connectionManager.state.collectAsStateWithLifecycle(
        initialValue = ConnectionState.DISCONNECTED,
    )

    var state by remember { mutableStateOf<IdentityUiState>(IdentityUiState.Loading) }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) onNavigateToScanner()
        }

    LaunchedEffect(Unit) {
        try {
            val identity = identityManager.getKeyPair()
            if (identity != null) {
                val qrBitmap =
                    QrCodeEncoder.encode(
                        identity.publicKeyRaw,
                        identity.fingerprint,
                    )
                state =
                    IdentityUiState.Ready(
                        publicKeyRaw = identity.publicKeyRaw,
                        fingerprint = identity.fingerprint,
                        qrBitmap = qrBitmap,
                    )
            } else {
                state = IdentityUiState.NotCreated
            }
        } catch (_: Exception) {
            state = IdentityUiState.NotCreated
        }

        connectionManager.startListening()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "SeChat",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Anonymous Encrypted Messenger",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(48.dp))

        when (val s = state) {
            is IdentityUiState.Loading -> {
                Text("Loading...", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
            }

            is IdentityUiState.NotCreated -> {
                Text(
                    text = "No identity found.\nCreate an anonymous key pair to start.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    state = IdentityUiState.Loading
                    try {
                        val identity = identityManager.generateIdentity()
                        val qrBitmap =
                            QrCodeEncoder.encode(
                                identity.publicKeyRaw,
                                identity.fingerprint,
                            )
                        state =
                            IdentityUiState.Ready(
                                publicKeyRaw = identity.publicKeyRaw,
                                fingerprint = identity.fingerprint,
                                qrBitmap = qrBitmap,
                            )
                    } catch (e: Exception) {
                        state = IdentityUiState.Error(e.message ?: "Failed to create identity")
                    }
                }) {
                    Text("Create Anonymous Identity")
                }
            }

            is IdentityUiState.Ready -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Your Identity",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(16.dp))

                            Image(
                                bitmap = s.qrBitmap.asImageBitmap(),
                                contentDescription = "Identity QR Code",
                                modifier =
                                    Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                            )

                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = s.fingerprint,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                ConnectionStatusBar(connectionState)

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onNavigateToContacts,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Contacts")
                    }
                    OutlinedButton(
                        onClick = {
                            val hasCamera =
                                ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA,
                                ) == PackageManager.PERMISSION_GRANTED
                            if (hasCamera) {
                                onNavigateToScanner()
                            } else {
                                cameraLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Text("Scan")
                    }
                }
            }

            is IdentityUiState.Error -> {
                Text(
                    text = s.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { state = IdentityUiState.NotCreated }) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusBar(state: ConnectionState) {
    val (color, text) =
        when (state) {
            ConnectionState.DISCONNECTED -> Color(0xFF999999) to "Offline"
            ConnectionState.LISTENING -> Color(0xFF4CAF50) to "Listening for peers"
            ConnectionState.CONNECTING -> Color(0xFFFF9800) to "Connecting..."
            ConnectionState.CONNECTED -> Color(0xFF4CAF50) to "Connected"
            ConnectionState.FAILED -> Color(0xFFF44336) to "Connection failed"
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

sealed class IdentityUiState {
    data object Loading : IdentityUiState()

    data object NotCreated : IdentityUiState()

    data class Ready(
        val publicKeyRaw: ByteArray,
        val fingerprint: String,
        val qrBitmap: android.graphics.Bitmap,
    ) : IdentityUiState()

    data class Error(val message: String) : IdentityUiState()
}
