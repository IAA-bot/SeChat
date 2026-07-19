package com.sechat.core.p2p

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DiscoveryEngine(private val context: Context) {
    private val serviceType = "_sechat._tcp."
    private val serviceName = "SeChatPeer"

    private val nsdManager: NsdManager
        get() = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    fun registerService(port: Int): Flow<NsdRegistrationState> =
        callbackFlow {
            val registrationListener =
                object : NsdManager.RegistrationListener {
                    override fun onServiceRegistered(info: NsdServiceInfo) {
                        trySend(NsdRegistrationState.Registered(info))
                    }

                    override fun onRegistrationFailed(
                        info: NsdServiceInfo,
                        errorCode: Int,
                    ) {
                        trySend(NsdRegistrationState.Failed(errorCode))
                    }

                    override fun onServiceUnregistered(info: NsdServiceInfo) {
                        trySend(NsdRegistrationState.Unregistered)
                    }

                    override fun onUnregistrationFailed(
                        info: NsdServiceInfo,
                        errorCode: Int,
                    ) {
                        trySend(NsdRegistrationState.UnregisterFailed(errorCode))
                    }
                }

            val serviceInfo =
                NsdServiceInfo().apply {
                    serviceType = this@DiscoveryEngine.serviceType
                    serviceName = this@DiscoveryEngine.serviceName
                    this@apply.port = port
                }

            nsdManager.registerService(serviceInfo, 1, registrationListener)

            awaitClose {
                try {
                    nsdManager.unregisterService(registrationListener)
                } catch (_: Exception) {
                }
            }
        }

    fun discoverServices(): Flow<NsdDiscoveredService> =
        callbackFlow {
            val discoveryListener =
                object : NsdManager.DiscoveryListener {
                    override fun onDiscoveryStarted(serviceType: String) {
                        trySend(NsdDiscoveredService.DiscoveryStarted)
                    }

                    override fun onDiscoveryStopped(serviceType: String) {
                        trySend(NsdDiscoveredService.DiscoveryStopped)
                    }

                    override fun onStartDiscoveryFailed(
                        serviceType: String,
                        errorCode: Int,
                    ) {
                        trySend(NsdDiscoveredService.DiscoveryFailed(errorCode))
                    }

                    override fun onStopDiscoveryFailed(
                        serviceType: String,
                        errorCode: Int,
                    ) { }

                    override fun onServiceFound(info: NsdServiceInfo) {
                        trySend(NsdDiscoveredService.Found(info))
                    }

                    override fun onServiceLost(info: NsdServiceInfo) {
                        trySend(NsdDiscoveredService.Lost(info))
                    }
                }

            nsdManager.discoverServices(serviceType, 1, discoveryListener)

            awaitClose {
                try {
                    nsdManager.stopServiceDiscovery(discoveryListener)
                } catch (_: Exception) {
                }
            }
        }

    fun resolveService(info: NsdServiceInfo): Flow<NsdResolvedService> =
        callbackFlow {
            val resolveListener =
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(
                        info: NsdServiceInfo,
                        errorCode: Int,
                    ) {
                        trySend(NsdResolvedService.ResolveFailed(errorCode))
                    }

                    override fun onServiceResolved(info: NsdServiceInfo) {
                        trySend(NsdResolvedService.Resolved(info))
                    }
                }

            nsdManager.resolveService(info, resolveListener)

            awaitClose { }
        }
}

sealed class NsdRegistrationState {
    data class Registered(val info: NsdServiceInfo) : NsdRegistrationState()

    data class Failed(val errorCode: Int) : NsdRegistrationState()

    data object Unregistered : NsdRegistrationState()

    data class UnregisterFailed(val errorCode: Int) : NsdRegistrationState()
}

sealed class NsdDiscoveredService {
    data object DiscoveryStarted : NsdDiscoveredService()

    data object DiscoveryStopped : NsdDiscoveredService()

    data class Found(val info: NsdServiceInfo) : NsdDiscoveredService()

    data class Lost(val info: NsdServiceInfo) : NsdDiscoveredService()

    data class DiscoveryFailed(val errorCode: Int) : NsdDiscoveredService()
}

sealed class NsdResolvedService {
    data class Resolved(val info: NsdServiceInfo) : NsdResolvedService()

    data class ResolveFailed(val errorCode: Int) : NsdResolvedService()
}
