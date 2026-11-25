package com.nononsenseapps.feeder.ui.compose.folder

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.modifiers.interceptKey
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder

@Composable
fun FolderTextDialog(
    @StringRes title: Int,
    onDismiss: () -> Unit,
    onOk: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialFolderName: String? = null,
    invalidNames: ImmutableHolder<List<String>> = ImmutableHolder(emptyList()),
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        val text = initialFolderName ?: ""
        mutableStateOf(
            TextFieldValue(
                text = text,
                selection = TextRange(text.length),
            ),
        )
    }
    var isError by rememberSaveable {
        mutableStateOf(false)
    }

    FolderTextDialog(
        title = title,
        textFieldValue = textFieldValue,
        isError = isError,
        onValueChange = { newTextFieldValue ->
            if (newTextFieldValue.text != textFieldValue.text) {
                isError = false
            }
            textFieldValue = newTextFieldValue
        },
        onDismiss = onDismiss,
        onOk = {
            val folderName = textFieldValue.text.trim()
            if (folderName.isNotEmpty() && !invalidNames.item.contains(folderName)) {
                onOk(folderName)
            } else {
                isError = true
            }
        },
        modifier = modifier,
    )
}

@Composable
fun FolderTextDialog(
    @StringRes title: Int,
    textFieldValue: TextFieldValue,
    isError: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onOk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onOk,
                enabled = !isError,
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(vertical = 8.dp),
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = onValueChange,
                    label = {
                        Text(stringResource(R.string.folder_name))
                    },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrectEnabled = true,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = { onOk() },
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .interceptKey(Key.Enter, onOk)
                            .interceptKey(Key.Escape, onDismiss)
                            .focusRequester(focusRequester),
                )
                AnimatedVisibility(
                    visible = isError,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    val folderEmptyLabel = stringResource(R.string.folder_name_empty)
                    val folderExistsLabel = stringResource(R.string.folder_already_exists)

                    Text(
                        textAlign = TextAlign.Center,
                        text =
                            when {
                                textFieldValue.text.isBlank() -> folderEmptyLabel
                                else -> folderExistsLabel
                            },
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error),
                    )
                }
            }
        },
    )
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewFolderRenameDialog() =
    PreviewTheme {
        FolderTextDialog(
            title = R.string.rename_folder,
            textFieldValue = TextFieldValue("Name"),
            isError = false,
            onValueChange = {},
            onDismiss = {},
            onOk = {},
        )
    }

@Preview(showSystemUi = true)
@Composable
private fun PreviewFolderRenameDialogError() =
    PreviewTheme {
        FolderTextDialog(
            title = R.string.rename_folder,
            textFieldValue = TextFieldValue("Name"),
            isError = true,
            onValueChange = {},
            onDismiss = {},
            onOk = {},
        )
    }

@Preview(showSystemUi = true)
@Composable
private fun PreviewFolderCreateDialog() =
    PreviewTheme {
        FolderTextDialog(
            title = R.string.add_folder,
            textFieldValue = TextFieldValue(""),
            isError = false,
            onValueChange = {},
            onDismiss = {},
            onOk = {},
        )
    }

@Preview(showSystemUi = true)
@Composable
private fun PreviewFolderCreateDialogError() =
    PreviewTheme {
        FolderTextDialog(
            title = R.string.add_folder,
            textFieldValue = TextFieldValue(""),
            isError = true,
            onValueChange = {},
            onDismiss = {},
            onOk = {},
        )
    }
