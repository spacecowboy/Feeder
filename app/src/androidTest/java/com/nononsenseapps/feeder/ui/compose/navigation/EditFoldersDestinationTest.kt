package com.nononsenseapps.feeder.ui.compose.navigation

import androidx.navigation.NavController
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

@Ignore
class EditFoldersDestinationTest {
    @MockK
    private lateinit var navController: NavController

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true, relaxUnitFun = true)
    }

    @Test
    fun editFoldersHasCorrectRoute() {
        assertEquals(
            "folder/{folderId}",
            EditFoldersDestination.route,
        )
    }

    @Test
    fun editFoldersNavigateDefaults() {
        EditFoldersDestination.navigate(
            navController,
        )

        verify {
            navController.navigate("folder/0")
        }
    }

    @Test
    fun editFoldersNavigateFolder() {
        EditFoldersDestination.navigate(
            navController,
            2L,
        )

        verify {
            navController.navigate("folder/2")
        }
    }
}
