package com.sechat.core.p2p.di

import com.sechat.core.crypto.IdentityManager
import com.sechat.core.crypto.SessionCipher
import com.sechat.core.p2p.ConnectionManager
import com.sechat.core.p2p.MessageManager
import org.koin.dsl.module

val p2pModule = module {
    single { ConnectionManager() }
    single {
        MessageManager(
            connectionManager = get(),
            sessionCipher = get(),
            identityManager = get(),
            messageRepository = get()
        )
    }
}
