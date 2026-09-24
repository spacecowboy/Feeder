package com.nononsenseapps.feeder.ui.compose.folder

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.FeedDisplayItem
import com.nononsenseapps.feeder.db.room.FolderDisplayItem
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.feed.PlainTooltipBox
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.PreviewThemes
import kotlin.collections.emptyList

@Composable
fun EditFoldersScreen(
    onNavigateUp: () -> Unit,
    onNavigateToFeed: (Long) -> Unit,
    viewModel: EditFoldersViewModel,
    modifier: Modifier = Modifier,
) {
    val viewState: EditFoldersViewState by viewModel.viewState.collectAsStateWithLifecycle()
    val foldersAndFeedsList = viewState.foldersAndFeedsList
    val folderRenameDialog by viewModel.currentFolderRenameDialog.collectAsStateWithLifecycle()
    val foldersAndFeedsMoveDialog by viewModel.currentFoldersAndFeedsMoveDialog.collectAsStateWithLifecycle()
    val foldersAndFeedsDeleteDialog by viewModel.currentFoldersAndFeedsDeleteDialog.collectAsStateWithLifecycle()

    EditFoldersScreen(
        onNavigateBack = {
            if (foldersAndFeedsList.breadcrumbs.isNotEmpty()) {
                viewModel.setCurrentFolderId(
                    foldersAndFeedsList.breadcrumbs.getOrNull(foldersAndFeedsList.breadcrumbs.lastIndex - 1)?.id
                        ?: ID_UNSET,
                )
            } else {
                onNavigateUp()
            }
        },
        onFolderSelect = { folderId ->
            viewModel.setCurrentFolderId(folderId)
        },
        onFeedSelect = onNavigateToFeed,
        viewState = viewState,
        foldersAndFeedsList = foldersAndFeedsList,
        folderRenameDialog = folderRenameDialog,
        foldersAndFeedsMoveDialog = foldersAndFeedsMoveDialog,
        foldersAndFeedsDeleteDialog = foldersAndFeedsDeleteDialog,
        onToggleSelectedFolder = { folderId ->
            viewModel.setSelectModeEnabled(true)
            viewModel.toggleSelectedFolder(folderId)
        },
        onToggleSelectedFeed = { feedId ->
            viewModel.setSelectModeEnabled(true)
            viewModel.toggleSelectedFeed(feedId)
        },
        onSelectAll = {
            viewModel.setSelectModeEnabled(true)
            viewModel.setSelectedFolders(
                foldersAndFeedsList.folders.map { it.id },
            )
            viewModel.setSelectedFeeds(
                foldersAndFeedsList.feeds.map { it.id },
            )
        },
        onDeselectAll = {
            viewModel.setSelectModeEnabled(true)
            viewModel.clearSelectedFoldersAndFeeds()
        },
        onClearSelection = {
            viewModel.clearSelectedFoldersAndFeeds()
            viewModel.setSelectModeEnabled(false)
        },
        onAddFolder = { parentId, name ->
            viewModel.addFolder(parentId, name)
        },
        onRenameFolder = { folderId, name ->
            viewModel.renameFolder(folderId, name)
        },
        onMoveFolders = { folderIds, targetFolderId ->
            viewModel.moveFolders(folderIds, targetFolderId)
        },
        onDeleteFolders = { folderIds, feedIds ->
            viewModel.deleteFolders(folderIds = folderIds, feedIds = feedIds)
        },
        onMoveFeeds = { feedIds, targetFolderId ->
            viewModel.moveFeeds(feedIds, targetFolderId)
        },
        onDeleteFeeds = { feedIds ->
            viewModel.deleteFeeds(feedIds)
        },
        onShowFolderAddDialog = {
            viewModel.setAddFolderDialogVisible(true)
        },
        onDismissFolderAddDialog = {
            viewModel.setAddFolderDialogVisible(false)
        },
        onShowFolderRenameDialog = { folderId ->
            viewModel.setRenameFolderDialog(folderId)
        },
        onDismissFolderRenameDialog = {
            viewModel.dismissRenameFolderDialog()
        },
        onShowFoldersMoveDialog = { folders ->
            if (folders.isNotEmpty()) {
                viewModel.setMoveFoldersDialog(folders)
            }
        },
        onShowFeedsMoveDialog = { feeds ->
            if (feeds.isNotEmpty()) {
                viewModel.setMoveFeedsDialog(feeds)
            }
        },
        onDismissMoveDialog = {
            viewModel.dismissMoveDialog()
        },
        onShowFoldersDeleteDialog = { folders ->
            if (folders.isNotEmpty()) {
                viewModel.setDeleteFoldersDialog(folders)
            }
        },
        onShowFeedsDeleteDialog = { feeds ->
            if (feeds.isNotEmpty()) {
                viewModel.setDeleteFeedsDialog(feeds)
            }
        },
        onDismissDeleteDialog = {
            viewModel.dismissDeleteDialog()
        },
        modifier = modifier,
    )
}

