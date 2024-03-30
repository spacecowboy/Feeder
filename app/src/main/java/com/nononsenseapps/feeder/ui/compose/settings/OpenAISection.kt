package com.nononsenseapps.feeder.ui.compose.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Save
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aallam.openai.client.OpenAIHost
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.openai.toOpenAIHost
import com.nononsenseapps.feeder.openai.toUrl
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

@Composable
fun OpenAISection(
    openAISettings: OpenAISettings,
    openAIModels: OpenAIModelsState,
    openAIEdit: Boolean,
    onEvent: (OpenAISettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .padding(start = 64.dp, bottom = 16.dp)
                .width(LocalDimens.current.maxContentWidth),
    ) {
        if (openAIEdit) {
            OpenAISectionEdit(
                modifier = Modifier,
                settings = openAISettings,
                models = openAIModels,
                onEvent = onEvent,
            )
        } else {
            OpenAISectionReadOnly(
                modifier = Modifier,
                settings = openAISettings,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun OpenAISectionReadOnly(
    settings: OpenAISettings,
    modifier: Modifier = Modifier,
    onEvent: (OpenAISettingsEvent) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        val transformedKey = remember(settings.key) { VisualTransformationApiKey().filter(AnnotatedString(settings.key)) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.api_key),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = { onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = true)) }) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit")
            }
        }
        Text(
            text = transformedKey.text,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.model_id),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = settings.modelId,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.url),
            style = MaterialTheme.typography.titleMedium,
        )
        val url = remember(settings) { settings.toOpenAIHost(withAzureDeploymentId = true).toUrl().buildString() }
        Text(
            text = url,
            style = MaterialTheme.typography.bodyMedium,
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
    var current by remember(settings) { mutableStateOf(settings) }
    val latestOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(true) {
        latestOnEvent(OpenAISettingsEvent.LoadModels)
    }

    var modelsMenuExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = stringResource(R.string.api_key),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1.0f))
            IconButton(onClick = {
                onEvent(OpenAISettingsEvent.UpdateSettings(current))
                onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = false))
            }) {
                Icon(Icons.Outlined.Save, contentDescription = stringResource(R.string.save))
            }

            IconButton(onClick = { onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = false)) }) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(android.R.string.cancel))
            }
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.key,
            onValueChange = {
                current = current.copy(key = it)
                onEvent(OpenAISettingsEvent.LoadModels)
            },
            visualTransformation = VisualTransformationApiKey(),
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.model_id),
            style = MaterialTheme.typography.titleMedium,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.modelId,
            onValueChange = {
                current = current.copy(modelId = it)
                onEvent(OpenAISettingsEvent.LoadModels)
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
                    }
                }
            },
        )
        OpenAIModelsSection(
            menuExpanded = modelsMenuExpanded,
            state = models,
            onValueChange = {
                current = current.copy(modelId = it)
            },
            onDismissRequest = { modelsMenuExpanded = false },
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.url),
            style = MaterialTheme.typography.titleMedium,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.baseUrl,
            placeholder = {
                Text(OpenAIHost.OpenAI.baseUrl)
            },
            onValueChange = {
                current = current.copy(baseUrl = it)
                onEvent(OpenAISettingsEvent.LoadModels)
            },
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.azure_deployment_id),
            style = MaterialTheme.typography.titleMedium,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.azureDeploymentId,
            onValueChange = {
                current = current.copy(azureDeploymentId = it)
                onEvent(OpenAISettingsEvent.LoadModels)
            },
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.azure_api_version),
            style = MaterialTheme.typography.titleMedium,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.azureApiVersion,
            onValueChange = {
                current = current.copy(azureApiVersion = it)
                onEvent(OpenAISettingsEvent.LoadModels)
            },
        )
    }
}

@Composable
private fun OpenAIModelsSection(
    menuExpanded: Boolean,
    state: OpenAIModelsState,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (state) {
        is OpenAIModelsState.Success -> {
            if (state.ids.isEmpty()) {
                OutlinedCard {
                    Text(
                        text = stringResource(R.string.no_models_were_found),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            } else {
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
        }

        is OpenAIModelsState.Error -> {
            OutlinedCard {
                Text(
                    text = stringResource(R.string.unable_to_load_models) + " " + state.message,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        OpenAIModelsState.Loading -> {}
        OpenAIModelsState.None -> {}
    }
}

@Preview
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

@Preview
@Composable
private fun OpenAISectionEditPreview() {
    Surface {
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
}
