package com.sechat.core.p2p.di

import com.sechat.core.p2p.ConnectionManager
import com.sechat.core.p2p.MessageManager
import com.sechat.core.p2p.ServiceManager
import com.sechat.core.p2p.TorProxyManager
import com.sechat.core.p2p.TransportManager
import com.sechat.core.p2p.WebRTCManager
import com.sechat.core.p2p.WebRTCSignaling
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val p2pModule =
    module {
        single { ConnectionManager() }
        single { WebRTCManager(androidContext()) }
        single { WebRTCSignaling(get()) }
        single { ServiceManager(androidContext()) }
        single { TorProxyManager(androidContext()) }
        single {
            TransportManager(
                context = androidContext(),
                tcpManager = get(),
                webRTCManager = get(),
                torManager = get(),
            )
        }
        single {
            MessageManager(
                connectionManager = get<ConnectionManager>(),
                sessionCipher = get(),
                identityManager = get(),
                messageRepository = get(),
            )
        }
    }
