package com.nononsenseapps.feeder.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
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
import com.nononsenseapps.feeder.model.RssLocalSync
import com.nononsenseapps.feeder.ui.MainActivity
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.instance
import java.io.File
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
    val image: Bitmap?,
    val link: String?,
    val bookmarked: Boolean,
    val feedImageUrl: URL?,
    val primarySortTime: Instant,
    val rawPubDate: ZonedDateTime?,
    val wordCount: Int,
)

private fun FeedListItem.toWidgetItem(bitmap: Bitmap?) =
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

sealed class WidgetState {
    data object Syncing : WidgetState()

    data class Ready(
        val items: PagingData<FeedWidgetItem>,
    ) : WidgetState()
}

object FeederWidgetGlanceColorScheme {
    val colors =
        ColorProviders(
            light = lightColorScheme(),
            dark = darkColorScheme(),
        )
}

private class WidgetStateDataStore(
    private val context: Context,
) : DataStore<WidgetState> {
    private val app = (context.applicationContext as FeederApplication)
    private val imageLoader = app.imageLoader
    private val loadImageBitmap: suspend (String) -> Bitmap? = { url ->
        val request =
            ImageRequest
                .Builder(context)
                .data(url)
                .size(200, 200)
                .scale(Scale.FIT)
                .precision(Precision.INEXACT)
                .allowHardware(false)
                .build()

        (imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap(200, 200, Bitmap.Config.RGB_565)
    }

    private val repository: Repository by app.di.instance()

    override val data: Flow<WidgetState>
        get() =
            combine(
                repository.syncWorkerRunning,
                repository
                    .getCurrentWidgetFeedListItems()
                    .map { pagingData ->
                        pagingData
                            .map { listItem ->
                                val bitmap = loadImageBitmap(listItem.feedImageUrl?.toString() ?: "")
                                listItem.toWidgetItem(bitmap)
                            }
                    },
            ) { syncWorkerRunning, feedWidgetItems ->
                if (syncWorkerRunning) {
                    WidgetState.Syncing
                } else {
                    WidgetState.Ready(feedWidgetItems)
                }
            }

    override suspend fun updateData(transform: suspend (t: WidgetState) -> WidgetState): WidgetState = throw NotImplementedError("Widget does not need to update its own data")
}

class FeedWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<WidgetState>
        get() =
            object : GlanceStateDefinition<WidgetState> {
                override suspend fun getDataStore(
                    context: Context,
                    fileKey: String,
                ): DataStore<WidgetState> = WidgetStateDataStore(context)

                override fun getLocation(
                    context: Context,
                    fileKey: String,
                ): File = throw NotImplementedError("Widget does not provide a concrete state file location")
            }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val app = (context.applicationContext as FeederApplication)
            val coroutineScope = rememberCoroutineScope()
            val runSync = {
                val rssLocalSync by app.di.instance<RssLocalSync>()
                coroutineScope.launch { rssLocalSync.syncFeeds() }
                Unit
            }
            GlanceTheme(colors = FeederWidgetGlanceColorScheme.colors) {
                WidgetContent(
                    widgetState = currentState(),
                    runSync = runSync,
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        widgetState: WidgetState,
        runSync: () -> Unit,
    ) {
        Column(
            modifier =
                GlanceModifier
                    .fillMaxHeight()
                    .background(GlanceTheme.colors.background)
                    .padding(4.dp),
        ) {
            FeederTitleBar(runSync)
            when (widgetState) {
                is WidgetState.Syncing -> WidgetSyncingContent()
                is WidgetState.Ready -> {
                    val pagingItems = flowOf(widgetState.items).collectAsLazyPagingItems()
                    WidgetReadyContent(pagingItems.itemSnapshotList.items)
                }
            }
        }
    }

    @Composable
    private fun FeederTitleBar(runSync: () -> Unit) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier =
                    GlanceModifier
                        .clickable(onClick = actionStartActivity(MainActivity::class.java)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_stat_f),
                    contentDescription = null,
                )
                Text(
                    text = LocalContext.current.getString(R.string.widget_title),
                    style = TextStyle(color = GlanceTheme.colors.onSurface),
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Image(
                provider = ImageProvider(R.drawable.ic_stat_sync),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.padding(8.dp).clickable(runSync),
            )

            Image(
                provider = ImageProvider(R.drawable.ic_settings),
                contentDescription = "",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
                modifier =
                    GlanceModifier
                        .padding(start = 12.dp, end = 12.dp)
                        .clickable(actionStartActivity(FeedWidgetSettingsActivity::class.java)),
            )
        }
    }

    @Composable
    private fun WidgetSyncingContent() {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = LocalContext.current.getString(R.string.widget_refreshing),
                style = TextStyle(color = GlanceTheme.colors.onSurface),
            )
        }
    }

    @Composable
    private fun WidgetReadyContent(items: List<FeedWidgetItem>) {
        LazyColumn(modifier = GlanceModifier.padding(start = 12.dp)) {
            items(
                count = items.size,
                itemId = { items[it].id },
            ) { index ->
                WidgetCard(items[index])
            }
        }
    }

    @Composable
    private fun WidgetCard(item: FeedWidgetItem) {
        Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Row(
                modifier =
                    GlanceModifier
                        .fillMaxWidth()
                        .background(GlanceTheme.colors.surfaceVariant)
                        .padding(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                item.image?.let {
                    Image(ImageProvider(it), contentDescription = null)
                }
                Column(modifier = GlanceModifier.padding(start = 8.dp)) {
                    Text(
                        text = item.title,
                        maxLines = 2,
                        style =
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GlanceTheme.colors.onSurface,
                            ),
                    )
                    Text(
                        text = item.snippet,
                        maxLines = 2,
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
    }

    @Composable
    @OptIn(ExperimentalGlancePreviewApi::class)
    @androidx.glance.preview.Preview(400, 500)
    internal fun PreviewWidgetContent() {
        val iconBitmap =
            Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).also {
                android.graphics.Canvas(it).drawColor(android.graphics.Color.rgb(80, 120, 200))
            }
        val previewItems =
            listOf(
                FeedWidgetItem(
                    title = "title which is very long because we have to get enough of the clicks baited in donchaknow",
                    snippet =
                        "snippet which is quite long as you might expect from a snipper of a story. " +
                            "It keeps going and going and going and going and going and going and going and going " +
                            "and going and going and going and going and going and going and going and going and going " +
                            "and going and going and going and going and going and going and going and going and going and " +
                            "going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = false,
                    image = iconBitmap,
                    link = null,
                    id = ID_UNSET,
                    bookmarked = false,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 900,
                ),
                FeedWidgetItem(
                    title = "Second article with a more reasonable title",
                    snippet = "A shorter snippet that gets to the point quickly.",
                    feedTitle = "Another Feed",
                    pubDate = "Jun 10, 2021",
                    unread = true,
                    image = null,
                    link = null,
                    id = ID_UNSET + 1,
                    bookmarked = false,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 200,
                ),
            )
        GlanceTheme(colors = ColorProviders(light = darkColorScheme(), dark = darkColorScheme())) {
            Column(
                modifier =
                    GlanceModifier
                        .fillMaxHeight()
                        .background(GlanceTheme.colors.background)
                        .padding(4.dp),
            ) {
                FeederTitleBar({})
                WidgetReadyContent(previewItems)
            }
        }
    }
}
