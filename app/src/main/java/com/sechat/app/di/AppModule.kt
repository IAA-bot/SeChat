package com.sechat.app

import com.sechat.core.crypto.di.cryptoModule
import com.sechat.core.data.di.dataModule
import com.sechat.core.p2p.di.p2pModule
import org.koin.dsl.module

val appModule = module {
    includes(cryptoModule, dataModule, p2pModule)
}