@Composable
fun EditFoldersScreen(
    onNavigateBack: () -> Unit,
    onFolderSelect: (Long) -> Unit,
    onFeedSelect: (Long) -> Unit,
    viewState: EditFoldersViewState,
    foldersAndFeedsList: FoldersAndFeedsList,
    folderRenameDialog: FolderRenameDialog,
    foldersAndFeedsMoveDialog: FoldersAndFeedsMoveDialog,
    foldersAndFeedsDeleteDialog: FoldersAndFeedsDeleteDialog,
    onToggleSelectedFolder: (Long) -> Unit,
    onToggleSelectedFeed: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onAddFolder: (Long, String) -> Unit,
    onRenameFolder: (Long, String) -> Unit,
    onMoveFolders: (List<Long>, Long) -> Unit,
    onDeleteFolders: (List<Long>, List<Long>) -> Unit,
    onMoveFeeds: (List<Long>, Long) -> Unit,
    onDeleteFeeds: (List<Long>) -> Unit,
    onShowFolderAddDialog: () -> Unit,
    onDismissFolderAddDialog: () -> Unit,
    onShowFolderRenameDialog: (Long) -> Unit,
    onDismissFolderRenameDialog: () -> Unit,
    onShowFoldersMoveDialog: (List<Long>) -> Unit,
    onShowFeedsMoveDialog: (List<Long>) -> Unit,
    onDismissMoveDialog: () -> Unit,
    onShowFoldersDeleteDialog: (List<Long>) -> Unit,
    onShowFeedsDeleteDialog: (List<Long>) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = foldersAndFeedsList.selectModeEnabled || foldersAndFeedsList.breadcrumbs.isNotEmpty()) {
        if (foldersAndFeedsList.selectModeEnabled) {
            onClearSelection()
        } else {
            onNavigateBack()
        }
    }

    EditFoldersScreen(
        onNavigateBack = onNavigateBack,
        onFolderSelect = onFolderSelect,
        onFeedSelect = onFeedSelect,
        folderId = foldersAndFeedsList.folderId,
        folderName = foldersAndFeedsList.folderName,
        folders = foldersAndFeedsList.folders,
        feeds = foldersAndFeedsList.feeds,
        breadcrumbs = foldersAndFeedsList.breadcrumbs,
        initial = foldersAndFeedsList.initial,
        invalid = foldersAndFeedsList.invalid,
        selectedFolderIds = foldersAndFeedsList.selectedFolderIds,
        selectedFeedIds = foldersAndFeedsList.selectedFeedIds,
        selectModeEnabled = foldersAndFeedsList.selectModeEnabled,
        allSelected = foldersAndFeedsList.allSelected,
        onToggleSelectedFolder = onToggleSelectedFolder,
        onToggleSelectedFeed = onToggleSelectedFeed,
        onSelectAll = onSelectAll,
        onDeselectAll = onDeselectAll,
        onClearSelection = onClearSelection,
        onShowFolderAddDialog = onShowFolderAddDialog,
        onShowFolderRenameDialog = onShowFolderRenameDialog,
        onShowFoldersMoveDialog = onShowFoldersMoveDialog,
        onShowFoldersDeleteDialog = onShowFoldersDeleteDialog,
        onShowFeedsMoveDialog = onShowFeedsMoveDialog,
        onShowFeedsDeleteDialog = onShowFeedsDeleteDialog,
        modifier = modifier,
    )
    if (viewState.addDialogVisible) {
        if (!foldersAndFeedsList.invalid) {
            FolderTextDialog(
                title = R.string.add_folder,
                onDismiss = onDismissFolderAddDialog,
                onOk = { name ->
                    onAddFolder(foldersAndFeedsList.folderId, name)
                    onDismissFolderAddDialog()
                },
                invalidNames =
                    ImmutableHolder(
                        foldersAndFeedsList.folders.map {
                            it.name
                        },
                    ),
            )
        } else {
            onDismissFolderAddDialog()
        }
    }
    if (folderRenameDialog.folderId != null) {
        FolderTextDialog(
            title = R.string.rename_folder,
            onDismiss = onDismissFolderRenameDialog,
            onOk = { name ->
                if (name != folderRenameDialog.folderName) {
                    onRenameFolder(folderRenameDialog.folderId, name)
                }
                onDismissFolderRenameDialog()
            },
            initialFolderName = folderRenameDialog.folderName,
            invalidNames =
                ImmutableHolder(
                    folderRenameDialog.disabledFolderNames,
                ),
        )
    }
    if (foldersAndFeedsMoveDialog.enabled) {
        MoveFolderDialog(
            foldersCount = foldersAndFeedsMoveDialog.folderIds.size,
            feedsCount = foldersAndFeedsMoveDialog.feedIds.size,
            folders = ImmutableHolder(viewState.allFolders),
            disabledFolderIds = ImmutableHolder(foldersAndFeedsMoveDialog.disabledFolderIds),
            initialFolderId = foldersAndFeedsList.folderId,
            onDismiss = {
                onDismissMoveDialog()
            },
            onConfirm = { folderId ->
                if (folderId != foldersAndFeedsList.folderId) {
                    onMoveFolders(foldersAndFeedsMoveDialog.folderIds, folderId)
                    onMoveFeeds(foldersAndFeedsMoveDialog.feedIds, folderId)
                }
                onClearSelection()
                onDismissMoveDialog()
            },
        )
    }
    if (foldersAndFeedsDeleteDialog.enabled) {
        DeleteFolderDialog(
            foldersCount = foldersAndFeedsDeleteDialog.folderIds.size,
            feedsCount = foldersAndFeedsDeleteDialog.feedIds.size,
            feedsInFolders =
                ImmutableHolder(
                    foldersAndFeedsDeleteDialog.feedIdsInFolders.map {
                        DeletableFeed(
                            it.id,
                            it.displayTitle,
                        )
                    },
                ),
            onDismiss = {
                onDismissDeleteDialog()
            },
            onOk = { feedIdsToDelete ->
                onDeleteFolders(foldersAndFeedsDeleteDialog.folderIds, feedIdsToDelete)
                onDeleteFeeds(foldersAndFeedsDeleteDialog.feedIds)
                onClearSelection()
                onDismissDeleteDialog()
            },
        )
    }
}

