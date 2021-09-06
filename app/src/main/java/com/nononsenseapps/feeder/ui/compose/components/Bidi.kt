package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.text.BidiFormatter
import com.nononsenseapps.feeder.util.getLocale

@Composable
fun UnicodeWrap(text: String): String =
    BidiFormatter.getInstance(LocalContext.current.getLocale()).unicodeWrap(text)
