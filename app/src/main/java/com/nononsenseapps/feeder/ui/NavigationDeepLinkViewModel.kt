package com.nononsenseapps.feeder.ui

import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class NavigationDeepLinkViewModel(
    di: DI,
) : DIAwareViewModel(di) {
    private val repository: Repository by instance()

    fun setCurrentFeedAndFolder(
        feedId: Long,
        folderId: Long,
    ) = viewModelScope.launch {
        if ((feedId > ID_UNSET && repository.getFeed(feedId) == null) ||
            (folderId > ID_UNSET && repository.getFolder(folderId) == null)
        ) {
            repository.setCurrentFeedAndFolder(ID_ALL_FEEDS, ID_UNSET)
        } else {
            repository.setCurrentFeedAndFolder(feedId, folderId)
        }
        // Should open feed in portrait
        repository.setIsArticleOpen(false)
    }

    fun setCurrentArticle(itemId: Long) {
        viewModelScope.launch {
            repository.markAsReadAndNotified(itemId)
        }
        repository.setCurrentArticle(itemId)
        // Should open article in portrait
        repository.setIsArticleOpen(true)
    }
}
