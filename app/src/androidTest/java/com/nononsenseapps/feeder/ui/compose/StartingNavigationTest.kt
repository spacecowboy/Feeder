package com.nononsenseapps.feeder.ui.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import com.nononsenseapps.feeder.ui.MainActivity
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import kotlin.test.fail
import org.junit.Rule
import org.junit.Test
import org.kodein.di.compose.withDI

class StartingNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun backWillExitApp() {
        composeTestRule.setContent {
            FeederTheme {
                withDI {
                    composeTestRule.activity.appContent()
                }
            }
        }

        try {
            Espresso.pressBack()
            fail("Expected activity to be destroyed")
        } catch (_: NoActivityResumedException) {
            // Success, app is destroyed
        }
    }
}
