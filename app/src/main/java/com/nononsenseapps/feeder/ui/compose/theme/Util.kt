package com.nononsenseapps.feeder.ui.compose.theme

import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.nononsenseapps.feeder.R

@DrawableRes
@Composable
inline fun PlaceholderImage(): Int {
    val isLightTheme = MaterialTheme.colorScheme.isLight

    @DrawableRes
    val placeHolder: Int by remember(isLightTheme) {
        derivedStateOf {
            if (isLightTheme) {
                R.drawable.placeholder_image_article_day
            } else {
                R.drawable.placeholder_image_article_night
            }
        }
    }

    return placeHolder
}
