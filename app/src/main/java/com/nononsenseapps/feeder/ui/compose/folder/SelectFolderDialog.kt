package com.nononsenseapps.feeder.ui.compose.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.SelectableFolder
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.immutableListHolderOf

@Composable
fun SelectFolderDialog(
    folders: ImmutableHolder<List<SelectableFolder>>,
    initialFolderId: Long,
    showEditButton: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectFolderDialog(
        title = {
            if (showEditButton) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.select_folder),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                    Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_folders))
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.select_folder),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .padding(vertical = 8.dp),
                )
            }
        },
        confirmButton = {},
        folders = folders,
        isSelected = { folderId ->
            folderId == initialFolderId
        },
        isEnabled = { true },
        onDismiss = onDismiss,
        onSelect = onConfirm,
        modifier = modifier,
    )
}

@Composable
fun MoveFolderDialog(
    foldersCount: Int,
    feedsCount: Int,
    folders: ImmutableHolder<List<SelectableFolder>>,
    disabledFolderIds: ImmutableHolder<List<Long>>,
    initialFolderId: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedFolderId by rememberSaveable {
        mutableLongStateOf(initialFolderId)
    }
    val title =
        when {
            foldersCount == 1 && feedsCount == 0 -> R.string.move_folder
            feedsCount == 1 && foldersCount == 0 -> R.string.move_feed
            else -> R.string.move_selection
        }

    SelectFolderDialog(
        title = {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(vertical = 8.dp),
            )
        },
        confirmButton = {
            Button(
                enabled = !disabledFolderIds.item.contains(selectedFolderId),
                onClick = {
                    if (!disabledFolderIds.item.contains(selectedFolderId)) {
                        onConfirm(selectedFolderId)
                    }
                },
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        folders = folders,
        isSelected = { folderId ->
            folderId == selectedFolderId
        },
        isEnabled = { folderId ->
            !disabledFolderIds.item.contains(folderId)
        },
        onDismiss = onDismiss,
        onSelect = { folderId ->
            if (!disabledFolderIds.item.contains(folderId)) {
                selectedFolderId = folderId
            }
        },
        modifier = modifier,
    )
}

@Composable
fun SelectFolderDialog(
    title: @Composable (() -> Unit),
    confirmButton: @Composable (() -> Unit),
    folders: ImmutableHolder<List<SelectableFolder>>,
    isSelected: (Long) -> Boolean,
    isEnabled: (Long) -> Boolean,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = confirmButton,
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = title,
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                item(
                    key = "root",
                    contentType = "root",
                ) {
                    val folderId = ID_UNSET
                    FolderRadioButton(
                        onClick = {
                            onSelect(folderId)
                        },
                        selected = isSelected(folderId),
                        enabled = isEnabled(folderId),
                        name = stringResource(R.string.no_folder),
                        depth = 0,
                    )
                }
                items(
                    folders.item,
                    key = { it.id },
                    contentType = { "folder" },
                ) { folder ->
                    val folderId = folder.id
                    FolderRadioButton(
                        onClick = {
                            onSelect(folderId)
                        },
                        selected = isSelected(folderId),
                        enabled = isEnabled(folderId),
                        name = folder.name,
                        depth = folder.depth,
                    )
                }
            }
        },
    )
}

@Composable
fun FolderRadioButton(
    onClick: () -> Unit,
    selected: Boolean,
    enabled: Boolean,
    name: String,
    depth: Int,
    modifier: Modifier = Modifier,
) {
    val indentation = depth.coerceIn(0, 3) * 24.dp
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .requiredHeightIn(min = minimumTouchSize)
                .clickable(onClick = onClick)
                .semantics(mergeDescendants = true) {},
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.clearAndSetSemantics { },
        )
        Spacer(modifier = Modifier.width(indentation + 16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewSelectFolderDialog() {
    PreviewTheme {
        SelectFolderDialog(
            folders =
                immutableListHolderOf(
                    SelectableFolder(id = 1L, name = "Folder1", depth = 0),
                    SelectableFolder(id = 2L, name = "Subfolder1", depth = 1),
                    SelectableFolder(id = 3L, name = "Subfolder2", depth = 2),
                    SelectableFolder(id = 4L, name = "Subfolder3 with too long name", depth = 3),
                    SelectableFolder(id = 5L, name = "Subfolder4", depth = 4),
                    SelectableFolder(id = 6L, name = "Folder2", depth = 0),
                ),
            initialFolderId = 2L,
            showEditButton = true,
            onDismiss = {},
            onConfirm = {},
            onEdit = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewMoveFolderDialog() {
    PreviewTheme {
        MoveFolderDialog(
            foldersCount = 1,
            feedsCount = 0,
            folders =
                immutableListHolderOf(
                    SelectableFolder(id = 1L, name = "Parent folder", depth = 0),
                    SelectableFolder(id = 2L, name = "News", depth = 1),
                    SelectableFolder(id = 3L, name = "More News", depth = 2),
                    SelectableFolder(id = 4L, name = "Other folder", depth = 0),
                ),
            disabledFolderIds = ImmutableHolder(listOf(1L, 2L, 3L)),
            initialFolderId = 1L,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
