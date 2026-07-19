package com.sechat.app

import org.koin.dsl.module

val appModule = module {
    single { SeChatApplication.CHAT_CHANNEL_ID }
}