@Composable
fun EditFoldersScreen(
    onNavigateBack: () -> Unit,
    onFolderSelect: (Long) -> Unit,
    onFeedSelect: (Long) -> Unit,
    folderId: Long,
    folderName: String,
    folders: List<FolderDisplayItem>,
    feeds: List<FeedDisplayItem>,
    breadcrumbs: List<FolderDisplayItem>,
    initial: Boolean,
    invalid: Boolean,
    selectedFolderIds: List<Long>,
    selectedFeedIds: List<Long>,
    selectModeEnabled: Boolean,
    allSelected: Boolean,
    onToggleSelectedFolder: (Long) -> Unit,
    onToggleSelectedFeed: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onShowFolderAddDialog: () -> Unit,
    onShowFolderRenameDialog: (Long) -> Unit,
    onShowFoldersMoveDialog: (List<Long>) -> Unit,
    onShowFoldersDeleteDialog: (List<Long>) -> Unit,
    onShowFeedsMoveDialog: (List<Long>) -> Unit,
    onShowFeedsDeleteDialog: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val breadcrumbsListState = rememberLazyListState()
    var lastBreadcrumbsSize by rememberSaveable {
        mutableStateOf(breadcrumbs.size)
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val itemsCount = folders.size + feeds.size
    val isEmpty = itemsCount == 0
    val selectionCount = selectedFolderIds.size + selectedFeedIds.size
    val titleLabel =
        when {
            folderId > ID_UNSET -> folderName
            else -> stringResource(id = R.string.folders)
        }
    val selectionTitleLabel =
        pluralStringResource(
            id = R.plurals.n_selected,
            selectionCount,
            selectionCount,
        )
    val emptyFolderLabel = R.string.empty_folder
    val invalidFolderLabel = R.string.invalid_folder

    LaunchedEffect(breadcrumbs.size) {
        val currentBreadcrumbsSize = breadcrumbs.size
        if (currentBreadcrumbsSize > lastBreadcrumbsSize && breadcrumbs.isNotEmpty()) {
            breadcrumbsListState.scrollToItem(breadcrumbsListState.layoutInfo.totalItemsCount - 1)
        }
        lastBreadcrumbsSize = currentBreadcrumbsSize
    }

    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        AnimatedContent(
                            targetState =
                                when (selectModeEnabled) {
                                    false -> titleLabel
                                    true -> selectionTitleLabel
                                },
                            transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
                            label = "title",
                        ) { title ->
                            WithBidiDeterminedLayoutDirection(paragraph = title) {
                                Text(
                                    title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        AnimatedContent(
                            targetState = selectModeEnabled,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "navigationIcon",
                        ) { enabled ->
                            when (enabled) {
                                false -> {
                                    IconButton(onClick = onNavigateBack) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.go_back),
                                        )
                                    }
                                }

                                true -> {
                                    IconButton(onClick = onClearSelection) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.clear_selection),
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        AnimatedContent(
                            targetState = selectModeEnabled,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "actions",
                        ) { enabled ->
                            when (enabled) {
                                false -> {
                                    PlainTooltipBox(tooltip = { Text(stringResource(R.string.add_folder)) }) {
                                        IconButton(
                                            onClick = onShowFolderAddDialog,
                                            enabled = !invalid,
                                        ) {
                                            Icon(
                                                Icons.Default.CreateNewFolder,
                                                contentDescription = stringResource(R.string.add_folder),
                                            )
                                        }
                                    }
                                }

                                true -> {
                                    Row {
                                        PlainTooltipBox(tooltip = { Text(stringResource(R.string.action_move)) }) {
                                            IconButton(
                                                onClick = {
                                                    onShowFoldersMoveDialog(selectedFolderIds)
                                                    onShowFeedsMoveDialog(selectedFeedIds)
                                                },
                                                enabled = selectionCount > 0,
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.DriveFileMove,
                                                    contentDescription = stringResource(R.string.action_move),
                                                )
                                            }
                                        }
                                        PlainTooltipBox(tooltip = { Text(stringResource(R.string.action_delete)) }) {
                                            IconButton(
                                                onClick = {
                                                    onShowFoldersDeleteDialog(selectedFolderIds)
                                                    onShowFeedsDeleteDialog(selectedFeedIds)
                                                },
                                                enabled = selectionCount > 0,
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.action_delete),
                                                )
                                            }
                                        }
                                        AnimatedContent(
                                            targetState = allSelected,
                                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                                            label = "selectAction",
                                        ) {
                                            when (it) {
                                                false -> {
                                                    PlainTooltipBox(tooltip = { Text(stringResource(android.R.string.selectAll)) }) {
                                                        IconButton(
                                                            onClick = onSelectAll,
                                                            enabled = itemsCount > 0,
                                                        ) {
                                                            Icon(
                                                                Icons.Default.SelectAll,
                                                                contentDescription = stringResource(android.R.string.selectAll),
                                                            )
                                                        }
                                                    }
                                                }

                                                true -> {
                                                    PlainTooltipBox(tooltip = { Text(stringResource(R.string.clear_selection)) }) {
                                                        IconButton(
                                                            onClick = onDeselectAll,
                                                            enabled = itemsCount > 0,
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Deselect,
                                                                contentDescription = stringResource(R.string.clear_selection),
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors =
                        topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        ),
                )
                LazyRow(
                    state = breadcrumbsListState,
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(minimumTouchSize)
                            .padding(horizontal = LocalDimens.current.margin),
                ) {
                    item(
                        key = "root",
                        contentType = "root",
                    ) {
                        Breadcrumb(
                            onClick = {
                                onFolderSelect(ID_UNSET)
                            },
                            icon = Icons.Outlined.Home,
                            text = stringResource(R.string.root_folder),
                        )
                    }
                    items(
                        count = breadcrumbs.size,
                        key = { breadcrumbs[it].id },
                        contentType = { "breadcrumb" },
                    ) { index ->
                        val folder = breadcrumbs[index]
                        Breadcrumb(
                            onClick = {
                                onFolderSelect(folder.id)
                            },
                            icon = Icons.Outlined.ChevronRight,
                            text = folder.name,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedVisibility(
                enter = fadeIn(),
                exit = fadeOut(),
                visible = invalid && !initial,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .padding(horizontal = LocalDimens.current.margin)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                ) {
                    FolderListPlaceholder(
                        text = invalidFolderLabel,
                        icon = Icons.Outlined.FolderOff,
                        // Offset breadcrumbs row height so the placeholder looks centered relative to the screen
                        modifier = Modifier.offset(y = -minimumTouchSize),
                    )
                }
            }
            AnimatedVisibility(
                enter = fadeIn(),
                exit = fadeOut(),
                visible = isEmpty && !invalid && !initial,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .padding(horizontal = LocalDimens.current.margin)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                ) {
                    FolderListPlaceholder(
                        text = emptyFolderLabel,
                        icon = Icons.Outlined.Folder,
                        // Offset breadcrumbs row height so the placeholder looks centered relative to the screen
                        modifier = Modifier.offset(y = -minimumTouchSize),
                    )
                }
            }
            AnimatedVisibility(
                enter = fadeIn(),
                exit = fadeOut(),
                visible = !isEmpty && !invalid && !initial,
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = LocalDimens.current.margin),
                ) {
                    item(
                        key = "SpacerScrollTrick",
                        contentType = "SpacerScrollTrick",
                    ) {
                        Spacer(modifier = Modifier.fillMaxWidth())
                    }
                    items(
                        folders.size,
                        key = { "folder_${folders[it].id}" },
                        contentType = { "folder" },
                    ) { index ->
                        val folder = folders[index]
                        FolderRow(
                            folderName = folder.name,
                            selectModeEnabled = selectModeEnabled,
                            isSelected = selectedFolderIds.contains(folder.id),
                            onClick = {
                                onFolderSelect(folder.id)
                            },
                            onToggleSelect = {
                                onToggleSelectedFolder(folder.id)
                            },
                            onRename = {
                                onShowFolderRenameDialog(folder.id)
                            },
                            onMove = {
                                onShowFoldersMoveDialog(listOf(folder.id))
                            },
                            onDelete = {
                                onShowFoldersDeleteDialog(listOf(folder.id))
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                    items(
                        feeds.size,
                        key = { "feed_${feeds[it].id}" },
                        contentType = { "feed" },
                    ) { index ->
                        val feed = feeds[index]
                        FeedRow(
                            displayTitle = feed.displayTitle,
                            imageUrl = feed.imageUrl,
                            selectModeEnabled = selectModeEnabled,
                            isSelected = selectedFeedIds.contains(feed.id),
                            onClick = {
                                onFeedSelect(feed.id)
                            },
                            onToggleSelect = {
                                onToggleSelectedFeed(feed.id)
                            },
                            onMove = {
                                onShowFeedsMoveDialog(listOf(feed.id))
                            },
                            onDelete = {
                                onShowFeedsDeleteDialog(listOf(feed.id))
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Breadcrumb(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .padding(6.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(12.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
    }
}

@Composable
fun FolderListPlaceholder(
    @StringRes text: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Icon(
            icon,
            contentDescription = stringResource(text),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
@PreviewThemes
private fun Preview() {
    PreviewTheme {
        EditFoldersScreen(
            onNavigateBack = {},
            onFolderSelect = {},
            onFeedSelect = {},
            folderId = 3L,
            folderName = "Current folder",
            folders =
                listOf(
                    FolderDisplayItem(id = 10L, name = "Folder A"),
                    FolderDisplayItem(id = 11L, name = "Folder B"),
                    FolderDisplayItem(id = 12L, name = "Folder C"),
                ),
            feeds =
                listOf(
                    FeedDisplayItem(id = 1L, displayTitle = "Feed 1", imageUrl = null),
                    FeedDisplayItem(id = 2L, displayTitle = "Feed 2", imageUrl = null),
                    FeedDisplayItem(id = 3L, displayTitle = "Feed 3", imageUrl = null),
                ),
            breadcrumbs =
                listOf(
                    FolderDisplayItem(id = 1L, name = "Path to"),
                    FolderDisplayItem(id = 3L, name = "Current folder"),
                ),
            initial = false,
            invalid = false,
            selectedFolderIds = emptyList(),
            selectedFeedIds = emptyList(),
            selectModeEnabled = false,
            allSelected = false,
            onToggleSelectedFolder = {},
            onToggleSelectedFeed = {},
            onSelectAll = {},
            onDeselectAll = {},
            onClearSelection = {},
            onShowFolderAddDialog = {},
            onShowFolderRenameDialog = {},
            onShowFoldersMoveDialog = {},
            onShowFoldersDeleteDialog = {},
            onShowFeedsMoveDialog = {},
            onShowFeedsDeleteDialog = {},
        )
    }
}

@Composable
@PreviewThemes
private fun PreviewSelected() {
    PreviewTheme {
        EditFoldersScreen(
            onNavigateBack = {},
            onFolderSelect = {},
            onFeedSelect = {},
            folderId = 3L,
            folderName = "Current folder",
            folders =
                listOf(
                    FolderDisplayItem(id = 10L, name = "Folder A"),
                    FolderDisplayItem(id = 11L, name = "Folder B"),
                    FolderDisplayItem(id = 12L, name = "Folder C"),
                ),
            feeds =
                listOf(
                    FeedDisplayItem(id = 1L, displayTitle = "Feed 1", imageUrl = null),
                    FeedDisplayItem(id = 2L, displayTitle = "Feed 2", imageUrl = null),
                    FeedDisplayItem(id = 3L, displayTitle = "Feed 3", imageUrl = null),
                ),
            breadcrumbs =
                listOf(
                    FolderDisplayItem(id = 1L, name = "Path to"),
                    FolderDisplayItem(id = 3L, name = "Current folder"),
                ),
            initial = false,
            invalid = false,
            selectedFolderIds = listOf(11L),
            selectedFeedIds = listOf(1L, 3L),
            selectModeEnabled = true,
            allSelected = false,
            onToggleSelectedFolder = {},
            onToggleSelectedFeed = {},
            onSelectAll = {},
            onDeselectAll = {},
            onClearSelection = {},
            onShowFolderAddDialog = {},
            onShowFolderRenameDialog = {},
            onShowFoldersMoveDialog = {},
            onShowFoldersDeleteDialog = {},
            onShowFeedsMoveDialog = {},
            onShowFeedsDeleteDialog = {},
        )
    }
}

@Composable
@Preview
private fun PreviewEmpty() {
    PreviewTheme {
        EditFoldersScreen(
            onNavigateBack = {},
            onFolderSelect = {},
            onFeedSelect = {},
            folderId = ID_UNSET,
            folderName = "",
            folders = emptyList(),
            feeds = emptyList(),
            breadcrumbs = emptyList(),
            initial = false,
            invalid = false,
            selectedFolderIds = emptyList(),
            selectedFeedIds = emptyList(),
            selectModeEnabled = false,
            allSelected = false,
            onToggleSelectedFolder = {},
            onToggleSelectedFeed = {},
            onSelectAll = {},
            onDeselectAll = {},
            onClearSelection = {},
            onShowFolderAddDialog = {},
            onShowFolderRenameDialog = {},
            onShowFoldersMoveDialog = {},
            onShowFoldersDeleteDialog = {},
            onShowFeedsMoveDialog = {},
            onShowFeedsDeleteDialog = {},
        )
    }
}

@Composable
@Preview
private fun PreviewInvalid() {
    PreviewTheme {
        EditFoldersScreen(
            onNavigateBack = {},
            onFolderSelect = {},
            onFeedSelect = {},
            folderId = 99L,
            folderName = "Some folder that does not exist",
            folders = emptyList(),
            feeds = emptyList(),
            breadcrumbs = emptyList(),
            initial = false,
            invalid = true,
            selectedFolderIds = emptyList(),
            selectedFeedIds = emptyList(),
            selectModeEnabled = false,
            allSelected = false,
            onToggleSelectedFolder = {},
            onToggleSelectedFeed = {},
            onSelectAll = {},
            onDeselectAll = {},
            onClearSelection = {},
            onShowFolderAddDialog = {},
            onShowFolderRenameDialog = {},
            onShowFoldersMoveDialog = {},
            onShowFoldersDeleteDialog = {},
            onShowFeedsMoveDialog = {},
            onShowFeedsDeleteDialog = {},
        )
    }
}
