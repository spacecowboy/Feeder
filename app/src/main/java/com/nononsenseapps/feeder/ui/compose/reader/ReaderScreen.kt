package com.nononsenseapps.feeder.ui.compose.reader

import android.content.Context
import android.util.Log
import androidx.annotation.ColorInt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Enclosure
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.ui.compose.text.withBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import java.util.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

val dateTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

@Composable
fun ReaderView(
    modifier: Modifier = Modifier,
    screenType: ScreenType,
    articleListState: LazyListState = rememberLazyListState(),
    articleTitle: String = "Article title on top",
    feedTitle: String = "Feed Title is here",
    authorDate: String? = "2018-01-02",
    enclosure: Enclosure = Enclosure(),
    onEnclosureClick: () -> Unit,
    onFeedTitleClick: () -> Unit,
    articleBody: LazyListScope.() -> Unit,
) {
    val dimens = LocalDimens.current

    SelectionContainer {
        LazyColumn(
            state = articleListState,
            contentPadding = PaddingValues(
                bottom = 92.dp,
                start = when (screenType) {
                    ScreenType.DUAL -> 0.dp // List items have enough padding
                    ScreenType.SINGLE -> dimens.margin
                },
                end = dimens.margin
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
        ) {
            item {
                val goToFeedLabel = stringResource(R.string.go_to_feed, feedTitle)
                Column(
                    modifier = Modifier
                        .width(dimens.maxContentWidth)
                        .semantics(mergeDescendants = true) {
                            try {
                                customActions = listOf(
                                    CustomAccessibilityAction(goToFeedLabel) {
                                        onFeedTitleClick()
                                        true
                                    }
                                )
                            } catch (e: Exception) {
                                // Observed nullpointer exception when setting customActions
                                // No clue why it could be null
                                Log.e("FeederReaderScreen", "Exception in semantics", e)
                            }
                        }
                ) {
                    withBidiDeterminedLayoutDirection(paragraph = articleTitle) {
                        Text(
                            text = articleTitle,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .width(dimens.maxContentWidth)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    withBidiDeterminedLayoutDirection(paragraph = feedTitle) {
                        Text(
                            text = feedTitle,
                            style = MaterialTheme.typography.titleMedium.merge(LinkTextStyle()),
                            modifier = Modifier
                                .width(dimens.maxContentWidth)
                                .clearAndSetSemantics {
                                    contentDescription = feedTitle
                                }
                                .clickable {
                                    onFeedTitleClick()
                                }
                        )
                    }
                    if (authorDate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            withBidiDeterminedLayoutDirection(paragraph = authorDate) {
                                Text(
                                    text = authorDate,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .width(dimens.maxContentWidth)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (enclosure.present) {
                item {
                    val openLabel = if (enclosure.name.isBlank()) {
                        stringResource(R.string.open_enclosed_media)
                    } else {
                        stringResource(R.string.open_enclosed_media_file, enclosure.name)
                    }
                    Column(
                        modifier = Modifier
                            .width(dimens.maxContentWidth)
                    ) {
                        Text(
                            text = openLabel,
                            style = MaterialTheme.typography.bodyLarge.merge(LinkTextStyle()),
                            modifier = Modifier
                                .clickable {
                                    onEnclosureClick()
                                }
                                .clearAndSetSemantics {
                                    try {
                                        customActions = listOf(
                                            CustomAccessibilityAction(openLabel) {
                                                onEnclosureClick()
                                                true
                                            }
                                        )
                                    } catch (e: Exception) {
                                        // Observed nullpointer exception when setting customActions
                                        // No clue why it could be null
                                        Log.e("FeederReaderScreen", "Exception in semantics", e)
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            articleBody()
        }
    }
}

fun onLinkClick(
    link: String,
    linkOpener: LinkOpener,
    context: Context,
    @ColorInt toolbarColor: Int,
) {
    when (linkOpener) {
        LinkOpener.CUSTOM_TAB -> {
            openLinkInCustomTab(context, link, toolbarColor)
        }
        LinkOpener.DEFAULT_BROWSER -> {
            openLinkInBrowser(context, link)
        }
    }
}
