package com.nononsenseapps.feeder.ui.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aallam.openai.client.OpenAIHost
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.openai.isDeepL
import com.nononsenseapps.feeder.openai.isGoogleTranslate
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
        var provider by remember(state.settings) { mutableStateOf(AIProviderPreset.fromSettings(state.settings)) }
        val validationMessage =
            remember(current, provider, state.modelsResult) {
                current.validationMessage(
                    provider = provider,
                    modelsResult = state.modelsResult,
                )
            }
        Dialog(
            onDismissRequest = { onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = false)) },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.92f)
                        .padding(24.dp)
                        .imePadding(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = stringResource(R.string.openai_settings),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    OpenAISectionEdit(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true),
                        state = state,
                        current = current,
                        provider = provider,
                        validationMessage = validationMessage,
                        onEvent = {
                            if (it is OpenAISettingsEvent.UpdateSettings) {
                                current = it.settings
                            } else {
                                onEvent(it)
                            }
                        },
                        onProviderChange = { selected ->
                            provider = selected
                            current = selected.applyTo(current)
                        },
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(
                            onClick = {
                                onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = false))
                            },
                        ) {
                            Text(text = stringResource(android.R.string.cancel))
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            onClick = {
                                onEvent(OpenAISettingsEvent.UpdateSettings(current))
                                onEvent(OpenAISettingsEvent.SwitchEditMode(enabled = false))
                            },
                            enabled = validationMessage == null,
                        ) {
                            Text(text = stringResource(R.string.save))
                        }
                    }
                }
            }
        }
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

        val provider = remember(settings) { AIProviderPreset.fromSettings(settings) }
        TitleAndSubtitle(
            title = {
                Text(text = stringResource(R.string.api_settings_title))
            },
            subtitle = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.api_provider_summary, stringResource(provider.titleRes)),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text =
                            if (settings.key.isBlank()) {
                                stringResource(R.string.ai_not_configured)
                            } else {
                                stringResource(provider.previewSummaryRes)
                            },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }
}

fun isTimeoutInputValid(input: String): Boolean = input.trim().isNotEmpty() && input.toIntOrNull()?.takeIf { it in 30..600 } != null

