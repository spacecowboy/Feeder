package com.nononsenseapps.feeder.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeederTypography

class FeedWidget : GlanceAppWidget() {
    // The widget can be displayed in any size
    override val sizeMode = SizeMode.Exact

//    override suspend fun providePreview(context: Context, widgetCategory: Int) = provideContent {
//        Content()
//    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        provideContent {
            GlanceTheme(
                // Use dynamic colors if possible
                colors =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        GlanceTheme.colors
                    } else {
                        WidgetColors.colors
                    },
            ) {
                FeedList(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .background(GlanceTheme.colors.surfaceVariant),
//                        .background(Color(0x0a000000))
                )
            }
        }
    }

    @Composable
    private fun FeedList(modifier: GlanceModifier = GlanceModifier) {
        LazyColumn(
            modifier = modifier,
        ) {
            item {
                FooItem(
                    feedName = "THE WALL STREET JOURNAL",
                    title = "Foo bar it is a long title that goes on and on and on and on and on and on and on and on and on and on",
                )
            }
            item {
                FooItem(
                    feedName = "Ars Technica",
                    title = "Foo bar it is a long title that goes on and on and on and on and on and on and on and on and on and on",
                )
            }
        }
    }

    @Composable
    private fun FooItem(
        feedName: String,
        title: String,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        Column(
            modifier =
                modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(),
        ) {
            Text(
                text = title,
                maxLines = 5,
                style = GetTextStyleArticleTitle(),
                modifier = GlanceModifier.padding(bottom = 4.dp),
            )
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
            ) {
                Text(
                    text = feedName,
                    maxLines = 1,
                    style = GetTextStyleFeedName(),
                    modifier =
                        GlanceModifier.padding(end = 4.dp)
                            .defaultWeight(),
                )
                Text(
                    text = "2023-10-01",
                    maxLines = 1,
                    style = GetTextStyleFeedName(),
                    modifier = GlanceModifier.wrapContentWidth(),
                )
            }
        }
    }

    @Composable
    fun GetTextStyleFeedName(): TextStyle {
        return TextStyle(
            color = GlanceTheme.colors.onSurface,
            fontSize = FeederTypography.materialTypography.labelMedium.fontSize,
            fontWeight = FeederTypography.materialTypography.labelMedium.fontWeight?.toGlance(),
        )
    }

    @Composable
    fun GetTextStyleArticleTitle(): TextStyle {
        return TextStyle(
            color = GlanceTheme.colors.onSurface,
            fontSize = FeederTypography.materialTypography.titleMedium.fontSize,
            fontWeight = FeederTypography.materialTypography.titleMedium.fontWeight?.toGlance(),
        )
    }
}

private fun androidx.compose.ui.text.font.FontWeight.toGlance(): FontWeight? {
    return when {
        this.weight < 450 -> FontWeight.Normal
        this.weight < 600 -> FontWeight.Medium
        else -> FontWeight.Bold
    }
}
