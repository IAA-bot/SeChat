package com.sechat.feature.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sechat.core.crypto.IdentityManager

@Composable
fun IdentityScreen(
    onNavigateToContacts: () -> Unit
) {
    val identityManager = remember { IdentityManager() }
    var state by remember { mutableStateOf<IdentityState>(IdentityState.Loading) }

    LaunchedEffect(Unit) {
        state = if (identityManager.hasIdentity()) {
            val publicKey = identityManager.getPublicKey()
            if (publicKey != null) {
                val fingerprint = identityManager.getFingerprint(publicKey)
                IdentityState.Ready(fingerprint)
            } else {
                IdentityState.Error("Failed to load identity")
            }
        } else {
            IdentityState.NotCreated
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SeChat",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Anonymous Encrypted Messenger",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(48.dp))

        when (val s = state) {
            is IdentityState.Loading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Generating your identity...")
            }

            is IdentityState.NotCreated -> {
                Button(onClick = {
                    state = IdentityState.Loading
                    try {
                        identityManager.generateIdentity()
                        val pk = identityManager.getPublicKey()
                        val fp = identityManager.getFingerprint(pk!!)
                        state = IdentityState.Ready(fp)
                    } catch (e: Exception) {
                        state = IdentityState.Error(e.message ?: "Generation failed")
                    }
                }) {
                    Text("Create Anonymous Identity")
                }
            }

            is IdentityState.Ready -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Identity",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.size(160.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "QR Code Placeholder",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = s.fingerprint,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToContacts,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue to Contacts")
                }
            }

            is IdentityState.Error -> {
                Text(
                    text = s.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    state = IdentityState.NotCreated
                }) {
                    Text("Retry")
                }
            }
        }
    }
}

sealed class IdentityState {
    data object Loading : IdentityState()
    data object NotCreated : IdentityState()
    data class Ready(val fingerprint: String) : IdentityState()
    data class Error(val message: String) : IdentityState()
}
