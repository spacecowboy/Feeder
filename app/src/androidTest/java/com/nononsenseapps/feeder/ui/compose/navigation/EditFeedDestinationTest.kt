package com.nononsenseapps.feeder.ui.compose.navigation

import androidx.navigation.NavController
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class EditFeedDestinationTest {
    @MockK
    private lateinit var navController: NavController

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true, relaxUnitFun = true)
    }

    @Test
    fun editFeedHasCorrectRoute() {
        assertEquals(
            "edit/feed/{feedId}",
            EditFeedDestination.route
        )
    }

    @Test
    fun editFeedNavigate() {
        EditFeedDestination.navigate(
            navController,
            99L
        )

        verify {
            navController.navigate("edit/feed/99")
        }
    }
}
