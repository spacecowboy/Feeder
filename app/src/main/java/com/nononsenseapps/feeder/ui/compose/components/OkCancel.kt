package com.nononsenseapps.feeder.ui.compose.components

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OkCancelWithContent(
    onOk: () -> Unit,
    onCancel: () -> Unit,
    okEnabled: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
           content()
        }
        Spacer(modifier = Modifier.height(24.dp))
        OkCancelButtons(
            onOk = onOk,
            onCancel = onCancel,
            okEnabled = okEnabled
        )
    }
}

@Composable
@Preview(showBackground = true)
fun OkCancelButtons(
    onOk: () -> Unit = {},
    onCancel: () -> Unit = {},
    okEnabled: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = onCancel) {
            Text(
                text = stringResource(id = R.string.cancel)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(
            enabled = okEnabled,
            onClick = onOk
        ) {
            Text(
                text = stringResource(id = R.string.ok)
            )
        }
    }
}
