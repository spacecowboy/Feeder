package com.nononsenseapps.feeder.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.filter
import androidx.paging.map
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.size.Scale
import coil3.toBitmap
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.MainActivity
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.kodein.di.instance
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.getValue

data class FeedWidgetItem(
    val id: Long,
    val title: String,
    val snippet: String,
    val feedTitle: String,
    val unread: Boolean,
    val pubDate: String,
    val image: Bitmap,
    val link: String?,
    val bookmarked: Boolean,
    val feedImageUrl: URL?,
    val primarySortTime: Instant,
    val rawPubDate: ZonedDateTime?,
    val wordCount: Int,
)

private fun FeedListItem.toWidgetItem(bitmap: Bitmap) =
    FeedWidgetItem(
        id,
        title,
        snippet,
        feedTitle,
        unread,
        pubDate,
        bitmap,
        link,
        bookmarked,
        feedImageUrl,
        primarySortTime,
        rawPubDate,
        wordCount,
    )

object FeederWidgetGlanceColorScheme {
    val colors =
        ColorProviders(
            light = lightColorScheme(),
            dark = darkColorScheme(),
        )
}

class FeedWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val app = (context.applicationContext as FeederApplication)

        val imageLoader = app.imageLoader
        val loadImageBitmap: suspend (String) -> Bitmap? = { url ->
            val request =
                ImageRequest
                    .Builder(context)
                    .data(url)
                    .scale(Scale.FIT)
                    .precision(Precision.INEXACT)
                    .allowHardware(false)
                    .build()

            (imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap(200, 200)
        }

        provideContent {
            val repository: Repository by app.di.instance()
            val itemFlow =
                repository
                    .getCurrentFeedListItems()
                    .map { pagingData ->
                        pagingData
                            .map { listItem ->
                                val bitmap = loadImageBitmap(listItem.feedImageUrl?.toString() ?: "")
                                Pair(listItem, bitmap)
                            }.filter { it.second != null }
                            .map {
                                it.first.toWidgetItem(it.second!!)
                            }
                    }
            GlanceTheme(colors = FeederWidgetGlanceColorScheme.colors) {
                WidgetContent(
                    itemFlow = itemFlow,
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(itemFlow: Flow<PagingData<FeedWidgetItem>>) {
        val items = itemFlow.collectAsLazyPagingItems()
        Column(modifier = GlanceModifier.background(GlanceTheme.colors.background).padding(4.dp)) {
            FeederTitleBar()
            LazyColumn(modifier = GlanceModifier.padding(start = 12.dp)) {
                items(
                    count = items.itemCount,
                    itemId = { items[it]?.id ?: 0 },
                ) { index ->
                    items[index]?.let {
                        WidgetCard(it)
                    }
                }
            }
        }
    }

    @Composable
    private fun FeederTitleBar() {
        TitleBar(
            startIcon = ImageProvider(R.drawable.ic_stat_f),
            title = LocalContext.current.getString(R.string.widget_title),
            modifier = GlanceModifier.clickable(onClick = actionStartActivity(MainActivity::class.java)),
            actions = {
            },
        )
    }

    @Composable
    private fun WidgetCard(item: FeedWidgetItem) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(ImageProvider(item.image), contentDescription = null)
            Column {
                Text(
                    text = item.title,
                    maxLines = 1,
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GlanceTheme.colors.onSurface,
                        ),
                )
                Text(
                    text = item.snippet,
                    maxLines = 1,
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.secondary,
                        ),
                )
            }
        }
    }

    @Composable
    @OptIn(ExperimentalGlancePreviewApi::class)
    @androidx.glance.preview.Preview(200, 200)
    private fun PreviewWidgetContent() {
        val previewItems =
            listOf(
                FeedWidgetItem(
                    title = "title",
                    snippet =
                        "snippet which is quite long as you might expect from a snipper of a story. " +
                            "It keeps going and going and going and going and going and going and going and going " +
                            "and going and going and going and going and going and going and going and going and going " +
                            "and going and going and going and going and going and going and going and going and going and " +
                            "going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = false,
                    image = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888),
                    link = null,
                    id = ID_UNSET,
                    bookmarked = false,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 900,
                ),
            )
        WidgetContent(MutableStateFlow(PagingData.from(previewItems)))
    }
}
