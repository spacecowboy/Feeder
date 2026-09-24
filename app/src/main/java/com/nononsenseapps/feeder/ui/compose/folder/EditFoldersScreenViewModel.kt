package com.nononsenseapps.feeder.ui.compose.folder

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDisplayItem
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.FolderDisplayItem
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.SelectableFolder
import com.nononsenseapps.feeder.util.ToastMaker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.getValue

class EditFoldersViewModel(
    di: DI,
    private val state: SavedStateHandle,
) : DIAwareViewModel(di) {
    private val repository: Repository by instance()
    private val context: Application by instance()
    private val toastMaker: ToastMaker by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()

    private val currentFolderId: MutableStateFlow<Long> = MutableStateFlow(state["folderId"] ?: ID_UNSET)

    fun setCurrentFolderId(folderId: Long) {
        if (folderId != currentFolderId.value) {
            clearSelectedFoldersAndFeeds()
            setSelectModeEnabled(false)
            setAddFolderDialogVisible(false)
            dismissRenameFolderDialog()
            dismissMoveDialog()
            dismissDeleteDialog()
            state["currentFolderId"] = folderId
            currentFolderId.update { folderId }
        }
    }

    private val selectModeEnabled = MutableStateFlow(false)

    fun setSelectModeEnabled(enabled: Boolean) {
        if (enabled != selectModeEnabled.value) {
            state["selectModeEnabled"] = enabled
            selectModeEnabled.update { enabled }
        }
    }

    private val selectedFolderIds: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList())

    fun setSelectedFolders(folderIds: List<Long>) {
        state["selectedFolderIds"] = folderIds
        selectedFolderIds.update { folderIds }
    }

    fun toggleSelectedFolder(folderId: Long) {
        setSelectedFolders(
            selectedFolderIds.value.let { selectedFolderIds ->
                if (folderId in selectedFolderIds) {
                    selectedFolderIds - folderId
                } else {
                    selectedFolderIds + folderId
                }
            },
        )
    }

    private val selectedFeedIds: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList())

    fun setSelectedFeeds(feedIds: List<Long>) {
        state["selectedFeedIds"] = feedIds
        selectedFeedIds.update { feedIds }
    }

    fun toggleSelectedFeed(feedId: Long) {
        setSelectedFeeds(
            selectedFeedIds.value.let { selectedFeedIds ->
                if (feedId in selectedFeedIds) {
                    selectedFeedIds - feedId
                } else {
                    selectedFeedIds + feedId
                }
            },
        )
    }

    fun clearSelectedFoldersAndFeeds() {
        setSelectedFolders(emptyList())
        setSelectedFeeds(emptyList())
    }

    private val addDialogVisible = MutableStateFlow(false)

    fun setAddFolderDialogVisible(visible: Boolean) {
        state["addDialogVisible"] = visible
        addDialogVisible.update { visible }
    }

    private val folderIdToRename = MutableStateFlow<Long?>(null)

    fun setRenameFolderDialog(folderId: Long) {
        state["folderIdToRename"] = folderId
        folderIdToRename.update { folderId }
    }

    fun dismissRenameFolderDialog() {
        state["folderIdToRename"] = null
        folderIdToRename.update { null }
    }

    private val folderIdsToMove = MutableStateFlow<List<Long>>(emptyList())

    fun setMoveFoldersDialog(folders: List<Long>) {
        state["folderIdsToMove"] = folders
        folderIdsToMove.update { folders }
    }

    private val feedIdsToMove = MutableStateFlow<List<Long>>(emptyList())

    fun setMoveFeedsDialog(feeds: List<Long>) {
        state["feedIdsToMove"] = feeds
        feedIdsToMove.update { feeds }
    }

    fun dismissMoveDialog() {
        setMoveFoldersDialog(emptyList())
        setMoveFeedsDialog(emptyList())
    }

    private val folderIdsToDelete = MutableStateFlow<List<Long>>(emptyList())

    fun setDeleteFoldersDialog(folders: List<Long>) {
        state["folderIdsToDelete"] = folders
        folderIdsToDelete.update { folders }
    }

    private val feedIdsToDelete = MutableStateFlow<List<Long>>(emptyList())

    fun setDeleteFeedsDialog(feeds: List<Long>) {
        state["feedIdsToDelete"] = feeds
        feedIdsToDelete.update { feeds }
    }

    fun dismissDeleteDialog() {
        setDeleteFoldersDialog(emptyList())
        setDeleteFeedsDialog(emptyList())
    }

    fun addFolder(
        parentId: Long,
        name: String,
    ) = applicationCoroutineScope.launch {
        val folderName = name.trim()
        if (folderName.isNotEmpty()) {
            val folderId = repository.insertOrIgnoreFolder(name, parentId)
            if (folderId == null) {
                toastMaker.makeToast(R.string.folder_already_exists)
            }
        } else {
            toastMaker.makeToast(R.string.folder_name_empty)
        }
    }

    fun renameFolder(
        folderId: Long,
        name: String,
    ) = applicationCoroutineScope.launch {
        val nameTrimmed = name.trim()
        if (nameTrimmed.isNotEmpty()) {
            val count = repository.renameFolder(folderId, nameTrimmed)
            if (count == 0) {
                toastMaker.makeToast(R.string.folder_already_exists)
            }
        } else {
            toastMaker.makeToast(R.string.folder_name_empty)
        }
    }

    fun moveFolders(
        folderIds: List<Long>,
        targetFolderId: Long,
    ) = applicationCoroutineScope.launch {
        var count = 0
        // Move each folder individually to avoid everything rolling back on constraint violation
        folderIds.forEach { folderId ->
            try {
                count += repository.moveFolder(folderId, targetFolderId)
            } catch (_: SQLiteConstraintException) {
            }
        }
        val failedMoves = folderIds.size - count
        if (failedMoves > 0) {
            val text =
                context.getString(
                    R.string.failed_to_move_x_folders,
                    context.resources.getQuantityString(
                        R.plurals.n_folders,
                        failedMoves,
                        failedMoves,
                    ),
                )
            toastMaker.makeToast(text)
        }
    }

    fun deleteFolders(
        folderIds: List<Long>,
        feedIds: List<Long> = emptyList(),
    ) = applicationCoroutineScope.launch {
        var failedDeletes = 0
        if (feedIds.isNotEmpty()) {
            repository.deleteFeeds(feedIds)
        }
        // Delete each folder individually to avoid everything rolling back on constraint violation
        folderIds.forEach { folderId ->
            try {
                repository.deleteFolder(folderId)
            } catch (_: SQLiteConstraintException) {
                failedDeletes++
            }
        }
        if (failedDeletes > 0) {
            val text =
                context.getString(
                    R.string.failed_to_delete_x_folders,
                    context.resources.getQuantityString(
                        R.plurals.n_folders,
                        failedDeletes,
                        failedDeletes,
                    ),
                )
            toastMaker.makeToast(text)
        }
    }

    fun moveFeeds(
        feedIds: List<Long>,
        targetFolderId: Long,
    ) = applicationCoroutineScope.launch {
        try {
            repository.setFeedsFolderId(feedIds, targetFolderId)
        } catch (_: SQLiteConstraintException) {
            toastMaker.makeToast(R.string.failed_to_move_feeds)
        }
    }

    fun deleteFeeds(feedIds: List<Long>) =
        applicationCoroutineScope.launch {
            repository.deleteFeeds(feedIds)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentFolderRenameDialog: StateFlow<FolderRenameDialog> =
        folderIdToRename
            .flatMapLatest {
                it?.let { folderId ->
                    combine(
                        repository.getFolderNameFlow(folderId),
                        repository.getFolderNamesFolderCannotBeRenamedTo(folderId),
                    ) { folderName, disabledFolderNames ->
                        FolderRenameDialog(
                            folderId = folderId,
                            folderName = folderName ?: "",
                            disabledFolderNames = disabledFolderNames,
                        )
                    }
                } ?: flowOf(FolderRenameDialog())
            }.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FolderRenameDialog(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentFoldersAndFeedsMoveDialog: StateFlow<FoldersAndFeedsMoveDialog> =
        folderIdsToMove
            .flatMapLatest { folderIds ->
                combine(
                    feedIdsToMove,
                    when {
                        folderIds.isNotEmpty() -> repository.getFolderIdsFoldersCannotBeMovedInto(folderIds)
                        else -> flowOf(emptyList())
                    },
                ) { feedIds, disabledFolderIds ->
                    FoldersAndFeedsMoveDialog(
                        folderIds = folderIds,
                        feedIds = feedIds,
                        disabledFolderIds = disabledFolderIds,
                        enabled = folderIds.isNotEmpty() || feedIds.isNotEmpty(),
                    )
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FoldersAndFeedsMoveDialog(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentFoldersAndFeedsDeleteDialog: StateFlow<FoldersAndFeedsDeleteDialog> =
        folderIdsToDelete
            .flatMapLatest { folderIds ->
                combine(
                    feedIdsToDelete,
                    when {
                        folderIds.isNotEmpty() -> repository.getFeedTitlesInFolders(folderIds)
                        else -> flowOf(emptyList())
                    },
                ) { feedIds, feedIdsInFolders ->
                    FoldersAndFeedsDeleteDialog(
                        folderIds = folderIds,
                        feedIds = feedIds,
                        feedIdsInFolders = feedIdsInFolders,
                        enabled = folderIds.isNotEmpty() || feedIds.isNotEmpty(),
                    )
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FoldersAndFeedsDeleteDialog(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentFoldersAndFeedsList: StateFlow<FoldersAndFeedsList> =
        currentFolderId
            .flatMapLatest { folderId ->
                combine(
                    when {
                        folderId > ID_UNSET -> repository.getFolderNameFlow(folderId)
                        else -> flowOf("")
                    },
                    when {
                        folderId > ID_UNSET -> repository.getFolderDisplayItemsPath(folderId)
                        else -> flowOf(emptyList())
                    },
                    repository.getFolderDisplayItemsInFolder(folderId),
                    repository.getFeedDisplayItemsInFolder(folderId),
                    selectModeEnabled,
                    selectedFolderIds,
                    selectedFeedIds,
                ) { params ->
                    val name = params[0] as String?

                    @Suppress("UNCHECKED_CAST")
                    val breadcrumbs = params[1] as List<FolderDisplayItem>

                    @Suppress("UNCHECKED_CAST")
                    val folders = params[2] as List<FolderDisplayItem>

                    @Suppress("UNCHECKED_CAST")
                    val feeds = params[3] as List<FeedDisplayItem>

                    val selectModeEnabled = params[4] as Boolean

                    @Suppress("UNCHECKED_CAST")
                    val selectedFolderIds = params[5] as List<Long>

                    @Suppress("UNCHECKED_CAST")
                    val selectedFeedIds = params[6] as List<Long>

                    val allSelected =
                        when (selectModeEnabled) {
                            true -> folders.all { folder -> folder.id in selectedFolderIds } && feeds.all { feed -> feed.id in selectedFeedIds }
                            false -> false
                        }

                    FoldersAndFeedsList(
                        folderId = folderId,
                        folderName = name ?: "",
                        breadcrumbs = breadcrumbs,
                        folders = folders,
                        feeds = feeds,
                        selectedFolderIds = selectedFolderIds,
                        selectedFeedIds = selectedFeedIds,
                        selectModeEnabled = selectModeEnabled,
                        allSelected = allSelected,
                        initial = false,
                        invalid = name == null,
                    )
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FoldersAndFeedsList(),
            )

    val viewState: StateFlow<EditFoldersViewState> =
        combine(
            currentFoldersAndFeedsList,
            addDialogVisible,
            repository.allFolders,
        ) { foldersAndFeedsList, addDialogVisible, allFolders ->
            EditFoldersViewState(
                foldersAndFeedsList = foldersAndFeedsList,
                addDialogVisible = addDialogVisible,
                allFolders = allFolders,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            EditFoldersViewState(),
        )
}

@Immutable
data class EditFoldersViewState(
    val foldersAndFeedsList: FoldersAndFeedsList = FoldersAndFeedsList(),
    val addDialogVisible: Boolean = false,
    val allFolders: List<SelectableFolder> = emptyList(),
)

@Immutable
data class FoldersAndFeedsList(
    val folderId: Long = ID_UNSET,
    val folderName: String = "",
    val breadcrumbs: List<FolderDisplayItem> = emptyList(),
    val folders: List<FolderDisplayItem> = emptyList(),
    val feeds: List<FeedDisplayItem> = emptyList(),
    val selectedFolderIds: List<Long> = emptyList(),
    val selectedFeedIds: List<Long> = emptyList(),
    val selectModeEnabled: Boolean = false,
    val allSelected: Boolean = false,
    val initial: Boolean = true,
    val invalid: Boolean = false,
)

@Immutable
data class FolderRenameDialog(
    val folderId: Long? = null,
    val folderName: String = "",
    val disabledFolderNames: List<String> = emptyList(),
)

@Immutable
data class FoldersAndFeedsMoveDialog(
    val folderIds: List<Long> = emptyList(),
    val feedIds: List<Long> = emptyList(),
    val disabledFolderIds: List<Long> = emptyList(),
    val enabled: Boolean = false,
)

@Immutable
data class FoldersAndFeedsDeleteDialog(
    val folderIds: List<Long> = emptyList(),
    val feedIds: List<Long> = emptyList(),
    val feedIdsInFolders: List<FeedTitle> = emptyList(),
    val enabled: Boolean = false,
)
