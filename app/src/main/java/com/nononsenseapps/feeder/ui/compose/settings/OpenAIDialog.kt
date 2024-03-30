package com.nononsenseapps.feeder.ui.compose.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.archmodel.OpenAISettings

private enum class OpenAIHostType {
    OpenAI,
    Azure
}

private fun OpenAISettings.toOpenAIHostType(): OpenAIHostType
    = if (baseUrl.contains("openai.azure.com", ignoreCase = true))
        OpenAIHostType.Azure
    else
        OpenAIHostType.OpenAI

@Composable
fun OpenAIDialog(
    settings: OpenAISettings,
    models: OpenAIModelsState,
    onSettingsUpdate: (OpenAISettings) -> Unit = {},
    onDismissRequest: () -> Unit = { },
    onLoadModels: () -> Unit
) {
    LaunchedEffect(true) {
        onLoadModels()
    }

    var current by remember(settings) { mutableStateOf(settings) }
    var modelsMenuExpanded by remember { mutableStateOf(false) }
    var showHostMenu by remember { mutableStateOf(false) }
    var hostType: OpenAIHostType by remember(settings.baseUrl) { mutableStateOf(OpenAIHostType.OpenAI) }
    Surface {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "API Key",
                style = MaterialTheme.typography.titleMedium
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = current.key,
                onValueChange = {
                    current = current.copy(key = it)
                    onLoadModels()
                },
                visualTransformation = VisualTransformationApiKey()
            )
            Text(
                text = "Model Id",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = current.modelId,
                onValueChange = { current = current.copy(modelId = it) },
                trailingIcon = {
                    IconButton(
                        onClick = { modelsMenuExpanded = true },
                        enabled = models is OpenAIModelsState.Success
                    ) {
                        if (models is OpenAIModelsState.Loading) {
                            CircularProgressIndicator()
                        } else {
                            Icon(Icons.Filled.ExpandMore, contentDescription = "List of available models")
                        }
                    }
                }
            )
            OpenAIModelsState(
                menuExpanded = modelsMenuExpanded,
                state = models,
                onValueChange = { current = current.copy(modelId = it) },
                onDismissRequest = { modelsMenuExpanded = false }
            )

            Text(
                text = "Url",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedButton(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { showHostMenu = true }
            ) {
                Text(
                    text = hostType.name ,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (showHostMenu) {
                DropdownMenu(
                    expanded = showHostMenu,
                    onDismissRequest = { showHostMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(OpenAIHostType.OpenAI.name) },
                        onClick = {
                            hostType = OpenAIHostType.OpenAI
                            showHostMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(OpenAIHostType.Azure.name) },
                        onClick = {
                            hostType = OpenAIHostType.Azure
                            showHostMenu = false
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { onSettingsUpdate(current) }) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun OpenAIModelsState(menuExpanded: Boolean, state: OpenAIModelsState, onValueChange: (String) -> Unit, onDismissRequest: () -> Unit) {
    when (state) {
        is OpenAIModelsState.Success -> {
            if (state.ids.isEmpty()) {
                OutlinedCard(modifier = Modifier.padding(top = 4.dp)) {
                    Text("No models were found")
                }
            } else {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = onDismissRequest
                ) {
                    state.ids.forEach { id ->
                        DropdownMenuItem(
                            text = { Text(text = id) },
                            onClick = {
                                onValueChange(id)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }

        is OpenAIModelsState.Error -> {
            OutlinedCard(modifier = Modifier.padding(top = 4.dp)) {
                Text("Unable to load models: ${state.message}")
            }
        }

        OpenAIModelsState.Loading -> {}
        OpenAIModelsState.None -> {}
    }
}
