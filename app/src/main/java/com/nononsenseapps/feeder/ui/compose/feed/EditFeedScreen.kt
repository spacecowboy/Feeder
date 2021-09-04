package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.ui.compose.components.AutoCompleteFoo
import com.nononsenseapps.feeder.ui.compose.components.OkCancelWithContent
import com.nononsenseapps.feeder.ui.compose.settings.GroupTitle
import com.nononsenseapps.feeder.ui.compose.settings.RadioButtonSetting
import com.nononsenseapps.feeder.ui.compose.settings.SwitchSetting
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.keyline1Padding
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_BROWSER
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_READER
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_WEBVIEW
import kotlinx.coroutines.launch
import java.net.URL

@Composable
fun CreateFeedScreen(
    onNavigateUp: () -> Unit,
    feedUrl: String,
    feedTitle: String,
    feedViewModel: FeedViewModel,
    onSaved: (Long) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allTags by feedViewModel.liveAllTags.observeAsState(initial = emptyList())

    EditFeedScreen(
        onNavigateUp = onNavigateUp,
        feed = EditableFeed(
            url = feedUrl,
            title = feedTitle
        ),
        allTags = allTags.filter { it.isNotBlank() },
        onOk = { result ->
            coroutineScope.launch {
                val feedId = feedViewModel.saveAndRequestSync(
                    Feed(
                        title = result.title
                    ).updateFrom(result)
                )
                onSaved(feedId)
            }
        },
        onCancel = {
            onNavigateUp()
        }
    )
}

@Composable
fun EditFeedScreen(
    feed: Feed,
    onNavigateUp: () -> Unit,
    onOk: (Long) -> Unit,
    feedViewModel: FeedViewModel
) {
    val allTags by feedViewModel.liveAllTags.observeAsState(initial = emptyList())

    EditFeedScreen(
        onNavigateUp = onNavigateUp,
        feed = feed.toEditableFeed(),
        allTags = allTags.filter { it.isNotBlank() },
        onOk = { result ->
            feedViewModel.saveInBackgroundAndRequestSync(
                feed.updateFrom(result)
            )

            onOk(feed.id)
        },
        onCancel = {
            onNavigateUp()
        }
    )
}

@Composable
fun EditFeedScreen(
    onNavigateUp: () -> Unit,
    feed: EditableFeed,
    allTags: List<String>,
    onOk: (EditableFeed) -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.edit_feed),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                }
            )
        }
    ) { padding ->
        EditFeedView(
            initialState = feed,
            allTags = allTags,
            onOk = onOk,
            onCancel = onCancel,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun EditFeedView(
    initialState: EditableFeed,
    allTags: List<String>,
    onOk: (EditableFeed) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    // TODO rememberSaveable
    var editableFeed by remember {
        mutableStateOf(initialState)
    }

    var filteredTags by remember {
        mutableStateOf(
            allTags.filter { tag ->
                tag.startsWith(editableFeed.tag, ignoreCase = true)
            }
        )
    }

    val (focusTitle, focusTag) = createRefs()
    val focusManager = LocalFocusManager.current

    var tagHasFocus by remember { mutableStateOf(false) }

    OkCancelWithContent(
        onOk = {
            if (editableFeed.isOkToSave) {
                onOk(editableFeed)
            }
        },
        onCancel = onCancel,
        okEnabled = editableFeed.isOkToSave,
        modifier = modifier
            .padding(horizontal = keyline1Padding)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = editableFeed.url,
                onValueChange = {
                    editableFeed = editableFeed.copy(url = it)
                },
                label = {
                    Text(stringResource(id = R.string.url))
                },
                isError = editableFeed.isNotValidUrl,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusTitle.requestFocus()
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
            )
            AnimatedVisibility(visible = editableFeed.isNotValidUrl) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "Not a valid URL (TODO resource)",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error),
                )
            }
            OutlinedTextField(
                value = editableFeed.customTitle,
                onValueChange = {
                    editableFeed = editableFeed.copy(customTitle = it)
                },
                placeholder = {
                    Text(editableFeed.title)
                },
                label = {
                    Text(stringResource(id = R.string.title))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusTag.requestFocus()
                    }
                ),
                modifier = Modifier
                    .focusOrder(focusTitle)
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
            )

            AutoCompleteFoo(
                displaySuggestions = tagHasFocus,
                suggestions = filteredTags,
                onSuggestionClicked = { tag ->
                    editableFeed = editableFeed.copy(tag = tag)
                    filteredTags = allTags.filter { candidate ->
                        candidate.startsWith(tag, ignoreCase = true)
                    }
                    focusManager.clearFocus()
                },
                suggestionContent = {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(48.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                }
            ) {
                OutlinedTextField(
                    value = editableFeed.tag,
                    onValueChange = {
                        editableFeed = editableFeed.copy(tag = it)
                        filteredTags = allTags.filter { candidate ->
                            candidate.startsWith(it, ignoreCase = true)
                        }
                    },
                    label = {
                        Text(stringResource(id = R.string.tag))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        autoCorrect = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .focusOrder(focusTag)
                        .onFocusChanged {
                            tagHasFocus = it.isFocused
                        }
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                )
            }

            Divider(
                modifier = Modifier.fillMaxWidth()
            )
            SwitchSetting(
                title = stringResource(id = R.string.fetch_full_articles_by_default),
                checked = editableFeed.fullTextByDefault,
                onCheckedChanged = {
                    editableFeed = editableFeed.copy(fullTextByDefault = it)
                },
                icon = null
            )
            SwitchSetting(
                title = stringResource(id = R.string.notify_for_new_items),
                checked = editableFeed.notify,
                onCheckedChanged = {
                    editableFeed = editableFeed.copy(notify = it)
                },
                icon = null
            )
            Divider(
                modifier = Modifier.fillMaxWidth()
            )
            GroupTitle(
                startingSpace = false,
                height = 48.dp
            ) {
                Text(stringResource(id = R.string.open_item_by_default_with))
            }
            RadioButtonSetting(
                title = stringResource(id = R.string.use_app_default),
                selected = editableFeed.isOpenItemWithAppDefault,
                minHeight = 48.dp,
                icon = null,
                onClick = {
                    editableFeed = editableFeed.copy(openArticlesWith = "")
                }
            )
            RadioButtonSetting(
                title = stringResource(id = R.string.open_in_reader),
                selected = editableFeed.isOpenItemWithReader,
                minHeight = 48.dp,
                icon = null,
                onClick = {
                    editableFeed = editableFeed.copy(openArticlesWith = PREF_VAL_OPEN_WITH_READER)
                }
            )
            RadioButtonSetting(
                title = stringResource(id = R.string.open_in_custom_tab),
                selected = editableFeed.isOpenItemWithCustomTab,
                minHeight = 48.dp,
                icon = null,
                onClick = {
                    editableFeed =
                        editableFeed.copy(openArticlesWith = PREF_VAL_OPEN_WITH_CUSTOM_TAB)
                }
            )
            RadioButtonSetting(
                title = stringResource(id = R.string.open_in_default_browser),
                selected = editableFeed.isOpenItemWithBrowser,
                minHeight = 48.dp,
                icon = null,
                onClick = {
                    editableFeed = editableFeed.copy(openArticlesWith = PREF_VAL_OPEN_WITH_BROWSER)
                }
            )
        }
    }
}

