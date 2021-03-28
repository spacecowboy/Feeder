package com.nononsenseapps.feeder.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import com.nononsenseapps.feeder.base.KodeinAwareComponentActivity
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import org.kodein.di.direct
import org.kodein.di.generic.instance

@ExperimentalAnimationApi
class MainActivity : KodeinAwareComponentActivity() {
    private val feedListViewModel: FeedListViewModel by viewModels {
        kodein.direct.instance()
    }
    private val feedItemsViewModel: FeedItemsViewModel by viewModels {
        kodein.direct.instance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FeederTheme {
                FeedScreen(feedListViewModel, feedItemsViewModel)
            }
        }
    }
}
