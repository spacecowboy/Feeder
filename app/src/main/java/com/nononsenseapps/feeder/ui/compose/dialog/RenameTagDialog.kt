package com.nononsenseapps.feeder.ui.compose.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nononsenseapps.feeder.R

@Composable
fun RenameTagDialog(
    currentTagName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tagName by rememberSaveable { mutableStateOf(currentTagName) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onRename(tagName)
                    onDismiss()
                },
                enabled = tagName.isNotBlank() && tagName != currentTagName,
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
            Text(text = stringResource(id = R.string.rename_tag))
        },
        text = {
            OutlinedTextField(
                value = tagName,
                onValueChange = { tagName = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}
