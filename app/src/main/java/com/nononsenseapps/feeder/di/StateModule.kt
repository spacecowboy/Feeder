package com.nononsenseapps.feeder.di

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val stateModule = DI.Module(name = "state objects") {
    bind<ConflatedBroadcastChannel<Boolean>>(tag = CURRENTLY_SYNCING_STATE) with singleton {
        ConflatedBroadcastChannel(value = false)
    }
}

const val CURRENTLY_SYNCING_STATE = "CurrentlySyncingState"
