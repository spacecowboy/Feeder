package com.nononsenseapps.feeder.ui.compose.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.MarkAsUnread
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.ui.compose.text.htmlFormattedText
import com.nononsenseapps.feeder.ui.compose.theme.contentHorizontalPadding
import com.nononsenseapps.feeder.ui.compose.theme.upButtonStartPadding
import com.nononsenseapps.feeder.ui.unicodeWrap
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

private val dateTimeFormat =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

@Composable
fun ReaderScreen(
    feedItemViewModel: FeedItemViewModel,
    onNavigateUp: () -> Unit
) {
    val feedItem by feedItemViewModel.currentLiveItem.observeAsState()

    var showMenu by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = feedItem?.feedDisplayTitle ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clickable {
                                onNavigateUp()
                            }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back button",
                            modifier = Modifier
                                .padding(start = upButtonStartPadding)
                        )
                    }
                },
                actions = {
                    // TODO
                    // TODO hide if already fulltext
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Default.Article,
                            contentDescription = "Fetch full article button"
                        )
                    }

                    IconButton(onClick = { /*TODO open in custom tab*/ }) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = "Switch to browser view button"
                        )
                    }

                    // TODO show if not showing fulltext button
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share button"
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Open menu")
                        }
                        // TODO make it wider as necessary
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // TODO show only if not showing action bar button
                            DropdownMenuItem(onClick = {/* TODO */}) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.share))
                            }

                            DropdownMenuItem(onClick = {/* TODO */}) {
                                Icon(
                                    Icons.Default.MarkAsUnread,
                                    contentDescription = "Mark as unread button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.mark_as_unread))
                            }

                            // TODO different icon depending on state
                            DropdownMenuItem(onClick = {/* TODO */}) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Read aloud button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.read_article))
                            }
                        }
                    }

                    /*


                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Open menu")
                        }
                        // TODO make it wider as necessary
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(onClick = onAddFeed) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add feed button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.add_feed))
                            }
                            if (onEditFeed != null) {
                                DropdownMenuItem(onClick = onEditFeed) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit feed button"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.edit_feed))
                                }
                            }
                            DropdownMenuItem(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete feed button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.delete_feed))
                            }
                            Divider()
                            DropdownMenuItem(onClick = { onSettings() }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.action_settings))
                            }
                            Divider()
                            DropdownMenuItem(onClick = { /* TODO Handle send feedback! */ }) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Send bug report button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.send_bug_report))
                            }
                        }
                    }
                     */
                }
            )
        }
    ) { padding ->
//        val articleText by feedItemViewModel.getLiveTextMaybeFull(
//            options = TextOptions(
//                itemId = itemId,
//                maxImageSize = maxImageSize,
//                nightMode = isSystemInDarkTheme() /* TODO should be prefs or something - also in theme */
//            ),
//            urlClickListener = null /* TODO */
//        ).observeAsState(initial = SpannableString("Loading..."))

        val author = feedItem?.author
        val pubDate = feedItem?.pubDate

        // TODO some remember action
        val blob = feedItem?.let { item ->
            // TODO full article fetch action
            when (item.fullTextByDefault) {
                true -> blobFullInputStream(item.id, LocalContext.current.filesDir)
                false -> blobInputStream(item.id, LocalContext.current.filesDir)
            }
        }
        val feedUrl = feedItem?.feedUrl?.toString() ?: ""

        ReaderView(
            modifier = Modifier.padding(padding),
            articleTitle = feedItem?.plainTitle ?: "Could not load",
            feedTitle = feedItem?.feedDisplayTitle ?: "Unknown feed",
            authorDate = when {
                author == null && pubDate != null ->
                    stringResource(
                        R.string.on_date,
                        pubDate.format(dateTimeFormat)
                    )
                author != null && pubDate != null ->
                    stringResource(
                        R.string.by_author_on_date,
                        // Must wrap author in unicode marks to ensure it formats
                        // correctly in RTL
                        LocalContext.current.unicodeWrap(author),
                        pubDate.format(dateTimeFormat)
                    )
                else -> null
            }
        ) {
            blob?.use { inputStream ->
                htmlFormattedText(inputStream = inputStream, baseUrl = feedUrl)
            }
        }
    }
}

@Composable
private fun ReaderView(
    modifier: Modifier = Modifier,
    articleTitle: String = "Article title on top",
    feedTitle: String = "Feed Title is here",
    authorDate: String? = "2018-01-02",
    articleBody: LazyListScope.() -> Unit
) {
//    val scrollState = rememberScrollState()
//    Column(modifier = Modifier.verticalScroll(scrollState)) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = contentHorizontalPadding)
    ) {
        item {
            Text(
                text = articleTitle,
                style = MaterialTheme.typography.h1
            )
            Spacer(modifier = Modifier.height(8.dp))
            // TODO clickable so you can go direct to the feed
            Text(
                text = feedTitle,
                style = MaterialTheme.typography.subtitle1
            )
            if (authorDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = authorDate,
                    style = MaterialTheme.typography.subtitle1
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // TODO if any enclosure then show link or something here

        articleBody()

        item {
            Spacer(modifier = Modifier.height(92.dp))
        }
    }
}
