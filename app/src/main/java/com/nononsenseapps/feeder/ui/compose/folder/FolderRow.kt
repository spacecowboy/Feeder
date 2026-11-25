package com.nononsenseapps.feeder.ui.compose.folder

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.settings.TitleAndSubtitle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.util.logDebug

@Composable
fun FolderRow(
    folderName: String,
    selectModeEnabled: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DisplayRow(
        title = folderName,
        image = {
            Icon(
                Icons.Outlined.Folder,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
        },
        selectModeEnabled = selectModeEnabled,
        isSelected = isSelected,
        onClick = onClick,
        onToggleSelect = onToggleSelect,
        renameOptionEnabled = true,
        onRename = onRename,
        onMove = onMove,
        onDelete = onDelete,
        modifier = modifier,
    )
}

@Composable
fun FeedRow(
    displayTitle: String,
    imageUrl: String?,
    selectModeEnabled: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DisplayRow(
        title = displayTitle,
        image =
            if (imageUrl != null) {
                {
                    val pixels =
                        with(LocalDensity.current) {
                            32.dp.roundToPx()
                        }
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(imageUrl)
                                .listener(
                                    onError = { a, b ->
                                        logDebug("FEEDER_FOLDER_SCREEN", "error ${a.data}", b.throwable)
                                    },
                                ).scale(Scale.FIT)
                                .size(pixels)
                                .precision(Precision.INEXACT)
                                .build(),
                        contentDescription = stringResource(id = R.string.feed_icon),
                        fallback = rememberTintedVectorPainter(Icons.Outlined.RssFeed),
                        contentScale = ContentScale.Fit,
                        modifier =
                            Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .size(32.dp),
                    )
                }
            } else {
                {
                    Icon(
                        Icons.Outlined.RssFeed,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                }
            },
        selectModeEnabled = selectModeEnabled,
        isSelected = isSelected,
        onClick = onClick,
        onToggleSelect = onToggleSelect,
        renameOptionEnabled = false,
        onRename = {},
        onMove = onMove,
        onDelete = onDelete,
        modifier = modifier,
    )
}

@Composable
fun DisplayRow(
    title: String,
    image: (@Composable () -> Unit),
    onClick: () -> Unit,
    onToggleSelect: () -> Unit,
    selectModeEnabled: Boolean,
    isSelected: Boolean,
    renameOptionEnabled: Boolean,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current
    val renameLabel = stringResource(R.string.action_rename)
    val moveLabel = stringResource(R.string.action_move)
    val deleteLabel = stringResource(R.string.action_delete)
    val selectedString = stringResource(androidx.compose.ui.R.string.selected)
    val notSelectedString = stringResource(androidx.compose.ui.R.string.not_selected)

    var dropDownMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .heightIn(min = 64.dp)
                .combinedClickable(
                    onClick = {
                        if (!selectModeEnabled) {
                            onClick()
                        } else {
                            onToggleSelect()
                        }
                    },
                    onLongClick = onToggleSelect,
                ).safeSemantics(mergeDescendants = true) {
                    stateDescription =
                        when (isSelected) {
                            true -> selectedString
                            false -> notSelectedString
                        }
                    customActions =
                        buildList {
                            if (renameOptionEnabled) {
                                add(
                                    CustomAccessibilityAction(renameLabel) {
                                        onRename()
                                        true
                                    },
                                )
                            }
                            add(
                                CustomAccessibilityAction(moveLabel) {
                                    onMove()
                                    true
                                },
                            )
                            add(
                                CustomAccessibilityAction(deleteLabel) {
                                    onDelete()
                                    true
                                },
                            )
                        }
                },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .minimumInteractiveComponentSize()
                    .padding(end = 8.dp),
        ) {
            image()
        }

        TitleAndSubtitle(
            title = {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )

        if (selectModeEnabled) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = {
                    onToggleSelect()
                },
                modifier = Modifier.clearAndSetSemantics { },
            )
        } else {
            Box {
                IconButton(
                    onClick = {
                        dropDownMenuExpanded = true
                    },
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.open_menu),
                    )
                }

                DropdownMenu(expanded = dropDownMenuExpanded, onDismissRequest = { dropDownMenuExpanded = false }) {
                    if (renameOptionEnabled) {
                        DropdownMenuItem(
                            onClick = {
                                onRename()
                                dropDownMenuExpanded = false
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = renameLabel,
                                    )
                                }
                            },
                        )
                    }
                    DropdownMenuItem(
                        onClick = {
                            onMove()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = moveLabel,
                                )
                            }
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDelete()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = deleteLabel,
                                )
                            }
                        },
                    )

                    val closeMenuText = stringResource(id = R.string.close_menu)
                    // Hidden button for TalkBack
                    DropdownMenuItem(
                        onClick = {
                            dropDownMenuExpanded = false
                        },
                        text = {},
                        modifier =
                            Modifier
                                .height(0.dp)
                                .safeSemantics {
                                    contentDescription = closeMenuText
                                    role = Role.Button
                                },
                    )
                }
            }
        }
    }
}
