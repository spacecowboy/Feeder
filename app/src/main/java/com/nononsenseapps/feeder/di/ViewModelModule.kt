package com.nononsenseapps.feeder.di

import com.nononsenseapps.feeder.base.bindWithDIAwareViewModelFactory
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.SearchFeedViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import org.kodein.di.DI

val viewModelModule = DI.Module(name = "view models") {
    bindWithDIAwareViewModelFactory<FeedItemsViewModel>()
    bindWithDIAwareViewModelFactory<FeedListViewModel>()
    bindWithDIAwareViewModelFactory<SettingsViewModel>()
    bindWithDIAwareViewModelFactory<FeedItemViewModel>()
    bindWithDIAwareViewModelFactory<FeedViewModel>()
    bindWithDIAwareViewModelFactory<TextToSpeechViewModel>()
    bindWithDIAwareViewModelFactory<SearchFeedViewModel>()
}
