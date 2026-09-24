package com.nononsenseapps.feeder.ui.compose.navigation

import androidx.navigation.NavController
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

@Ignore
class FeedDestinationTest {
    @MockK
    private lateinit var navController: NavController

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true, relaxUnitFun = true)
    }

    @Test
    fun feedHasCorrectRoute() {
        assertEquals(
            "feed?feedId={feedId}&folderId={folderId}",
            FeedDestination.route,
        )
    }

    @Test
    fun feedHasCorrectDeeplinks() {
        assertEquals(
            listOf(
                "$DEEP_LINK_BASE_URI/feed?feedId={feedId}&folderId={folderId}",
            ),
            FeedDestination.deepLinks.map { it.uriPattern },
        )
    }

    @Test
    fun feedNavigateDefaults() {
        FeedDestination.navigate(
            navController,
        )

        verify {
            navController.navigate("feed")
        }
    }

    @Test
    fun feedNavigateFeed() {
        FeedDestination.navigate(
            navController,
            feedId = 6L,
        )

        verify {
            navController.navigate("feed?feedId=6")
        }
    }

    @Test
    fun feedNavigateFolder() {
        FeedDestination.navigate(
            navController,
            folderId = 3L,
        )

        verify {
            navController.navigate("feed?folderId=3")
        }
    }

    @Test
    fun feedNavigateFeedAndFolder() {
        FeedDestination.navigate(
            navController,
            feedId = ID_ALL_FEEDS,
            folderId = 3L,
        )

        verify {
            navController.navigate("feed?feedId=-10&folderId=3")
        }
    }
}
