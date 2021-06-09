package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.google.accompanist.coil.rememberCoilPainter
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.ui.compose.theme.FeederTypography
import com.nononsenseapps.feeder.ui.compose.theme.contentHorizontalPadding
import org.kodein.di.compose.instance
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

private val shortDateTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

private val feedItemHeightMax = 128.dp
private val feedItemHeightMin = 64.dp

@Composable
fun FeedItemPreview(item: PreviewItem, onItemClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(
                top = 2.dp,
                bottom = 2.dp,
                end = if (item.imageUrl?.isNotBlank() != true) contentHorizontalPadding else 0.dp
            )
            .heightIn(min = feedItemHeightMin, max = feedItemHeightMax)
//            .height(feedItemHeight)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
//                .fillMaxHeight()
                .weight(weight = 1.0f, fill = true)
//                .border(0.5.dp, Color.Red)
                .clickable {
                    onItemClick()
                }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
//                    .border(0.5.dp, Color.Magenta)
                    .fillMaxWidth()
            ) {
                Text(
                    text = item.feedDisplayTitle,
                    style = MaterialTheme.typography.caption.merge(FeederTypography.previewDate),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(weight = 1.0f, fill = true)
//                        .border(1.dp, Color.Green)
                )
                Text(
                    text = item.pubDate?.toLocalDate()?.format(shortDateTimeFormat)
                        ?: "24 September 2021",
                    style = MaterialTheme.typography.caption.merge(FeederTypography.previewDate),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(start = 4.dp)
//                        .border(1.dp, Color.Cyan)
                )
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        MaterialTheme.typography.subtitle1.merge(FeederTypography.previewTitle.merge())
                            .toSpanStyle()
                    ) {
                        append(item.plainTitle)
                    }
                    append(" — ${item.plainSnippet}…")
                },
                style = MaterialTheme.typography.body1,
                overflow = TextOverflow.Ellipsis,
                maxLines = 5,
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.Top)
//                    .weight(weight = 1.0f, fill = true)
//                    .border(1.dp, Color.Green)
            )

        }

        item.imageUrl?.let { imageUrl ->
            val imageLoader: ImageLoader by instance()

            Image(
                painter = rememberCoilPainter(
                    request = imageUrl,
                    imageLoader = imageLoader,
                    shouldRefetchOnSizeChange = { _, _ -> false },
                ),
                contentScale = ContentScale.Crop,
                contentDescription = "Thumbnail for the article",
                modifier = Modifier
                    .width(64.dp)
//                    .height(IntrinsicSize.Min)
                    .fillMaxHeight() // TODO make this match parent height somehow
                    .padding(start = 4.dp)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun preview() =
    FeedItemPreview(
        item = PreviewItem(
            plainTitle = "title",
            plainSnippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
            feedTitle = "Super Duper Feed One two three hup di too dasf",
            pubDate = null
        )
    ) {

    }
