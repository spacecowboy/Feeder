package com.nononsenseapps.feeder.ui.compose.settings

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class VisualTransformationApiKey : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.isBlank() || text.length < 10) {
            return VisualTransformation.None.filter(text)
        }
        val prefixLength = 3
        val suffixLength = 4
        val stars = "*".repeat(text.length - prefixLength - suffixLength)
        val transformed = "${text.subSequence(0..prefixLength)}$stars${text.subSequence(text.length - prefixLength, text.length)}"
        return TransformedText(AnnotatedString(transformed), OffsetMapping.Identity)
    }
}
