package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> AutoCompleteFoo(
    displaySuggestions: Boolean,
    suggestions: List<T>,
    onSuggestionClicked: (T) -> Unit,
    maxHeight: Dp = TextFieldDefaults.MinHeight * 3,
    suggestionContent: @Composable (T) -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        content()

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedVisibility(visible = displaySuggestions) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = maxHeight)
                    .fillMaxWidth(0.9f)
                    .border(
                        border = BorderStroke(2.dp, MaterialTheme.colors.onBackground),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                items(suggestions) { suggestion ->
                    Box(
                        modifier = Modifier
                            .clickable { onSuggestionClicked(suggestion) }
                    ) {
                        suggestionContent(suggestion)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAutoCompleteOutlinedText() {
    AutoCompleteFoo(
        displaySuggestions = true,
        onSuggestionClicked = {},
        suggestionContent = {
            Text(text = it)
        },
        suggestions = listOf("One", "Two", "Three")
    ) {
        OutlinedTextField(value = "Testing", onValueChange = {})
    }
}
