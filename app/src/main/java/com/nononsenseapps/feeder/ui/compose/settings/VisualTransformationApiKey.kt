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
        val stars = "*".repeat(text.length - PREFIX_LENGTH - SUFFIX_LENGTH)
        val transformed = "${text.subSequence(0..PREFIX_LENGTH)}$stars${text.subSequence(text.length - PREFIX_LENGTH, text.length)}"
        return TransformedText(AnnotatedString(transformed), OffsetMapping.Identity)
    }

    companion object {
        const val PREFIX_LENGTH = 3
        const val SUFFIX_LENGTH = 4
    }
}
