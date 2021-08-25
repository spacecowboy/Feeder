package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.SearchFeedViewModel
import com.nononsenseapps.feeder.ui.compose.theme.keyline1Padding
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL

@Composable
fun SearchFeedScreen(
    onNavigateUp: () -> Unit,
    initialFeedUrl: String? = null,
    searchFeedViewModel: SearchFeedViewModel,
    onClick: (SearchResult) -> Unit
) {
    Scaffold(
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
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back button"
                        )
                    }
                },
                actions = {
                    // todo
                }
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
    onClick: (SearchResult) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var feedUrl by rememberSaveable {
        mutableStateOf(initialFeedUrl)
    }

    var currentlySearching by rememberSaveable {
        mutableStateOf(false)
    }

    // TODO remember saveable?
    val results = remember {
        mutableStateListOf<SearchResult>()
    }
    // TODO remember saveable?
    val errors = remember {
        mutableStateListOf<SearchResult>()
    }

    SearchFeedView(
        feedUrl = feedUrl,
        onUrlChanged = {
            feedUrl = it
        },
        onSearch = { url ->
            results.clear()
            errors.clear()
            currentlySearching = true
            coroutineScope.launch {
                searchFeedViewModel.searchForFeeds(url)
                    .onCompletion {
                        currentlySearching = false
                    }
                    .collect {
                        if (it.isError) {
                            errors.add(it)
                        } else {
                            results.add(it)
                        }
                    }
            }
        },
        results = results,
        errors = if (currentlySearching) emptyList() else errors,
        currentlySearching = currentlySearching,
        modifier = modifier,
        onClick = onClick
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchFeedView(
    feedUrl: String = "",
    onUrlChanged: (String) -> Unit,
    onSearch: (URL) -> Unit,
    results: List<SearchResult>,
    errors: List<SearchResult>,
    currentlySearching: Boolean,
    modifier: Modifier,
    onClick: (SearchResult) -> Unit
) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(horizontal = keyline1Padding, vertical = 8.dp)
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
                            focusManager.clearFocus()
                        }
                    }
                ),
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        if (results.isEmpty()) {
            for (error in errors) {
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
        for (result in results) {
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
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SearchResultView(
    title: String,
    url: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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

//@Preview(showBackground = true)
//@Composable
//fun SearchFeedScreenPreview() {
//    FeederTheme {
//        SearchFeedScreen(
//            onNavigateUp = {}
//        )
//    }
//}

private fun isValidUrl(url: String): Boolean {
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
data class SearchResult(
    val title: String,
    val url: String,
    val description: String,
    val isError: Boolean
)
