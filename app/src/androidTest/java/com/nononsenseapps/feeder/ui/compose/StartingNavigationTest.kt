package com.nononsenseapps.feeder.ui.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.nononsenseapps.feeder.ui.MainActivity
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.robots.feedScreen
import kotlin.test.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.compose.withDI

class StartingNavigationTest : BaseComposeTest {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent {
            FeederTheme {
                withDI {
                    composeTestRule.activity.appContent()
                }
            }
        }
    }

    @Test
    fun backWillExitApp() {
        feedScreen {
            pressBackButton()
            assertFalse {
                isAppRunning
            }
        }
    }
}
