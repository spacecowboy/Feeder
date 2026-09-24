package com.nononsenseapps.feeder.widget

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.FeedUnreadCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class FeedWidgetSettingsActivityViewModel(
    di: DI,
) : DIAwareViewModel(di) {
    val repository by instance<Repository>()
    val pagedNavDrawerItems: Flow<PagingData<FeedUnreadCount>> =
        repository
            .getPagedNavDrawerItems()
            .cachedIn(viewModelScope)

    fun toggleFolderExpansion(folderId: Long) =
        viewModelScope.launch {
            repository.toggleFolderExpansion(folderId)
        }

    fun selectFeedForWidget(
        feedId: Long,
        folderId: Long,
    ) {
        repository.setCurrentWidgetFeedAndFolder(feedId, folderId)
    }
}
