package com.sechat.core.data.di

import com.sechat.core.data.ContactRepository
import com.sechat.core.data.MessageRepository
import com.sechat.core.data.SechatDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule =
    module {
        single { SechatDatabase.create(androidContext()) }
        single { get<SechatDatabase>().contactDao() }
        single { get<SechatDatabase>().messageDao() }
        single { ContactRepository(get()) }
        single { MessageRepository(get()) }
    }
