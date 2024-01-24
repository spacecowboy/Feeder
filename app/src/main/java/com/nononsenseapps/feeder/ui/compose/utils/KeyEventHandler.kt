package com.nononsenseapps.feeder.ui.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState

fun interface KeyEventHandler {
    fun invoke(keyCode: Int): Boolean
}

val LocalKeyEventHandlers: ProvidableCompositionLocal<MutableList<KeyEventHandler>> = compositionLocalOf { error("Missing LocalKeyEventHandlers!") }

@Suppress("ktlint:compose:mutable-params-check")
@Composable
fun ProvideKeyEventHandler(
    handlers: MutableList<KeyEventHandler>,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalKeyEventHandlers provides handlers, content = content)
}

@Composable
fun ListenKeyEvents(handler: KeyEventHandler) {
    val handlerState = rememberUpdatedState(handler)
    val eventHandlers = LocalKeyEventHandlers.current
    DisposableEffect(handlerState) {
        val localHandler = KeyEventHandler { keyEvent -> handlerState.value.invoke(keyEvent) }
        eventHandlers.add(localHandler)
        onDispose {
            eventHandlers.remove(localHandler)
        }
    }
}
