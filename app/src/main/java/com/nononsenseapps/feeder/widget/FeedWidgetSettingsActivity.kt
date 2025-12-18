package com.nononsenseapps.feeder.widget

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.ui.compose.navdrawer.ListOfFeedsAndTags
import com.nononsenseapps.feeder.ui.compose.utils.withAllProviders
import kotlinx.coroutines.launch
import org.kodein.di.instance

class FeedWidgetSettingsActivity : DIAwareComponentActivity() {
    val viewModel: FeedWidgetSettingsActivityViewModel by instance(arg = this)

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            withAllProviders {
                Surface {
                    val feedsAndTags = viewModel.pagedNavDrawerItems.collectAsLazyPagingItems()
                    ListOfFeedsAndTags(
                        feedsAndTags = feedsAndTags,
                        { tag ->
                            viewModel.toggleTagExpansion(tag)
                        },
                        rememberLazyListState(),
                        onItemClick = {
                            viewModel.selectFeedForWidget(it.id, it.tag)
                            viewModel.viewModelScope.launch {
                                val manager = GlanceAppWidgetManager(this@FeedWidgetSettingsActivity)
                                val widget = FeedWidget()
                                val glanceIds = manager.getGlanceIds(widget.javaClass)
                                glanceIds.forEach { glanceId ->
                                    widget.updateAll(this@FeedWidgetSettingsActivity)
                                }
                            }
                            finish()
                        },
                    )
                }
            }
        }
    }
}
