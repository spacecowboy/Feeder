package com.nononsenseapps.feeder.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.nononsenseapps.feeder.archmodel.Enclosure
import com.nononsenseapps.feeder.ui.compose.feedarticle.DefaultArticleItemKeyHolder
import com.nononsenseapps.feeder.ui.compose.feedarticle.ReaderView
import com.nononsenseapps.feeder.ui.compose.text.htmlFormattedText
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class HtmlRecompositionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testReaderViewDoesNotMixArticleContents() {
        var content: Int by mutableStateOf(0)

        composeTestRule.setContent {
            FeederTheme {
                ReaderView(
                    screenType = ScreenType.SINGLE,
                    wordCount = 1,
                    onEnclosureClick = { },
                    onFeedTitleClick = { },
                    enclosure = Enclosure(),
                    articleTitle = "title",
                    feedTitle = "feed",
                    authorDate = null,
                    image = null,
                    isFeedText = false,
                    articleBody = {
                        if (content == 1) {
                            "<p>one</p><img src=\"http://example.com/image.jpg\" />".byteInputStream().use {
                                htmlFormattedText(
                                    inputStream = it,
                                    baseUrl = "http://example.com",
                                    keyHolder = DefaultArticleItemKeyHolder(1),
                                    onLinkClick = {},
                                )
                            }
                        } else {
                            "<p>zero</p><img src=\"http://example.com/image.jpg\" /><p>ze-ro</p>".byteInputStream().use {
                                htmlFormattedText(
                                    inputStream = it,
                                    baseUrl = "http://example.com",
                                    keyHolder = DefaultArticleItemKeyHolder(0),
                                    onLinkClick = {},
                                )
                            }
                        }
                    },
                )
            }
        }

        runBlocking {
            composeTestRule.awaitIdle()
            composeTestRule.onNodeWithText("zero").assertIsDisplayed()
            composeTestRule.onNodeWithText("ze-ro").assertIsDisplayed()

            content = 1

            composeTestRule.awaitIdle()
            composeTestRule.onNodeWithText("one").assertIsDisplayed()
            composeTestRule.onNodeWithText("zero").assertDoesNotExist()
            composeTestRule.onNodeWithText("ze-ro").assertDoesNotExist()
        }
    }
}