@Composable
private fun OpenAISectionEdit(
    state: OpenAISettingsState,
    current: OpenAISettings,
    provider: AIProviderPreset,
    validationMessage: String?,
    onEvent: (OpenAISettingsEvent) -> Unit,
    onProviderChange: (AIProviderPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    val showAzureFields = provider == AIProviderPreset.AZURE_OPENAI
    val isTranslationOnlyProvider = provider.isDeepL || provider.isGoogleTranslate

    LaunchedEffect(current) {
        latestOnEvent(OpenAISettingsEvent.LoadModels(settings = current))
    }

    var modelsMenuExpanded by remember { mutableStateOf(false) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
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
        Text(
            text = stringResource(R.string.openai_settings_info),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )

        validationMessage?.let { message ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                    )
                    Text(
                        text = message,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        ProviderField(
            provider = provider,
            expanded = providerMenuExpanded,
            onExpandedChange = { providerMenuExpanded = it },
            onSelect = { selected ->
                providerMenuExpanded = false
                onProviderChange(selected)
            },
        )

        FeatureSummaryCard(provider = provider)

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.key,
            label = {
                Text(
                    stringResource(
                        if (provider.isDeepL) {
                            R.string.deepl_api_key
                        } else {
                            R.string.api_key
                        },
                    ),
                )
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

        if (!isTranslationOnlyProvider) {
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
        }

        if (!isTranslationOnlyProvider) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = current.baseUrl,
                placeholder = {
                    Text(OpenAIHost.OpenAI.baseUrl)
                },
                label = {
                    Text(stringResource(R.string.base_url_optional))
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
        } else {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = provider.endpoint,
                label = {
                    Text(stringResource(R.string.translation_endpoint))
                },
                readOnly = true,
                enabled = false,
                onValueChange = {},
            )
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = current.preferredTranslationLanguage,
            placeholder = {
                Text(stringResource(R.string.translation_language_placeholder))
            },
            label = {
                Text(stringResource(R.string.preferred_translation_language))
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(focusDirection = FocusDirection.Down)
                    },
                ),
            supportingText = {
                Text(stringResource(R.string.preferred_translation_language_description))
            },
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(current.copy(preferredTranslationLanguage = it)))
            },
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = timeoutString,
            placeholder = { Text(text = stringResource(R.string.time_out_placeholder)) },
            label = {
                Text(stringResource(R.string.time_out))
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (showAzureFields) ImeAction.Next else ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(focusDirection = FocusDirection.Down)
                    },
                    onDone = {
                        keyboardController?.hide()
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

        if (showAzureFields) {
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
}

@Composable
private fun ProviderField(
    provider: AIProviderPreset,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (AIProviderPreset) -> Unit,
) {
    Box {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = stringResource(provider.titleRes),
            label = {
                Text(stringResource(R.string.ai_provider))
            },
            readOnly = true,
            onValueChange = {},
            trailingIcon = {
                IconButton(onClick = { onExpandedChange(true) }) {
                    Icon(Icons.Filled.ExpandMore, contentDescription = stringResource(R.string.ai_provider))
                }
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            AIProviderPreset.entries.forEach { preset ->
                DropdownMenuItem(
                    text = { Text(stringResource(preset.titleRes)) },
                    onClick = {
                        onSelect(preset)
                    },
                )
            }
        }
    }
}

@Composable
private fun FeatureSummaryCard(provider: AIProviderPreset) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(provider.descriptionRes),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.titleSmall,
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

private enum class AIProviderPreset(
    val titleRes: Int,
    val descriptionRes: Int,
    val helpRes: Int,
    val previewSummaryRes: Int,
    val isDeepL: Boolean,
    val isGoogleTranslate: Boolean,
    val endpoint: String,
) {
    OPENAI_COMPATIBLE(
        titleRes = R.string.provider_openai_compatible,
        descriptionRes = R.string.provider_openai_compatible_features,
        helpRes = R.string.provider_openai_compatible_help,
        previewSummaryRes = R.string.translation_and_summaries,
        isDeepL = false,
        isGoogleTranslate = false,
        endpoint = "",
    ),
    AZURE_OPENAI(
        titleRes = R.string.provider_azure_openai,
        descriptionRes = R.string.provider_azure_openai_features,
        helpRes = R.string.provider_azure_openai_help,
        previewSummaryRes = R.string.translation_and_summaries,
        isDeepL = false,
        isGoogleTranslate = false,
        endpoint = "",
    ),
    DEEPL(
        titleRes = R.string.provider_deepl,
        descriptionRes = R.string.provider_deepl_features,
        helpRes = R.string.provider_deepl_help,
        previewSummaryRes = R.string.translation_only,
        isDeepL = true,
        isGoogleTranslate = false,
        endpoint = "https://api.deepl.com/v2/translate",
    ),
    GOOGLE_TRANSLATE(
        titleRes = R.string.provider_google_translate,
        descriptionRes = R.string.provider_google_translate_features,
        helpRes = R.string.provider_google_translate_help,
        previewSummaryRes = R.string.translation_only,
        isDeepL = false,
        isGoogleTranslate = true,
        endpoint = "https://translation.googleapis.com/language/translate/v2",
    ),
    ;

    fun applyTo(settings: OpenAISettings): OpenAISettings =
        when (this) {
            OPENAI_COMPATIBLE ->
                settings.copy(
                    baseUrl = "",
                    azureApiVersion = "",
                    azureDeploymentId = "",
                )

            AZURE_OPENAI ->
                settings.copy(
                    baseUrl = inferAzureBaseUrl(settings),
                )

            DEEPL ->
                settings.copy(
                    baseUrl = inferDeepLBaseUrl(settings),
                    azureApiVersion = "",
                    azureDeploymentId = "",
                    modelId = "",
                )

            GOOGLE_TRANSLATE ->
                settings.copy(
                    baseUrl = "https://translation.googleapis.com",
                    azureApiVersion = "",
                    azureDeploymentId = "",
                    modelId = "",
                )
        }

    companion object {
        fun fromSettings(settings: OpenAISettings): AIProviderPreset =
            when {
                settings.baseUrl.contains("openai.azure.com", ignoreCase = true) -> AZURE_OPENAI
                settings.isGoogleTranslate -> GOOGLE_TRANSLATE
                settings.isDeepL -> DEEPL
                else -> OPENAI_COMPATIBLE
            }

        private fun inferAzureBaseUrl(settings: OpenAISettings): String =
            settings.baseUrl.takeIf { it.contains("openai.azure.com", ignoreCase = true) }.orEmpty()

        private fun inferDeepLBaseUrl(settings: OpenAISettings): String =
            when {
                settings.baseUrl.contains("api-free.deepl.com", ignoreCase = true) -> "https://api-free.deepl.com"
                settings.key.endsWith(":fx") -> "https://api-free.deepl.com"
                else -> "https://api.deepl.com"
            }
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
                        key = "",
                        modelId = "gpt-4o-mini",
                    ),
                modelsResult = OpenAIModelsState.None,
                isEditMode = true,
            ),
        onEvent = { },
    )
}

private fun OpenAISettings.validationMessage(
    provider: AIProviderPreset,
    modelsResult: OpenAIModelsState,
): String? {
    if (key.isBlank()) {
        return if (provider.isDeepL) {
            "Enter a DeepL API key before saving."
        } else {
            "Enter an API key before saving."
        }
    }

    val timeoutInput = timeoutSeconds.toString()
    if (!isTimeoutInputValid(timeoutInput)) {
        return "Timeout must be between 30 and 600 seconds."
    }

    if (preferredTranslationLanguage.isBlank()) {
        return "Set a preferred translation language before saving."
    }

    return when (provider) {
        AIProviderPreset.OPENAI_COMPATIBLE -> {
            when {
                modelId.isBlank() -> "Enter a model id before saving."
                modelsResult is OpenAIModelsState.Loading -> "Verifying API settings..."
                modelsResult is OpenAIModelsState.Error -> {
                    modelsResult.message.ifBlank { "The API settings could not be verified." }
                }
                else -> null
            }
        }

        AIProviderPreset.AZURE_OPENAI -> {
            when {
                modelId.isBlank() -> "Enter a model id before saving."
                baseUrl.isBlank() -> "Enter your Azure endpoint before saving."
                azureDeploymentId.isBlank() -> "Enter your Azure deployment id before saving."
                azureApiVersion.isBlank() -> "Enter your Azure API version before saving."
                modelsResult is OpenAIModelsState.Loading -> "Verifying Azure OpenAI settings..."
                modelsResult is OpenAIModelsState.Error -> {
                    modelsResult.message.ifBlank { "The Azure OpenAI settings could not be verified." }
                }
                else -> null
            }
        }

        AIProviderPreset.DEEPL -> {
            when (modelsResult) {
                is OpenAIModelsState.Loading -> "Verifying DeepL key..."
                is OpenAIModelsState.Error -> modelsResult.message.ifBlank { "The DeepL key could not be verified." }
                else -> null
            }
        }

        AIProviderPreset.GOOGLE_TRANSLATE -> {
            when (modelsResult) {
                is OpenAIModelsState.Loading -> "Verifying Google Cloud Translation key..."
                is OpenAIModelsState.Error -> modelsResult.message.ifBlank { "The Google Cloud Translation key could not be verified." }
                else -> null
            }
        }
    }
}
