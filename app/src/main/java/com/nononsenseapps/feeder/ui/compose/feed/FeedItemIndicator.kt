package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme

@Composable
fun FeedItemIndicatorRow(
    unread: Boolean,
    bookmarked: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (unread) {
            FeedItemIndicator {
                Text(stringResource(id = R.string.new_indicator))
            }
        }
        if (bookmarked) {
            FeedItemIndicator {
                Icon(
                    Icons.Default.Star,
                    contentDescription = stringResource(id = R.string.saved_article),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
fun FeedItemIndicatorColumn(
    unread: Boolean,
    bookmarked: Boolean,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    iconSize: Dp = 16.dp,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.End,
    ) {
        if (unread) {
            FeedItemIndicator {
                Text(stringResource(id = R.string.new_indicator))
            }
        }
        if (bookmarked) {
            FeedItemIndicator {
                Icon(
                    Icons.Default.Star,
                    contentDescription = stringResource(id = R.string.saved_article),
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

@Composable
fun FeedItemIndicator(
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit),
) {
    ProvideTextStyle(
        value = MaterialTheme.typography.labelMedium,
    ) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .defaultMinSize(
                        minHeight = 24.dp,
                    )
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                    ),
            ) {
                content()
            }
        }
    }
}

@Preview("Light")
@Composable
fun PreviewLightFeedItemIndicatorRow() {
    FeederTheme(currentTheme = ThemeOptions.DAY) {
        Surface {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(32.dp),
            ) {
                FeedItemIndicatorRow(
                    unread = true,
                    bookmarked = true,
                )
            }
        }
    }
}

@Preview("Dark")
@Composable
fun PreviewDarkFeedItemIndicatorRow() {
    FeederTheme(currentTheme = ThemeOptions.NIGHT) {
        Surface {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(32.dp),
            ) {
                FeedItemIndicatorRow(
                    unread = true,
                    bookmarked = true,
                )
            }
        }
    }
}

@Preview("Light")
@Composable
fun PreviewLightFeedItemIndicatorColumn() {
    FeederTheme(currentTheme = ThemeOptions.DAY) {
        Surface {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(32.dp),
            ) {
                FeedItemIndicatorColumn(
                    unread = true,
                    bookmarked = true,
                )
            }
        }
    }
}

@Preview("Dark")
@Composable
fun PreviewDarkFeedItemIndicatorColumn() {
    FeederTheme(currentTheme = ThemeOptions.NIGHT) {
        Surface {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(32.dp),
            ) {
                FeedItemIndicatorColumn(
                    unread = true,
                    bookmarked = true,
                )
            }
        }
    }
}
