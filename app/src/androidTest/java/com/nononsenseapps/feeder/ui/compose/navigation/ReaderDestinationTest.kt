package com.nononsenseapps.feeder.ui.compose.navigation

import androidx.navigation.NavController
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class ReaderDestinationTest {
    @MockK
    private lateinit var navController: NavController

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true, relaxUnitFun = true)
    }

    @Test
    fun readerHasCorrectRoute() {
        assertEquals(
            "reader/{itemId}",
            ReaderDestination.route
        )
    }

    @Test
    fun readerHasCorrectDeeplinks() {
        assertEquals(
            listOf(
                "$DEEP_LINK_BASE_URI/article/{itemId}"
            ),
            ReaderDestination.deepLinks.map { it.uriPattern }
        )
    }

    @Test
    fun readerNavigate() {
        ReaderDestination.navigate(
            navController,
            55L
        )

        verify {
            navController.navigate("reader/55")
        }
    }
}
