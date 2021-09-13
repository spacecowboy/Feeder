package com.nononsenseapps.feeder.di

import com.nononsenseapps.feeder.base.bindWithActivityViewModelScope
import com.nononsenseapps.feeder.base.bindWithComposableViewModelScope
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.SearchFeedViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import org.kodein.di.DI

val viewModelModule = DI.Module(name = "view models") {
    bindWithComposableViewModelScope<FeedItemsViewModel>()
    bindWithComposableViewModelScope<FeedListViewModel>()
    bindWithActivityViewModelScope<SettingsViewModel>()
    bindWithComposableViewModelScope<FeedItemViewModel>()
    bindWithComposableViewModelScope<FeedViewModel>()
    bindWithComposableViewModelScope<TextToSpeechViewModel>()
    bindWithComposableViewModelScope<SearchFeedViewModel>()
}
