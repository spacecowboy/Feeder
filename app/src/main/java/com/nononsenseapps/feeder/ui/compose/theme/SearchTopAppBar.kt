package com.nononsenseapps.feeder.ui.compose.theme

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.utils.ThemePreviews

/**
 * On a small but tall screen this will be a LargeTopAppBar to make the screen
 * more one-hand friendly.
 *
 * One a short screen - or bigger tablet size - then it's a small top app bar which can scoll
 * out of the way to make best use of the available screen space.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    searchText: String? = null,
    onSearchChange: (String) -> Unit,
    hideSearchField: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            val focusRequester = remember { FocusRequester() }
            val interactionSource = remember {
                MutableInteractionSource()
            }
            val inputText = remember {
                mutableStateOf(searchText ?: "")
            }


            BasicTextField(
                value = inputText.value,
                onValueChange = {
                    inputText.value = it
                    onSearchChange(it)
                },
                textStyle = TextStyle.Default.copy(
                    color = LocalContentColor.current,
                ),
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
                    .focusRequester(focusRequester),
                decorationBox = @Composable { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = searchText ?: "",
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        leadingIcon = {
                            Icon(Icons.Default.Search, "")
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = hideSearchField
                            ) {
                                Icon(Icons.Default.Clear, "")
                            }
                        },
                        contentPadding = PaddingValues(2.dp),
                        placeholder = {
                            Text(text = stringResource(R.string.search_noun))
                        },
                    )
                },
            )
            LaunchedEffect(Unit, searchText) {
                if(searchText === null){
                    focusRequester.requestFocus()
                }
            }
        },
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@ThemePreviews
fun SearchTopAppBarPreview(){
    FeederTheme {
        SearchTopAppBar(
            searchText = "Sample text",
            onSearchChange = {},
            hideSearchField = {},
        )
    }
}
