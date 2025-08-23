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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aallam.openai.client.OpenAIHost
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

@Composable
fun OpenAISection(
    state: OpenAISettingsState,
    onEvent: (OpenAISettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OpenAISectionItem(
        settings = state.settings,
        onEvent = onEvent,
        modifier = modifier,
    )

    if (state.isEditMode) {
        var current by remember(state.settings) { mutableStateOf(state.settings) }
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
                    modifier = Modifier,
                    state = state,
                    current = current,
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
    onEvent: (OpenAISettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
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

fun isTimeoutInputValid(input: String): Boolean = input.trim().isNotEmpty() && input.toIntOrNull()?.takeIf { it in 30..600 } != null

@Composable
fun OpenAISectionEdit(
    state: OpenAISettingsState,
    current: OpenAISettings,
    onEvent: (OpenAISettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(current) {
        latestOnEvent(OpenAISettingsEvent.LoadModels(settings = current))
    }

    var modelsMenuExpanded by remember { mutableStateOf(false) }
    var timeoutString by remember { mutableStateOf(current.timeoutSeconds.toString()) }
    val isTimeoutInputValid =
        remember(timeoutString) {
            isTimeoutInputValid(timeoutString)
        }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.key,
            label = {
                Text(stringResource(R.string.api_key))
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(focusDirection = FocusDirection.Down)
                    },
                ),
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(current.copy(key = it)))
            },
            visualTransformation = VisualTransformationApiKey(),
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.modelId,
            label = {
                Text(stringResource(R.string.model_id))
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(focusDirection = FocusDirection.Down)
                    },
                ),
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(current.copy(modelId = it)))
            },
            trailingIcon = {
                IconButton(
                    onClick = { modelsMenuExpanded = true },
                    enabled = state.modelsResult is OpenAIModelsState.Success,
                ) {
                    if (state.modelsResult is OpenAIModelsState.Loading) {
                        CircularProgressIndicator()
                    } else {
                        Icon(Icons.Filled.ExpandMore, contentDescription = stringResource(R.string.list_of_available_models))
                        if (state.modelsResult is OpenAIModelsState.Success) {
                            OpenAIModelsDropdown(
                                menuExpanded = modelsMenuExpanded,
                                state = state.modelsResult,
                                onValueChange = {
                                    onEvent(OpenAISettingsEvent.UpdateSettings(current.copy(modelId = it)))
                                },
                                onDismissRequest = { modelsMenuExpanded = false },
                            )
                        }
                    }
                }
            },
        )

        OpenAIModelsStatus(
            state = state.modelsResult,
            showError = state.showModelsError,
            onEvent = onEvent,
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.baseUrl,
            placeholder = {
                Text(OpenAIHost.OpenAI.baseUrl)
            },
            label = {
                Text(stringResource(R.string.url))
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(focusDirection = FocusDirection.Down)
                    },
                ),
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(current.copy(baseUrl = it)))
            },
        )

        TextField(
            modifier =
                Modifier.fillMaxWidth(),
            value = timeoutString,
            placeholder = { Text(text = stringResource(R.string.time_out_placeholder)) },
            label = {
                Text(stringResource(R.string.time_out))
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(focusDirection = FocusDirection.Down)
                    },
                ),
            supportingText = {
                if (!isTimeoutInputValid) {
                    Text(stringResource(R.string.time_out_validation_error))
                }
            },
            onValueChange = { input ->
                timeoutString = input
                if (isTimeoutInputValid(timeoutString)) {
                    onEvent(OpenAISettingsEvent.UpdateSettings(current.copy(timeoutSeconds = timeoutString.toInt())))
                }
            },
            isError = !isTimeoutInputValid,
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.azureDeploymentId,
            label = {
                Text(stringResource(R.string.azure_deployment_id))
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(focusDirection = FocusDirection.Down)
                    },
                ),
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(current.copy(azureDeploymentId = it)))
            },
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.azureApiVersion,
            placeholder = {
                Text("2024-02-15-preview")
            },
            label = {
                Text(stringResource(R.string.azure_api_version))
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    },
                ),
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(current.copy(azureApiVersion = it)))
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
private fun OpenAIModelsStatus(
    state: OpenAIModelsState,
    showError: Boolean,
    onEvent: (OpenAISettingsEvent) -> Unit,
) {
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
            val hasError by remember(state.message) { mutableStateOf(state.message.isNotEmpty()) }
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(OpenAISettingsEvent.ShowModelsError(show = !showError)) },
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(R.string.unable_to_load_models),
                            modifier =
                                Modifier
                                    .padding(start = 4.dp)
                                    .weight(1f),
                        )
                        if (hasError) {
                            Icon(
                                imageVector = if (showError) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = stringResource(R.string.show_message),
                            )
                        }
                    }

                    if (hasError && showError) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }
        }

        OpenAIModelsState.Loading -> {}
        OpenAIModelsState.None -> {}
    }
}

@Preview("tablet", device = Devices.PIXEL_C)
@Preview("phone", device = Devices.PIXEL_7)
@Composable
private fun OpenAISectionReadPreview() {
    Surface {
        OpenAISection(
            state =
                OpenAISettingsState(
                    settings =
                        OpenAISettings(
                            key = "sk-test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                            modelId = "gpt-4o-mini",
                        ),
                    modelsResult = OpenAIModelsState.None,
                    isEditMode = false,
                ),
            onEvent = { },
        )
    }
}

@Preview("tablet", device = Devices.PIXEL_C)
@Preview("phone", device = Devices.PIXEL_7)
@Composable
private fun OpenAISectionEditPreview() {
    OpenAISection(
        state =
            OpenAISettingsState(
                settings =
                    OpenAISettings(
                        key = "sk-test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                        modelId = "gpt-4o-mini",
                    ),
                modelsResult = OpenAIModelsState.None,
                isEditMode = true,
            ),
        onEvent = { },
    )
}

@Preview("tablet", device = Devices.PIXEL_C)
@Preview("phone", device = Devices.PIXEL_7)
@Composable
private fun OpenAISectionErrorCollapsedPreview() {
    OpenAISection(
        state =
            OpenAISettingsState(
                settings =
                    OpenAISettings(
                        key = "sk-test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                        modelId = "gpt-4o-mini",
                    ),
                modelsResult =
                    OpenAIModelsState.Error(
                        message = "A sample error message",
                    ),
                isEditMode = true,
                showModelsError = false,
            ),
        onEvent = { },
    )
}

@Preview("tablet", device = Devices.PIXEL_C)
@Preview("phone", device = Devices.PIXEL_7)
@Composable
private fun OpenAISectionErrorExpandedPreview() {
    OpenAISection(
        state =
            OpenAISettingsState(
                settings =
                    OpenAISettings(
                        key = "sk-test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                        modelId = "gpt-4o-mini",
                    ),
                modelsResult =
                    OpenAIModelsState.Error(
                        message = "A sample error message",
                    ),
                isEditMode = true,
                showModelsError = true,
            ),
        onEvent = { },
    )
}
