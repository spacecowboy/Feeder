package com.nononsenseapps.feeder.di

import com.nononsenseapps.feeder.base.DIAwareViewModelFactory
import com.nononsenseapps.feeder.base.activityViewModelProvider
import com.nononsenseapps.feeder.base.bindWithDIAwareViewModelFactory
import com.nononsenseapps.feeder.model.EphemeralState
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.SearchFeedViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val viewModelModule = DI.Module(name = "view models") {
    bind<DIAwareViewModelFactory>() with singleton { DIAwareViewModelFactory(di) }
    bindWithDIAwareViewModelFactory<FeedItemsViewModel>()
    bindWithDIAwareViewModelFactory<FeedListViewModel>()
    bindWithDIAwareViewModelFactory<SettingsViewModel>()
    bindWithDIAwareViewModelFactory<FeedItemViewModel>()
    bindWithDIAwareViewModelFactory<FeedViewModel>()
    bindWithDIAwareViewModelFactory<TextToSpeechViewModel>()
    bindWithDIAwareViewModelFactory<SearchFeedViewModel>()

    bind<EphemeralState>() with activityViewModelProvider()
}
