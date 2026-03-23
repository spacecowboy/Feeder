package com.nononsenseapps.feeder.ui.compose.settings

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
import com.nononsenseapps.feeder.openai.isBlankConfiguration
import com.nononsenseapps.feeder.openai.isDeepL
import com.nononsenseapps.feeder.openai.isGoogleTranslate
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

enum class OpenAISectionType {
    Summary,
    Translation,
}

@Composable
fun OpenAISection(
    title: String,
    info: String,
    state: OpenAISettingsState,
    onEvent: (OpenAISettingsEvent) -> Unit,
    section: OpenAISectionType,
    preferredTranslationLanguage: String = "",
    onPreferredTranslationLanguageChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sanitizedSettings = remember(section, state.settings) { section.sanitizeSettings(state.settings) }

    OpenAISectionItem(
        title = title,
        section = section,
        settings = sanitizedSettings,
        onEvent = onEvent,
        modifier = modifier,
    )

    if (state.isEditMode) {
        var current by remember(section, state.settings) { mutableStateOf(sanitizedSettings) }
        var currentPreferredTranslationLanguage by remember(preferredTranslationLanguage) { mutableStateOf(preferredTranslationLanguage) }
        var provider by remember(section, state.settings) { mutableStateOf(AIProviderPreset.fromSettings(sanitizedSettings)) }
        val context = LocalContext.current
        val matchingModelsResult =
            remember(current, state.modelsResult) {
                state.modelsResult.takeIf { it.matches(current) } ?: OpenAIModelsState.None
            }
        val validationMessage =
            remember(current, provider, matchingModelsResult, context) {
                current.validationMessage(
                    provider = provider,
                    modelsResult = matchingModelsResult,
                    context = context,
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
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    OpenAISectionEdit(
                        info = info,
                        modelsResult = matchingModelsResult,
                        current = current,
                        provider = provider,
                        section = section,
                        validationMessage = validationMessage,
                        preferredTranslationLanguage = currentPreferredTranslationLanguage,
                        showModelsError = state.showModelsError,
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
                        onPreferredTranslationLanguageChange = { currentPreferredTranslationLanguage = it },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true),
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
                                if (section == OpenAISectionType.Translation) {
                                    onPreferredTranslationLanguageChange(currentPreferredTranslationLanguage)
                                }
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
    title: String,
    section: OpenAISectionType,
    settings: OpenAISettings,
    onEvent: (OpenAISettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sanitizedSettings = remember(section, settings) { section.sanitizeSettings(settings) }
    val provider = remember(sanitizedSettings) { AIProviderPreset.fromSettings(sanitizedSettings) }

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

        TitleAndSubtitle(
            title = {
                Text(text = title)
            },
            subtitle = {
                Text(
                    text =
                        if (sanitizedSettings.key.isBlank()) {
                            stringResource(R.string.ai_not_configured)
                        } else {
                            stringResource(R.string.api_provider_summary, stringResource(provider.titleRes))
                        },
                    style = MaterialTheme.typography.bodySmall,
                )
            },
        )
    }
}

fun isTimeoutInputValid(input: String): Boolean = input.trim().isNotEmpty() && input.toIntOrNull()?.takeIf { it in 30..600 } != null

@Composable
private fun OpenAISectionEdit(
    info: String,
    modelsResult: OpenAIModelsState,
    current: OpenAISettings,
    provider: AIProviderPreset,
    section: OpenAISectionType,
    validationMessage: String?,
    preferredTranslationLanguage: String,
    showModelsError: Boolean,
    onEvent: (OpenAISettingsEvent) -> Unit,
    onProviderChange: (AIProviderPreset) -> Unit,
    onPreferredTranslationLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    val showAzureFields = provider == AIProviderPreset.AZURE_OPENAI
    val hasProvider = provider != AIProviderPreset.NONE
    val isTranslationOnlyProvider = provider.isDeepL || provider.isGoogleTranslate

    LaunchedEffect(current, provider) {
        if (provider != AIProviderPreset.NONE) {
            latestOnEvent(OpenAISettingsEvent.LoadModels(settings = current))
        }
    }

    var modelsMenuExpanded by remember { mutableStateOf(false) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var timeoutString by remember(current.timeoutSeconds) { mutableStateOf(current.timeoutSeconds.toString()) }
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
            text = info,
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
            section = section,
            provider = provider,
            expanded = providerMenuExpanded,
            onExpandedChange = { providerMenuExpanded = it },
            onSelect = { selected ->
                providerMenuExpanded = false
                onProviderChange(selected)
            },
        )

        if (hasProvider) {
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
        }

        if (hasProvider && !isTranslationOnlyProvider) {
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
                        enabled = modelsResult is OpenAIModelsState.Success,
                    ) {
                        if (modelsResult is OpenAIModelsState.Loading) {
                            CircularProgressIndicator()
                        } else {
                            Icon(Icons.Filled.ExpandMore, contentDescription = stringResource(R.string.list_of_available_models))
                            if (modelsResult is OpenAIModelsState.Success) {
                                OpenAIModelsDropdown(
                                    menuExpanded = modelsMenuExpanded,
                                    state = modelsResult,
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
                state = modelsResult,
                showError = showModelsError,
                onEvent = onEvent,
            )
        }

        if (hasProvider && !isTranslationOnlyProvider) {
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
        } else if (hasProvider) {
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

        if (section == OpenAISectionType.Translation && hasProvider) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = preferredTranslationLanguage,
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
                onValueChange = onPreferredTranslationLanguageChange,
            )
        }

        if (hasProvider) {
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
                        imeAction =
                            when {
                                showAzureFields -> ImeAction.Next
                                else -> ImeAction.Done
                            },
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
        }

        if (hasProvider && showAzureFields) {
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
    section: OpenAISectionType,
    provider: AIProviderPreset,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (AIProviderPreset) -> Unit,
) {
    val availableProviders = remember(section) { AIProviderPreset.availableFor(section) }

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
            availableProviders.forEach { preset ->
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

        is OpenAIModelsState.Loading -> {}
        OpenAIModelsState.None -> {}
    }
}

private enum class AIProviderPreset(
    val titleRes: Int,
    val supportsSummary: Boolean,
    val supportsTranslation: Boolean,
    val isDeepL: Boolean,
    val isGoogleTranslate: Boolean,
    val endpoint: String,
) {
    NONE(
        titleRes = R.string.provider_none,
        supportsSummary = true,
        supportsTranslation = true,
        isDeepL = false,
        isGoogleTranslate = false,
        endpoint = "",
    ),
    OPENAI_COMPATIBLE(
        titleRes = R.string.provider_openai_compatible,
        supportsSummary = true,
        supportsTranslation = true,
        isDeepL = false,
        isGoogleTranslate = false,
        endpoint = "",
    ),
    AZURE_OPENAI(
        titleRes = R.string.provider_azure_openai,
        supportsSummary = true,
        supportsTranslation = true,
        isDeepL = false,
        isGoogleTranslate = false,
        endpoint = "",
    ),
    DEEPL(
        titleRes = R.string.provider_deepl,
        supportsSummary = false,
        supportsTranslation = true,
        isDeepL = true,
        isGoogleTranslate = false,
        endpoint = "https://api.deepl.com/v2/translate",
    ),
    GOOGLE_TRANSLATE(
        titleRes = R.string.provider_google_translate,
        supportsSummary = false,
        supportsTranslation = true,
        isDeepL = false,
        isGoogleTranslate = true,
        endpoint = "https://translation.googleapis.com/language/translate/v2",
    ),
    ;

    fun applyTo(settings: OpenAISettings): OpenAISettings =
        when (this) {
            NONE ->
                settings.copy(
                    key = "",
                    modelId = "",
                    baseUrl = "",
                    azureApiVersion = "",
                    azureDeploymentId = "",
                )

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
        fun availableFor(section: OpenAISectionType): List<AIProviderPreset> =
            entries.filter { preset ->
                when (section) {
                    OpenAISectionType.Summary -> preset.supportsSummary
                    OpenAISectionType.Translation -> preset.supportsTranslation
                }
            }

        fun fromSettings(settings: OpenAISettings): AIProviderPreset =
            when {
                settings.isBlankConfiguration -> NONE
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
private fun SummaryOpenAISectionReadPreview() {
    Surface {
        OpenAISection(
            title = "Summary API",
            info = "Used for summaries.",
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
            section = OpenAISectionType.Summary,
        )
    }
}

@Preview("tablet", device = Devices.PIXEL_C)
@Preview("phone", device = Devices.PIXEL_7)
@Composable
private fun TranslationOpenAISectionEditPreview() {
    OpenAISection(
        title = "Translation API",
        info = "Used for translation.",
        state =
            OpenAISettingsState(
                settings = OpenAISettings(),
                modelsResult = OpenAIModelsState.None,
                isEditMode = true,
            ),
        onEvent = { },
        section = OpenAISectionType.Translation,
        preferredTranslationLanguage = "English",
    )
}

private fun OpenAISettings.validationMessage(
    provider: AIProviderPreset,
    modelsResult: OpenAIModelsState,
    context: Context,
): String? {
    if (provider == AIProviderPreset.NONE) {
        return null
    }

    if (key.isBlank()) {
        return if (provider.isDeepL) {
            context.getString(R.string.enter_deepl_api_key_before_saving)
        } else {
            context.getString(R.string.enter_api_key_before_saving)
        }
    }

    val timeoutInput = timeoutSeconds.toString()
    if (!isTimeoutInputValid(timeoutInput)) {
        return context.getString(R.string.time_out_validation_error)
    }

    return when (provider) {
        AIProviderPreset.NONE -> null
        AIProviderPreset.OPENAI_COMPATIBLE -> {
            when {
                modelId.isBlank() -> context.getString(R.string.enter_model_id_before_saving)
                modelsResult == OpenAIModelsState.None || modelsResult is OpenAIModelsState.Loading ->
                    context.getString(R.string.verifying_api_settings)
                modelsResult is OpenAIModelsState.Error -> {
                    modelsResult.message.ifBlank { context.getString(R.string.api_settings_could_not_be_verified) }
                }
                else -> null
            }
        }

        AIProviderPreset.AZURE_OPENAI -> {
            when {
                modelId.isBlank() -> context.getString(R.string.enter_model_id_before_saving)
                baseUrl.isBlank() -> context.getString(R.string.enter_azure_endpoint_before_saving)
                azureDeploymentId.isBlank() -> context.getString(R.string.enter_azure_deployment_id_before_saving)
                azureApiVersion.isBlank() -> context.getString(R.string.enter_azure_api_version_before_saving)
                modelsResult == OpenAIModelsState.None || modelsResult is OpenAIModelsState.Loading ->
                    context.getString(R.string.verifying_azure_openai_settings)
                modelsResult is OpenAIModelsState.Error -> {
                    modelsResult.message.ifBlank { context.getString(R.string.azure_openai_settings_could_not_be_verified) }
                }
                else -> null
            }
        }

        AIProviderPreset.DEEPL -> {
            when (modelsResult) {
                OpenAIModelsState.None, is OpenAIModelsState.Loading -> context.getString(R.string.verifying_deepl_key)
                is OpenAIModelsState.Error -> modelsResult.message.ifBlank { context.getString(R.string.deepl_key_could_not_be_verified) }
                else -> null
            }
        }

        AIProviderPreset.GOOGLE_TRANSLATE -> {
            when (modelsResult) {
                OpenAIModelsState.None, is OpenAIModelsState.Loading -> context.getString(R.string.verifying_google_cloud_translation_key)
                is OpenAIModelsState.Error ->
                    modelsResult.message.ifBlank { context.getString(R.string.google_cloud_translation_key_could_not_be_verified) }
                else -> null
            }
        }
    }
}

private fun OpenAIModelsState.matches(settings: OpenAISettings): Boolean =
    when (this) {
        is OpenAIModelsState.Error -> this.settings == settings
        is OpenAIModelsState.Loading -> this.settings == settings
        OpenAIModelsState.None -> false
        is OpenAIModelsState.Success -> this.settings == settings
    }

private fun OpenAISectionType.sanitizeSettings(settings: OpenAISettings): OpenAISettings =
    when (this) {
        OpenAISectionType.Summary ->
            when {
                settings.isBlankConfiguration -> settings
                settings.isDeepL || settings.isGoogleTranslate -> OpenAISettings()
                else -> settings
            }

        OpenAISectionType.Translation ->
            when {
                settings.isBlankConfiguration -> settings
                else -> settings
            }
    }
