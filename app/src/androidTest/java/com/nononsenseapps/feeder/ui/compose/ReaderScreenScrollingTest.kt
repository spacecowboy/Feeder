package com.nononsenseapps.feeder.ui.compose

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Ignore
import org.junit.Rule

@Ignore
class ReaderScreenScrollingTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    // createAndroidComposeRule<MainActivity>()

    /**
     * This test doesn't really test anything - it's just good documentation
     * of how to test scrolling
     */
//    @OptIn(ExperimentalTestApi::class)
//    @Test
//    fun scrollingWorks() {
//        composeTestRule.setContent {
//            FeederTheme {
//                withDI {
//                    ReaderScreen(
//                        articleTitle = "Title",
//                        feedDisplayTitle = "Feed",
//                        author = "Author",
//                        pubDate = null,
//                        enclosure = null,
//                        onFetchFullText = {},
//                        onMarkAsUnread = {},
//                        onShare = {},
//                        onOpenInCustomTab = {},
//                        onNavigateUp = {},
//                        ttsPlayer = {},
//                        onTTSStart = {},
//                        onFeedTitleClick = {},
//                        articleBody = {
//                            dummyHtml.byteInputStream().use {
//                                htmlFormattedText(
//                                    it,
//                                    baseUrl = "http://google.com",
//                                    imagePlaceholder = R.drawable.placeholder_image_article_day,
//                                    onLinkClick = {},
//                                )
//                            }
//                        }
//                    )
//                }
//            }
//        }
//
// //        composeTestRule.onRoot(useUnmergedTree = false).printToLog("FeederTest")
//
//        // Does not exist until it is rendered
//        composeTestRule
//            .onNodeWithText(
//                "16 paragraph",
//                substring = true,
//                ignoreCase = true,
//                useUnmergedTree = true
//            )
//            .assertDoesNotExist()
//
//        composeTestRule
//            .onNode(hasScrollToIndexAction())
//            .performGesture {
//                val start = Offset(centerX, top)
//                val end = Offset(centerX, -20_000f)
//                swipe(start, end)
//            }
//
//
// //        composeTestRule.onRoot(useUnmergedTree = false).printToLog("FeederTest")
//
//        // If we scrolled, then this node should exist now
//        composeTestRule
//            .onNodeWithText(
//                "16 paragraph",
//                substring = true,
//                ignoreCase = true,
//                useUnmergedTree = true
//            )
//            .assertExists()
//    }
}

private const val dummyHtml = """
    <p>First paragraph</p>
    <p>First paragraph</p>
    <p>First paragraph</p>
    <p>First paragraph</p>
    <p>First paragraph</p>
    <p>First paragraph</p>
    <p>First paragraph</p>
    <p>First paragraph</p>
    
    <pre><code>First paragraph</code></pre>
    
       <p>Second paragraph</p>
    <p>Second paragraph</p>
    <p>Second paragraph</p>
    <p>Second paragraph</p>
    <p>Second paragraph</p>
    <p>Second paragraph</p>
    <p>Second paragraph</p>
    <p>Second paragraph</p>
    
    <pre><code>Second paragraph</code></pre>
    
        <p>Third paragraph</p>
    <p>Third paragraph</p>
    <p>Third paragraph</p>
    <p>Third paragraph</p>
    <p>Third paragraph</p>
    <p>Third paragraph</p>
    <p>Third paragraph</p>
    <p>Third paragraph</p>
    
    <pre><code>Third paragraph</code></pre>
    
        <p>Fourth paragraph</p>
    <p>Fourth paragraph</p>
    <p>Fourth paragraph</p>
    <p>Fourth paragraph</p>
    <p>Fourth paragraph</p>
    <p>Fourth paragraph</p>
    <p>Fourth paragraph</p>
    <p>Fourth paragraph</p>
    
    <pre><code>Fourth paragraph</code></pre>
    
        <p>5 paragraph</p>
    <p>5 paragraph</p>
    <p>5 paragraph</p>
    <p>5 paragraph</p>
    <p>5 paragraph</p>
    <p>5 paragraph</p>
    <p>5 paragraph</p>
    <p>5 paragraph</p>
    
    <pre><code>5 paragraph</code></pre>
    
        <p>6 paragraph</p>
    <p>6 paragraph</p>
    <p>6 paragraph</p>
    <p>6 paragraph</p>
    <p>6 paragraph</p>
    <p>6 paragraph</p>
    <p>6 paragraph</p>
    <p>6 paragraph</p>
    
    <pre><code>6 paragraph</code></pre>
    
        <p>7 paragraph</p>
    <p>7 paragraph</p>
    <p>7 paragraph</p>
    <p>7 paragraph</p>
    <p>7 paragraph</p>
    <p>7 paragraph</p>
    <p>7 paragraph</p>
    <p>7 paragraph</p>
    
    <pre><code>7 paragraph</code></pre>
    
        <p>8 paragraph</p>
    <p>8 paragraph</p>
    <p>8 paragraph</p>
    <p>8 paragraph</p>
    <p>8 paragraph</p>
    <p>8 paragraph</p>
    <p>8 paragraph</p>
    <p>8 paragraph</p>
    
    <pre><code>8 paragraph</code></pre>
    
        <p>9 paragraph</p>
    <p>9 paragraph</p>
    <p>9 paragraph</p>
    <p>9 paragraph</p>
    <p>9 paragraph</p>
    <p>9 paragraph</p>
    <p>9 paragraph</p>
    <p>9 paragraph</p>
    
    <pre><code>9 paragraph</code></pre>
    
        <p>10 paragraph</p>
    <p>10 paragraph</p>
    <p>10 paragraph</p>
    <p>10 paragraph</p>
    <p>10 paragraph</p>
    <p>10 paragraph</p>
    <p>10 paragraph</p>
    <p>10 paragraph</p>
    
    <pre><code>10 paragraph</code></pre>
    
        <p>11 paragraph</p>
    <p>11 paragraph</p>
    <p>11 paragraph</p>
    <p>11 paragraph</p>
    <p>11 paragraph</p>
    <p>11 paragraph</p>
    <p>11 paragraph</p>
    <p>11 paragraph</p>
    
    <pre><code>11 paragraph</code></pre>
    
        <p>12 paragraph</p>
    <p>12 paragraph</p>
    <p>12 paragraph</p>
    <p>12 paragraph</p>
    <p>12 paragraph</p>
    <p>12 paragraph</p>
    <p>12 paragraph</p>
    <p>12 paragraph</p>
    
    <pre><code>12 paragraph</code></pre>
    
        <p>13 paragraph</p>
    <p>13 paragraph</p>
    <p>13 paragraph</p>
    <p>13 paragraph</p>
    <p>13 paragraph</p>
    <p>13 paragraph</p>
    <p>13 paragraph</p>
    <p>13 paragraph</p>
    
    <pre><code>13 paragraph</code></pre>
    
        <p>14 paragraph</p>
    <p>14 paragraph</p>
    <p>14 paragraph</p>
    <p>14 paragraph</p>
    <p>14 paragraph</p>
    <p>14 paragraph</p>
    <p>14 paragraph</p>
    <p>14 paragraph</p>
    
    <pre><code>14 paragraph</code></pre>
    
        <p>15 paragraph</p>
    <p>15 paragraph</p>
    <p>15 paragraph</p>
    <p>15 paragraph</p>
    <p>15 paragraph</p>
    <p>15 paragraph</p>
    <p>15 paragraph</p>
    <p>15 paragraph</p>
    
    <pre><code>15 paragraph</code></pre>
    
    16 paragraph
"""
