package com.nononsenseapps.feeder.di

import com.nononsenseapps.feeder.model.ApplicationState
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val stateModule = DI.Module(name = "state objects") {
    bind<ApplicationState>() with singleton { ApplicationState() }
}
