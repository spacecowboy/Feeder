package com.nononsenseapps.feeder.ui.compose.searchfeed

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemSnippetTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import java.net.URL

private const val LOG_TAG = "FEEDER_ADD"

@Composable
fun AddFeedScreen() {
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddFeedView(
    screenType: ScreenType,
    categories: ImmutableHolder<List<CategoryToggle>>,
    feedSuggestions: ImmutableHolder<List<FeedSuggestion>>,
) {
    val localDimens = LocalDimens.current
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            categories.item.forEach { category ->
                FilterChip(
                    selected = category.selected,
                    onClick = { /*TODO*/ },
                    label = {
                        Text(category.title)
                    },
                )
            }
        }
//        LazyRow(
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            items(
//                categories.item,
//                key = {
//                    it.title
//                },
//                contentType = {
//                    0
//                },
//            ) { category ->
//                FilterChip(
//                    selected = category.selected,
//                    onClick = { /*TODO*/ },
//                    label = {
//                        Text(category.title)
//                    },
//                )
//            }
//        }
        LazyVerticalGrid(
            columns = when (screenType) {
                ScreenType.DUAL -> GridCells.Fixed(2)
                ScreenType.SINGLE -> GridCells.Fixed(1)
            },
            verticalArrangement = Arrangement.spacedBy(localDimens.gutter),
            horizontalArrangement = Arrangement.spacedBy(localDimens.gutter),
        ) {
            items(
                feedSuggestions.item,
                key = {
                    it.url
                },
                contentType = {
                    0
                },
            ) {
                FeedSuggestionCard(
                    feed = it,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSuggestionCard(
    feed: FeedSuggestion,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = { /*TODO*/ },
        modifier = Modifier
            .requiredHeightIn(min = minimumTouchSize),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            feed.image.let { imageUrl ->
                BoxWithConstraints(modifier = Modifier.size(64.dp)) {
                    val pixels = with(LocalDensity.current) {
                        val width = maxWidth.roundToPx()
                        Size(width, (width * 9) / 16)
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .listener(
                                onError = { a, b ->
                                    Log.e(LOG_TAG, "error ${a.data}", b.throwable)
                                },
                            )
                            .scale(Scale.FILL)
                            .size(pixels)
                            .precision(Precision.INEXACT)
                            .build(),
                        placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
                        error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                        contentDescription = stringResource(id = R.string.article_image),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .size(64.dp),
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = modifier
                    .weight(1f),
            ) {
                WithBidiDeterminedLayoutDirection(paragraph = feed.title) {
                    Text(
                        text = feed.title,
                        style = FeedListItemTitleTextStyle(),
                        fontWeight = FontWeight.Normal,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 9,
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }
                WithBidiDeterminedLayoutDirection(paragraph = feed.subtitle) {
                    Text(
                        text = feed.subtitle,
                        style = FeedListItemSnippetTextStyle(),
                        fontWeight = FontWeight.Normal,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 9,
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Immutable
data class CategoryToggle(
    val title: String,
    val selected: Boolean,
)

@Immutable
data class FeedSuggestion(
    val title: String,
    val subtitle: String,
    val url: URL,
    val image: URL?,
    val language: String?,
    val category: String?,
    val tags: Set<String>,
    /*
    <itunes:category text="Technology">
        <itunes:category text="Tech News"/>
    </itunes:category>
     */
    /*
    plus a million. category might have a 'label' meant as the display text
    <category term="Machine Learning"/><category term="Android"/><category term="chrome"/>
     */
)

private fun previewItems() = ImmutableHolder(
    listOf(
        FeedSuggestion(
            title = "Coding Horror",
            subtitle = "programming and human factors",
            url = URL("https://feeds.feedburner.com/codinghorror"),
            image = URL("https://blog.codinghorror.com/favicon.png"),
            language = "english",
            category = "Programming",
            tags = emptySet(),
        ),
        FeedSuggestion(
            title = "Coding Horror",
            subtitle = "programming and human factors",
            url = URL("https://feeds.feedburner.com/codinghorror1"),
            image = URL("https://blog.codinghorror.com/favicon.png"),
            language = "english",
            category = "Programming",
            tags = emptySet(),
        ),
        FeedSuggestion(
            title = "Coding Horror",
            subtitle = "programming and human factors",
            url = URL("https://feeds.feedburner.com/codinghorror2"),
            image = URL("https://blog.codinghorror.com/favicon.png"),
            language = "english",
            category = "Programming",
            tags = emptySet(),
        ),
        FeedSuggestion(
            title = "Coding Horror",
            subtitle = "programming and human factors",
            url = URL("https://feeds.feedburner.com/codinghorror3"),
            image = URL("https://blog.codinghorror.com/favicon.png"),
            language = "english",
            category = "Programming",
            tags = emptySet(),
        ),
        FeedSuggestion(
            title = "Coding Horror",
            subtitle = "programming and human factors",
            url = URL("https://feeds.feedburner.com/codinghorror4"),
            image = URL("https://blog.codinghorror.com/favicon.png"),
            language = "english",
            category = "Programming",
            tags = emptySet(),
        ),
    ),
)

private fun previewCategories() = ImmutableHolder(
    listOf(
        CategoryToggle(
            title = "Programming",
            selected = true,
        ),
        CategoryToggle(
            title = "Android Development",
            selected = false,
        ),
        CategoryToggle(
            title = "Android",
            selected = true,
        ),
        CategoryToggle(
            title = "Apple",
            selected = true,
        ),
        CategoryToggle(
            title = "Architecture",
            selected = false,
        ),
        CategoryToggle(
            title = "Beauty",
            selected = true,
        ),
        CategoryToggle(
            title = "Books",
            selected = false,
        ),
        CategoryToggle(
            title = "Business & Economy",
            selected = false,
        ),
        CategoryToggle(
            title = "Cars",
            selected = false,
        ),
        CategoryToggle(
            title = "Cricket",
            selected = false,
        ),
        CategoryToggle(
            title = "DIY",
            selected = false,
        ),
        CategoryToggle(
            title = "Fashion",
            selected = false,
        ),
        CategoryToggle(
            title = "Food",
            selected = false,
        ),
        CategoryToggle(
            title = "Football",
            selected = false,
        ),
        CategoryToggle(
            title = "Funny",
            selected = false,
        ),
        CategoryToggle(
            title = "Gaming",
            selected = false,
        ),
        CategoryToggle(
            title = "History",
            selected = false,
        ),
        CategoryToggle(
            title = "Interior design",
            selected = false,
        ),
        CategoryToggle(
            title = "iOS Development",
            selected = false,
        ),
        CategoryToggle(
            title = "Movies",
            selected = false,
        ),
        CategoryToggle(
            title = "Music",
            selected = false,
        ),
        CategoryToggle(
            title = "News",
            selected = false,
        ),
        CategoryToggle(
            title = "Personal finance",
            selected = false,
        ),
        CategoryToggle(
            title = "Photography",
            selected = false,
        ),
        CategoryToggle(
            title = "Programming",
            selected = false,
        ),
        CategoryToggle(
            title = "Science",
            selected = false,
        ),
        CategoryToggle(
            title = "Space",
            selected = false,
        ),
        CategoryToggle(
            title = "Sports",
            selected = false,
        ),
        CategoryToggle(
            title = "Startups",
            selected = false,
        ),
        CategoryToggle(
            title = "Tech",
            selected = false,
        ),
        CategoryToggle(
            title = "Television",
            selected = false,
        ),
        CategoryToggle(
            title = "Tennis",
            selected = false,
        ),
        CategoryToggle(
            title = "Travel",
            selected = false,
        ),
        CategoryToggle(
            title = "UI - UX",
            selected = false,
        ),
        CategoryToggle(
            title = "Web Development",
            selected = false,
        ),
    ).sortedBy { it.title },
)

@Preview(
    name = "Feed Suggestion",
    showSystemUi = true,
    device = Devices.NEXUS_5,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
fun PreviewFeedSuggestionCard() {
    FeederTheme {
        Surface {
            FeedSuggestionCard(
                FeedSuggestion(
                    title = "Coding Horror",
                    subtitle = "programming and human factors",
                    url = URL("https://feeds.feedburner.com/codinghorror"),
                    image = URL("https://blog.codinghorror.com/favicon.png"),
                    language = "english",
                    category = "Programming",
                    tags = emptySet(),
                ),
            )
        }
    }
}

@Preview(
    name = "Add Screen Phone",
    showSystemUi = true,
    device = Devices.NEXUS_5,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
fun PreviewAddScreenPhone() {
    FeederTheme {
        Surface {
            AddFeedView(
                screenType = ScreenType.SINGLE,
                categories = previewCategories(),
                feedSuggestions = previewItems(),
            )
        }
    }
}

@Preview(
    name = "Add Screen Foldable",
    showSystemUi = true,
    device = Devices.FOLDABLE,
    uiMode = UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Add Screen results Tablet",
    showSystemUi = true,
    device = Devices.PIXEL_C,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
fun PreviewAddScreenLarge() {
    FeederTheme {
        Surface {
            AddFeedView(
                screenType = ScreenType.DUAL,
                categories = previewCategories(),
                feedSuggestions = previewItems(),
            )
        }
    }
}
