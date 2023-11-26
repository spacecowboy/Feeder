package com.nononsenseapps.feeder.ui.compose.utils

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.debugInspectorInfo

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.onKeyEventLikeEscape(action: () -> Unit) =
    composed(
        inspectorInfo =
            debugInspectorInfo {
                name = "onEscapeLikeKeyPress"
                properties["action"] = action
            },
    ) {
        onKeyEvent {
            when (it.key) {
                Key.Escape, Key.Back, Key.NavigateOut -> {
                    action()
                    true
                }

                else -> false
            }
        }
    }

fun Modifier.focusableInNonTouchMode(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) = composed(
    inspectorInfo =
        debugInspectorInfo {
            name = "focusableInNonTouchMode"
            properties["enabled"] = enabled
            properties["interactionSource"] = interactionSource
        },
) {
    val inputModeManager = LocalInputModeManager.current
    Modifier
        .focusProperties { canFocus = inputModeManager.inputMode != InputMode.Touch }
        .focusable(enabled, interactionSource)
}
