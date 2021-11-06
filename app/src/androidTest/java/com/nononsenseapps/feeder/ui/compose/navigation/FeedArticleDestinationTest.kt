package com.nononsenseapps.feeder.ui.compose.navigation

import androidx.navigation.NavController
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class FeedArticleDestinationTest {
    @MockK
    private lateinit var navController: NavController

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true, relaxUnitFun = true)
    }

    @Test
    fun feedHasCorrectRoute() {
        assertEquals(
            "feedarticle",
            FeedArticleDestination.route
        )
    }
}
