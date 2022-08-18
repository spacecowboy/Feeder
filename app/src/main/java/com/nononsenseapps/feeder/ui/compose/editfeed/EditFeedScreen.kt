package com.nononsenseapps.feeder.ui.compose.editfeed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_BROWSER
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_READER
import com.nononsenseapps.feeder.ui.compose.components.AutoCompleteFoo
import com.nononsenseapps.feeder.ui.compose.components.OkCancelWithContent
import com.nononsenseapps.feeder.ui.compose.feed.ExplainPermissionDialog
import com.nononsenseapps.feeder.ui.compose.modifiers.interceptKey
import com.nononsenseapps.feeder.ui.compose.settings.GroupTitle
import com.nononsenseapps.feeder.ui.compose.settings.RadioButtonSetting
import com.nononsenseapps.feeder.ui.compose.settings.SwitchSetting
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.rememberApiPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CreateFeedScreen(
    onNavigateUp: () -> Unit,
    createFeedScreenViewModel: CreateFeedScreenViewModel,
    onSaved: (Long) -> Unit,
) {
    val viewState by createFeedScreenViewModel.viewState.collectAsState()

    val notificationsPermissionState = rememberApiPermissionState(
        permission = "android.permission.POST_NOTIFICATIONS",
        minimumApiLevel = 33,
    ) { value ->
        createFeedScreenViewModel.setNotify(value)
    }

    val shouldShowExplanationForPermission by remember {
        derivedStateOf {
            notificationsPermissionState.status.shouldShowRationale
        }
    }

    var permissionDismissed by rememberSaveable {
        mutableStateOf(true)
    }

    EditFeedScreen(
        onNavigateUp = onNavigateUp,
        viewState = viewState,
        setUrl = createFeedScreenViewModel::setUrl,
        setTitle = createFeedScreenViewModel::setTitle,
        setTag = createFeedScreenViewModel::setTag,
        setFullTextByDefault = createFeedScreenViewModel::setFullTextByDefault,
        setNotify = createFeedScreenViewModel::setNotify,
        setArticleOpener = createFeedScreenViewModel::setArticleOpener,
        setAlternateId = createFeedScreenViewModel::setAlternateId,
        showPermissionExplanation = shouldShowExplanationForPermission && !permissionDismissed,
        onPermissionExplanationDismissed = {
            permissionDismissed = true
        },
        onPermissionExplanationOk = {
            notificationsPermissionState.launchPermissionRequest()
        },
        onOk = {
            val feedId = createFeedScreenViewModel.saveAndRequestSync()
            onSaved(feedId)
        },
        onCancel = {
            onNavigateUp()
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditFeedScreen(
    onNavigateUp: () -> Unit,
    onOk: (Long) -> Unit,
    editFeedScreenViewModel: EditFeedScreenViewModel,
) {
    val viewState by editFeedScreenViewModel.viewState.collectAsState()

    val notificationsPermissionState = rememberApiPermissionState(
        permission = "android.permission.POST_NOTIFICATIONS",
        minimumApiLevel = 33,
    ) { value ->
        editFeedScreenViewModel.setNotify(value)
    }

    val shouldShowExplanationForPermission by remember {
        derivedStateOf {
            notificationsPermissionState.status.shouldShowRationale
        }
    }

    var permissionDismissed by rememberSaveable {
        mutableStateOf(true)
    }

    fun setNotify(value: Boolean) {
        if (!value) {
            editFeedScreenViewModel.setNotify(value)
        } else {
            when (notificationsPermissionState.status) {
                is PermissionStatus.Denied -> {
                    if (notificationsPermissionState.status.shouldShowRationale) {
                        // Dialog is shown inside EditFeedScreen with a button
                        permissionDismissed = false
                    } else {
                        notificationsPermissionState.launchPermissionRequest()
                    }
                }
                PermissionStatus.Granted -> editFeedScreenViewModel.setNotify(value)
            }
        }
    }

    EditFeedScreen(
        onNavigateUp = onNavigateUp,
        viewState = viewState,
        setUrl = editFeedScreenViewModel::setUrl,
        setTitle = editFeedScreenViewModel::setTitle,
        setTag = editFeedScreenViewModel::setTag,
        setFullTextByDefault = editFeedScreenViewModel::setFullTextByDefault,
        setNotify = ::setNotify,
        setArticleOpener = editFeedScreenViewModel::setArticleOpener,
        setAlternateId = editFeedScreenViewModel::setAlternateId,
        showPermissionExplanation = shouldShowExplanationForPermission && !permissionDismissed,
        onPermissionExplanationDismissed = {
            permissionDismissed = true
        },
        onPermissionExplanationOk = {
            notificationsPermissionState.launchPermissionRequest()
        },
        onOk = {
            editFeedScreenViewModel.saveInBackgroundAndRequestSync()
            onOk(editFeedScreenViewModel.feedId)
        },
        onCancel = {
            onNavigateUp()
        }
    )
}

@Composable
fun EditFeedScreen(
    onNavigateUp: () -> Unit,
    viewState: EditFeedViewState,
    setUrl: (String) -> Unit,
    setTitle: (String) -> Unit,
    setTag: (String) -> Unit,
    setFullTextByDefault: (Boolean) -> Unit,
    setNotify: (Boolean) -> Unit,
    setArticleOpener: (String) -> Unit,
    setAlternateId: (Boolean) -> Unit,
    showPermissionExplanation: Boolean,
    onPermissionExplanationDismissed: () -> Unit,
    onPermissionExplanationOk: () -> Unit,
    onOk: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        // In case device is rotated to landscape and navigation bar ends up on the side
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
            .asPaddingValues(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.edit_feed),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    .asPaddingValues(),
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
            viewState = viewState,
            setUrl = setUrl,
            setTitle = setTitle,
            setTag = setTag,
            setFullTextByDefault = setFullTextByDefault,
            setNotify = setNotify,
            setArticleOpener = setArticleOpener,
            setAlternateId = setAlternateId,
            onOk = onOk,
            onCancel = onCancel,
            modifier = Modifier.padding(padding)
        )

        if (showPermissionExplanation) {
            ExplainPermissionDialog(
                explanation = R.string.explanation_permission_notifications,
                onDismiss = onPermissionExplanationDismissed,
                onOk = onPermissionExplanationOk,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditFeedView(
    viewState: EditFeedViewState,
    setUrl: (String) -> Unit,
    setTitle: (String) -> Unit,
    setTag: (String) -> Unit,
    setFullTextByDefault: (Boolean) -> Unit,
    setNotify: (Boolean) -> Unit,
    setArticleOpener: (String) -> Unit,
    setAlternateId: (Boolean) -> Unit,
    onOk: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier,
) {
    val filteredTags by remember(viewState.allTags, viewState.tag) {
        derivedStateOf {
            ImmutableHolder(
                viewState.allTags.filter { tag ->
                    tag.isNotBlank() && tag.startsWith(viewState.tag, ignoreCase = true)
                }
            )
        }
    }

    val (focusTitle, focusTag) = createRefs()
    val focusManager = LocalFocusManager.current

    var tagHasFocus by rememberSaveable { mutableStateOf(false) }

    val dimens = LocalDimens.current

    OkCancelWithContent(
        onOk = {
            onOk()
        },
        onCancel = onCancel,
        okEnabled = viewState.isOkToSave,
        modifier = modifier
            .padding(horizontal = LocalDimens.current.margin)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.width(dimens.maxContentWidth)
        ) {
            TextField(
                value = viewState.url,
                onValueChange = setUrl,
                label = {
                    Text(stringResource(id = R.string.url))
                },
                isError = viewState.isNotValidUrl,
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
                    .interceptKey(Key.Enter) {
                        focusTitle.requestFocus()
                    }
                    .interceptKey(Key.Escape) {
                        focusManager.clearFocus()
                    }
            )
            AnimatedVisibility(visible = viewState.isNotValidUrl) {
                Text(
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.invalid_url),
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error),
                )
            }
            OutlinedTextField(
                value = viewState.title,
                onValueChange = setTitle,
                placeholder = {
                    Text(viewState.defaultTitle)
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
                    .focusRequester(focusTitle)
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .interceptKey(Key.Enter) {
                        focusTag.requestFocus()
                    }
                    .interceptKey(Key.Escape) {
                        focusManager.clearFocus()
                    }
            )

            AutoCompleteFoo(
                displaySuggestions = tagHasFocus,
                suggestions = filteredTags,
                onSuggestionClicked = { tag ->
                    setTag(tag)
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
                    value = viewState.tag,
                    onValueChange = setTag,
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
                        .focusRequester(focusTag)
                        .onFocusChanged {
                            tagHasFocus = it.isFocused
                        }
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .interceptKey(Key.Enter) {
                            focusManager.clearFocus()
                        }
                        .interceptKey(Key.Escape) {
                            focusManager.clearFocus()
                        }
                )
            }

            Divider(
                modifier = Modifier.fillMaxWidth()
            )
            SwitchSetting(
                title = stringResource(id = R.string.fetch_full_articles_by_default),
                checked = viewState.fullTextByDefault,
                onCheckedChanged = setFullTextByDefault,
                icon = null
            )
            SwitchSetting(
                title = stringResource(id = R.string.notify_for_new_items),
                checked = viewState.notify,
                onCheckedChanged = setNotify,
                icon = null
            )
            SwitchSetting(
                title = stringResource(id = R.string.generate_extra_unique_ids),
                description = stringResource(id = R.string.only_enable_for_bad_id_feeds),
                checked = viewState.alternateId,
                onCheckedChanged = setAlternateId,
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
                selected = viewState.isOpenItemWithAppDefault,
                minHeight = 48.dp,
                icon = null,
                onClick = {
                    setArticleOpener("")
                }
            )
            RadioButtonSetting(
                title = stringResource(id = R.string.open_in_reader),
                selected = viewState.isOpenItemWithReader,
                minHeight = 48.dp,
                icon = null,
                onClick = {
                    setArticleOpener(PREF_VAL_OPEN_WITH_READER)
                }
            )
            RadioButtonSetting(
                title = stringResource(id = R.string.open_in_custom_tab),
                selected = viewState.isOpenItemWithCustomTab,
                minHeight = 48.dp,
                icon = null,
                onClick = {
                    setArticleOpener(PREF_VAL_OPEN_WITH_CUSTOM_TAB)
                }
            )
            RadioButtonSetting(
                title = stringResource(id = R.string.open_in_default_browser),
                selected = viewState.isOpenItemWithBrowser,
                minHeight = 48.dp,
                icon = null,
                onClick = {
                    setArticleOpener(PREF_VAL_OPEN_WITH_BROWSER)
                }
            )
        }
    }
}

@Preview
@Composable
fun EditFeedScreenPreview() {
    FeederTheme {
        EditFeedScreen(
            onNavigateUp = {},
            onOk = {},
            onCancel = {},
            viewState = EditFeedViewState(),
            setUrl = {},
            setTitle = {},
            setTag = {},
            setFullTextByDefault = {},
            setNotify = {},
            setArticleOpener = {},
            setAlternateId = {},
            showPermissionExplanation = true,
            onPermissionExplanationDismissed = {},
            onPermissionExplanationOk = {},
        )
    }
}
