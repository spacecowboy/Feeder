package com.nononsenseapps.feeder.ui.compose.searchfeed

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.modifiers.interceptKey
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.utils.StableHolder
import com.nononsenseapps.feeder.ui.compose.utils.stableListHolderOf
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import java.net.MalformedURLException
import java.net.URL
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Composable
fun SearchFeedScreen(
    onNavigateUp: () -> Unit,
    initialFeedUrl: String? = null,
    searchFeedViewModel: SearchFeedViewModel,
    onClick: (SearchResult) -> Unit,
) {
    Scaffold(
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.navigationBars,
            applyBottom = false,
            applyTop = false,
        ),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.add_feed),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
            )
        }
    ) { padding ->
        SearchFeedView(
            initialFeedUrl = initialFeedUrl ?: "",
            modifier = Modifier.padding(padding),
            searchFeedViewModel = searchFeedViewModel,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchFeedView(
    initialFeedUrl: String = "",
    modifier: Modifier,
    searchFeedViewModel: SearchFeedViewModel,
    onClick: (SearchResult) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    var feedUrl by rememberSaveable {
        mutableStateOf(initialFeedUrl)
    }

    var currentlySearching by rememberSaveable {
        mutableStateOf(false)
    }

    var results by rememberSaveable {
        mutableStateOf(listOf<SearchResult>())
    }
    var errors by rememberSaveable {
        mutableStateOf(listOf<SearchResult>())
    }

    SearchFeedView(
        feedUrl = feedUrl,
        onUrlChanged = {
            feedUrl = it
        },
        onSearch = { url ->
            results = emptyList()
            errors = emptyList()
            currentlySearching = true
            coroutineScope.launch {
                searchFeedViewModel.searchForFeeds(url)
                    .onCompletion {
                        currentlySearching = false
                    }
                    .collect {
                        if (it.isError) {
                            errors = errors + it
                        } else {
                            results = results + it
                        }
                    }
            }
        },
        results = StableHolder(results),
        errors = if (currentlySearching) StableHolder(emptyList()) else StableHolder(errors),
        currentlySearching = currentlySearching,
        modifier = modifier,
        onClick = onClick
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchFeedView(
    feedUrl: String = "",
    onUrlChanged: (String) -> Unit,
    onSearch: (URL) -> Unit,
    results: StableHolder<List<SearchResult>>,
    errors: StableHolder<List<SearchResult>>,
    currentlySearching: Boolean,
    modifier: Modifier,
    onClick: (SearchResult) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val dimens = LocalDimens.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // If screen is opened from intent with pre-filled URL, trigger search directly
    LaunchedEffect(Unit) {
        if (feedUrl.isNotBlank() && isValidUrl(feedUrl)) {
            onSearch(sloppyLinkToStrictURLNoThrows(feedUrl))
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = dimens.margin, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        item {
            TextField(
                value = feedUrl,
                onValueChange = onUrlChanged,
                label = {
                    Text(stringResource(id = R.string.add_feed_search_hint))
                },
                leadingIcon = null,
                trailingIcon = null,
                isError = feedUrl.isNotEmpty() && isNotValidUrl(feedUrl),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (isValidUrl(feedUrl)) {
                            onSearch(sloppyLinkToStrictURLNoThrows(feedUrl))
                            keyboardController?.hide()
                        }
                    }
                ),
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(),
                modifier = Modifier
                    .width(dimens.maxContentWidth)
                    .interceptKey(Key.Enter) {
                        if (isValidUrl(feedUrl)) {
                            onSearch(sloppyLinkToStrictURLNoThrows(feedUrl))
                            keyboardController?.hide()
                        }
                    }
                    .interceptKey(Key.Escape) {
                        focusManager.clearFocus()
                    }
                    .safeSemantics {
                        testTag = "urlField"
                    }
            )
        }
        item {
            OutlinedButton(
                enabled = isValidUrl(feedUrl),
                onClick = {
                    if (isValidUrl(feedUrl)) {
                        onSearch(sloppyLinkToStrictURLNoThrows(feedUrl))
                        focusManager.clearFocus()
                    }
                }
            ) {
                Text(
                    stringResource(android.R.string.search_go)
                )
            }
        }
        if (results.item.isEmpty()) {
            for (error in errors.item) {
                item {
                    val title = stringResource(
                        R.string.failed_to_parse,
                        error.url
                    )
                    SearchResultView(
                        title = title,
                        url = error.url,
                        description = error.description
                    ) {
                    }
                }
            }
        }
        for (result in results.item) {
            item {
                SearchResultView(
                    title = result.title,
                    url = result.url,
                    description = result.description
                ) {
                    onClick(result)
                }
            }
        }
        item {
            AnimatedVisibility(visible = currentlySearching) {
                SearchingIndicator()
            }
        }
    }
}

@Composable
fun SearchingIndicator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .safeSemantics {
                testTag = "searchingIndicator"
            }
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SearchResultView(
    title: String,
    url: String,
    description: String,
    onClick: () -> Unit,
) {
    val dimens = LocalDimens.current
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(dimens.maxContentWidth)
            .clickable(onClick = onClick)
            .safeSemantics {
                testTag = "searchResult"
            }
    ) {
        Text(
            title,
            style = MaterialTheme.typography.subtitle2
        )
        Text(
            url,
            style = MaterialTheme.typography.body2
        )
        Text(
            description,
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview(
    name = "Search with results",
    showBackground = true,
    backgroundColor = 0xffffff,
    showSystemUi = true,
    device = Devices.NEXUS_5,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
fun SearchPreview() {
    SearchFeedView(
        feedUrl = "https://cowboyprogrammer.org",
        currentlySearching = false,
        errors = stableListHolderOf(),
        results = stableListHolderOf(
            SearchResult(
                title = "Atom feed",
                url = "https://cowboyprogrammer.org/atom",
                description = "An atom feed",
                isError = false,
            )
        ),
        modifier = Modifier,
        onClick = {},
        onSearch = {},
        onUrlChanged = {},
    )
}

private fun isValidUrl(url: String): Boolean {
    if (url.isBlank()) {
        return false
    }
    return try {
        try {
            URL(url)
            true
        } catch (_: MalformedURLException) {
            URL("http://$url")
            true
        }
    } catch (e: Exception) {
        false
    }
}

private fun isNotValidUrl(url: String) = !isValidUrl(url)

@Immutable
@Parcelize
data class SearchResult(
    val title: String,
    val url: String,
    val description: String,
    val isError: Boolean,
) : Parcelable
