package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.ui.compose.components.OkCancelWithContent
import com.nononsenseapps.feeder.ui.compose.components.TextRadioButton
import com.nononsenseapps.feeder.ui.compose.components.TextSwitch
import com.nononsenseapps.feeder.ui.compose.theme.contentHorizontalPadding
import com.nononsenseapps.feeder.ui.compose.theme.upButtonStartPadding
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

    // Only compose this when a value has been retrieved
    EditFeedScreen(
        onNavigateUp = onNavigateUp,
        feed = EditableFeed(
            url = feedUrl,
            title = feedTitle
        ),
        onOk = { result ->
            coroutineScope.launch {
                val feedId = feedViewModel.save(
                    Feed().updateFrom(result)
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
    onNavigateUp: () -> Unit,
    feedViewModel: FeedViewModel
) {
    val feed by feedViewModel.currentLiveFeed.observeAsState()

    feed?.let { feed ->
        // Only compose this when a value has been retrieved
        EditFeedScreen(
            onNavigateUp = onNavigateUp,
            feed = feed.toEditableFeed(),
            onOk = { result ->
                feedViewModel.saveInBackground(
                    feed.updateFrom(result)
                )

                onNavigateUp()
            },
            onCancel = {
                onNavigateUp()
            }
        )
    }
}

@Composable
fun EditFeedScreen(
    onNavigateUp: () -> Unit,
    feed: EditableFeed,
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
                    // todo
                }
            )
        }
    ) { padding ->
        EditFeedView(
            initialState = feed,
            onOk = onOk,
            onCancel = onCancel,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EditFeedView(
    initialState: EditableFeed,
    onOk: (EditableFeed) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    // TODO rememberSaveable
    var editableFeed by remember {
        mutableStateOf(initialState)
    }

    // TODO Focusorder

    OkCancelWithContent(
        onOk = {
            if (editableFeed.isOkToSave) {
                onOk(editableFeed)
            }
        },
        onCancel = onCancel,
        okEnabled = editableFeed.isOkToSave,
        modifier = modifier
            .padding(horizontal = contentHorizontalPadding)
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
                leadingIcon = null,
                trailingIcon = null,
                isError = editableFeed.isNotValidUrl,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        // TODO focus order
                    }
                ),
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                colors = TextFieldDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
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
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )
            OutlinedTextField(
                value = editableFeed.tag,
                onValueChange = {
                    editableFeed = editableFeed.copy(tag = it)
                },
                label = {
                    Text(stringResource(id = R.string.tag))
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )
            Divider(
                modifier = Modifier.fillMaxWidth()
            )
            TextSwitch(
                text = stringResource(id = R.string.fetch_full_articles_by_default),
                selected = editableFeed.fullTextByDefault
            ) {
                editableFeed = editableFeed.copy(fullTextByDefault = it)
            }
            TextSwitch(
                text = stringResource(id = R.string.notify_for_new_items),
                selected = editableFeed.notify
            ) {
                editableFeed = editableFeed.copy(notify = it)
            }
            Divider(
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(id = R.string.open_item_by_default_with)
            )
            TextRadioButton(
                text = stringResource(id = R.string.use_app_default),
                selected = editableFeed.isOpenItemWithAppDefault
            ) {
                editableFeed = editableFeed.copy(openArticlesWith = "")
            }
            TextRadioButton(
                text = stringResource(id = R.string.open_in_reader),
                selected = editableFeed.isOpenItemWithReader
            ) {
                editableFeed = editableFeed.copy(openArticlesWith = PREF_VAL_OPEN_WITH_READER)
            }
            TextRadioButton(
                text = stringResource(id = R.string.open_in_custom_tab),
                selected = editableFeed.isOpenItemWithCustomTab
            ) {
                editableFeed = editableFeed.copy(openArticlesWith = PREF_VAL_OPEN_WITH_CUSTOM_TAB)
            }
            TextRadioButton(
                text = stringResource(id = R.string.open_in_default_browser),
                selected = editableFeed.isOpenItemWithBrowser
            ) {
                editableFeed = editableFeed.copy(openArticlesWith = PREF_VAL_OPEN_WITH_BROWSER)
            }
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
    val openArticlesWith: String = PREF_VAL_OPEN_WITH_READER,
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
    when (this) {
        null -> EditableFeed(
            url = "",
            title = "",
            customTitle = "",
            tag = "",
            fullTextByDefault = false,
            notify = false,
            openArticlesWith = "",

            )
        else -> EditableFeed(
            url = url.toString(),
            title = title,
            customTitle = customTitle,
            tag = tag,
            fullTextByDefault = fullTextByDefault,
            notify = notify,
            openArticlesWith = openArticlesWith
        )
    }.also {
        Log.d("JONAS", "ToEditable: $this, $it")
    }

fun Feed.updateFrom(editableFeed: EditableFeed) =
    copy(
        url = URL(editableFeed.url),
        customTitle = editableFeed.customTitle,
        tag = editableFeed.tag,
        fullTextByDefault = editableFeed.fullTextByDefault,
        notify = editableFeed.notify,
        openArticlesWith = editableFeed.openArticlesWith
    )
