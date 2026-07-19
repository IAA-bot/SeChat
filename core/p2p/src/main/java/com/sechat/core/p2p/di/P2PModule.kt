package com.sechat.core.p2p.di

import com.sechat.core.p2p.ConnectionManager
import org.koin.dsl.module

val p2pModule = module {
    single { ConnectionManager() }
}
