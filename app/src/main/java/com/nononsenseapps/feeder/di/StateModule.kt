package com.nononsenseapps.feeder.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

@ExperimentalCoroutinesApi
val stateModule = Kodein.Module(name = "state objects") {
    bind<ConflatedBroadcastChannel<Boolean>>(tag = CURRENTLY_SYNCING_STATE) with singleton {
        ConflatedBroadcastChannel(value = false)
    }
}

const val CURRENTLY_SYNCING_STATE = "CurrentlySyncingState"
