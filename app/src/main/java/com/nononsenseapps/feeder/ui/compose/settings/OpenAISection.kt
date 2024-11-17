package com.nononsenseapps.feeder.ui.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aallam.openai.client.OpenAIHost
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

@Composable
fun OpenAISection(
    openAISettings: OpenAISettings,
    openAIModels: OpenAIModelsState,
    openAIEdit: Boolean,
    onEvent: (OpenAISettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OpenAISectionItem(
        modifier = modifier,
        settings = openAISettings,
        onEvent = onEvent,
    )

    if (openAIEdit) {
        var current by remember(openAISettings) { mutableStateOf(openAISettings) }
        AlertDialog(
            confirmButton = {
                Button(onClick = {
                    onEvent(OpenAISettingsEvent.UpdateSettings(current))
                    onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = false))
                }) {
                    Text(text = stringResource(R.string.save))
                }
            },
            dismissButton = {
                Button(onClick = {
                    onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = false))
                }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            onDismissRequest = { onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = false)) },
            title = {
                Text(text = stringResource(R.string.openai_settings))
            },
            text = {
                OpenAISectionEdit(
                    modifier = modifier,
                    settings = current,
                    models = openAIModels,
                    onEvent = {
                        if (it is OpenAISettingsEvent.UpdateSettings) {
                            current = it.settings
                        } else {
                            onEvent(it)
                        }
                    },
                )
            },
        )
    }
}

@Composable
private fun OpenAISectionItem(
    settings: OpenAISettings,
    modifier: Modifier = Modifier,
    onEvent: (OpenAISettingsEvent) -> Unit,
) {
    Row(
        modifier =
            modifier
                .width(LocalDimens.current.maxContentWidth)
                .clickable { onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = true)) }
                .semantics { role = Role.Button },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center,
        ) { }

        val transformedKey = remember(settings.key) { VisualTransformationApiKey().filter(AnnotatedString(settings.key)) }
        TitleAndSubtitle(
            title = {
                Text(
                    text = stringResource(R.string.api_key),
                )
            },
            subtitle = {
                Text(
                    text = transformedKey.text,
                    style = MaterialTheme.typography.bodySmall,
                )
            },
        )
    }
}

@Composable
fun OpenAISectionEdit(
    settings: OpenAISettings,
    models: OpenAIModelsState,
    onEvent: (OpenAISettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(settings) {
        latestOnEvent(OpenAISettingsEvent.LoadModels(settings = settings))
    }

    var modelsMenuExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.key,
            label = {
                Text(stringResource(R.string.api_key))
            },
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings.copy(key = it)))
            },
            visualTransformation = VisualTransformationApiKey(),
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.modelId,
            label = {
                Text(stringResource(R.string.model_id))
            },
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings.copy(modelId = it)))
            },
            trailingIcon = {
                IconButton(
                    onClick = { modelsMenuExpanded = true },
                    enabled = models is OpenAIModelsState.Success,
                ) {
                    if (models is OpenAIModelsState.Loading) {
                        CircularProgressIndicator()
                    } else {
                        Icon(Icons.Filled.ExpandMore, contentDescription = stringResource(R.string.list_of_available_models))
                        if (models is OpenAIModelsState.Success) {
                            OpenAIModelsDropdown(
                                menuExpanded = modelsMenuExpanded,
                                state = models,
                                onValueChange = {
                                    onEvent(OpenAISettingsEvent.UpdateSettings(settings.copy(modelId = it)))
                                },
                                onDismissRequest = { modelsMenuExpanded = false },
                            )
                        }
                    }
                }
            },
        )

        OpenAIModelsStatus(state = models)

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.baseUrl,
            placeholder = {
                Text(OpenAIHost.OpenAI.baseUrl)
            },
            label = {
                Text(stringResource(R.string.url))
            },
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings.copy(baseUrl = it)))
            },
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.azureDeploymentId,
            label = {
                Text(stringResource(R.string.azure_deployment_id))
            },
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings.copy(azureDeploymentId = it)))
            },
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.azureApiVersion,
            placeholder = {
                Text("2024-02-15-preview")
            },
            label = {
                Text(stringResource(R.string.azure_api_version))
            },
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings.copy(azureApiVersion = it)))
            },
        )
    }
}

@Composable
private fun OpenAIModelsDropdown(
    menuExpanded: Boolean,
    state: OpenAIModelsState.Success,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = onDismissRequest,
    ) {
        state.ids.forEach { id ->
            DropdownMenuItem(
                text = { Text(text = id) },
                onClick = {
                    onValueChange(id)
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
private fun OpenAIModelsStatus(state: OpenAIModelsState) {
    when (state) {
        is OpenAIModelsState.Success -> {
            if (state.ids.isEmpty()) {
                OutlinedCard {
                    Text(
                        text = stringResource(R.string.no_models_were_found),
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }

        is OpenAIModelsState.Error -> {
            OutlinedCard {
                Text(
                    text = stringResource(R.string.unable_to_load_models) + " " + state.message,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        OpenAIModelsState.Loading -> {}
        OpenAIModelsState.None -> {}
    }
}

@Preview("OpenAI section item tablet", device = Devices.PIXEL_C)
@Preview("OpenAI section item phone", device = Devices.PIXEL_7)
@Composable
private fun OpenAISectionReadPreview() {
    Surface {
        OpenAISection(
            openAISettings =
                OpenAISettings(
                    key = "sk-test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                    modelId = "gpt-4o-mini",
                ),
            openAIModels = OpenAIModelsState.None,
            openAIEdit = false,
            onEvent = { },
        )
    }
}

@Preview("OpenAI section dialog tablet", device = Devices.PIXEL_C)
@Preview("OpenAI section dialog phone", device = Devices.PIXEL_7)
@Composable
private fun OpenAISectionEditPreview() {
    OpenAISection(
        openAISettings =
            OpenAISettings(
                key = "sk-test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                modelId = "gpt-4o-mini",
            ),
        openAIModels = OpenAIModelsState.None,
        openAIEdit = true,
        onEvent = { },
    )
}
