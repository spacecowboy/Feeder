package com.nononsenseapps.feeder.di

import com.nononsenseapps.feeder.base.KodeinAwareViewModelFactory
import com.nononsenseapps.feeder.base.activityViewModelProvider
import com.nononsenseapps.feeder.base.bindWithKodeinAwareViewModelFactory
import com.nononsenseapps.feeder.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

@ExperimentalCoroutinesApi
val viewModelModule = Kodein.Module(name = "view models") {
    bind<KodeinAwareViewModelFactory>() with singleton { KodeinAwareViewModelFactory(kodein) }
    bindWithKodeinAwareViewModelFactory<FeedViewModel>()
    bindWithKodeinAwareViewModelFactory<FeedItemViewModel>()
    bindWithKodeinAwareViewModelFactory<FeedItemsViewModel>()
    bindWithKodeinAwareViewModelFactory<FeedListViewModel>()
    bindWithKodeinAwareViewModelFactory<SettingsViewModel>()
    bind<EphemeralState>() with activityViewModelProvider()
}