@Immutable
data class EditableFeed(
    val url: String,
    val title: String,
    val customTitle: String = "",
    val tag: String = "",
    val fullTextByDefault: Boolean = false,
    val notify: Boolean = false,
    val openArticlesWith: String = "",
) {
    val isOpenItemWithBrowser: Boolean
        get() = openArticlesWith == PREF_VAL_OPEN_WITH_BROWSER

    val isOpenItemWithCustomTab: Boolean
        get() = openArticlesWith == PREF_VAL_OPEN_WITH_CUSTOM_TAB

    val isOpenItemWithReader: Boolean
        get() = openArticlesWith == PREF_VAL_OPEN_WITH_READER

    val isOpenItemWithAppDefault: Boolean
        get() = when (openArticlesWith) {
            PREF_VAL_OPEN_WITH_READER,
            PREF_VAL_OPEN_WITH_WEBVIEW,
            PREF_VAL_OPEN_WITH_BROWSER,
            PREF_VAL_OPEN_WITH_CUSTOM_TAB -> false
            else -> true
        }

    val isNotValidUrl = !isValidUrl

    private val isValidUrl: Boolean
        get() {
            return try {
                URL(url)
                true
            } catch (e: Exception) {
                Log.d("JONAS", e.message, e)
                false
            }
        }

    val isOkToSave: Boolean
        get() = isValidUrl
}

fun Feed.toEditableFeed() =
    EditableFeed(
        url = url.toString(),
        title = title,
        customTitle = customTitle,
        tag = tag,
        fullTextByDefault = fullTextByDefault,
        notify = notify,
        openArticlesWith = openArticlesWith
    )

fun Feed.updateFrom(editableFeed: EditableFeed) =
    copy(
        url = URL(editableFeed.url),
        customTitle = editableFeed.customTitle,
        tag = editableFeed.tag,
        fullTextByDefault = editableFeed.fullTextByDefault,
        notify = editableFeed.notify,
        openArticlesWith = editableFeed.openArticlesWith
    )

@Preview
@Composable
fun EditFeedScreenPreview() {
    FeederTheme {
        EditFeedScreen(
            onNavigateUp = {},
            feed = EditableFeed(
                url = "",
                title = "Foo Bar"
            ),
            allTags = emptyList(),
            onOk = {},
            onCancel = {}
        )
    }
}
