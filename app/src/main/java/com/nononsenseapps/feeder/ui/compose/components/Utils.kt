package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics

fun Modifier.safeSemantics(
    mergeDescendants: Boolean = false,
    properties: (SemanticsPropertyReceiver.() -> Unit),
): Modifier =
    semantics(mergeDescendants = mergeDescendants) {
        try {
            properties()
        } catch (_: Exception) {
            // Bug in framework? This can be null in any case
        }
    }

