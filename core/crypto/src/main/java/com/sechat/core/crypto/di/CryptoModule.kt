package com.sechat.core.crypto.di

import com.sechat.core.crypto.IdentityManager
import com.sechat.core.crypto.SessionCipher
import org.koin.dsl.module

val cryptoModule = module {
    single { IdentityManager() }
    single { SessionCipher() }
}
