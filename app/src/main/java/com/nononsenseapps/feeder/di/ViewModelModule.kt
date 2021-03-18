package com.nononsenseapps.feeder.di

import com.nononsenseapps.feeder.base.KodeinAwareViewModelFactory
import com.nononsenseapps.feeder.base.activityViewModelProvider
import com.nononsenseapps.feeder.base.bindWithKodeinAwareViewModelFactory
import com.nononsenseapps.feeder.model.EphemeralState
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

@FlowPreview
@ExperimentalCoroutinesApi
val viewModelModule = Kodein.Module(name = "view models") {
    bind<KodeinAwareViewModelFactory>() with singleton { KodeinAwareViewModelFactory(kodein) }
    bindWithKodeinAwareViewModelFactory<FeedItemsViewModel>()
    bindWithKodeinAwareViewModelFactory<FeedListViewModel>()
    bindWithKodeinAwareViewModelFactory<SettingsViewModel>()
    bindWithKodeinAwareViewModelFactory<FeedItemViewModel>()
    bindWithKodeinAwareViewModelFactory<FeedViewModel>()

    bind<EphemeralState>() with activityViewModelProvider()
}
