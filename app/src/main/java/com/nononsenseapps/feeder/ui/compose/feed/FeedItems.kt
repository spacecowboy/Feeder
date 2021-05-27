package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import org.kodein.di.compose.instance
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

private val shortDateTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

private val feedItemHeight = 128.dp

//@Composable
//fun FeedItemPreview(item: PreviewItem, onItemClick: () -> Unit) {
//    ClickableText(
//        text = buildAnnotatedString { append("${item.plainTitle} — ${item.plainSnippet}…") },
//        maxLines = 3,
//        overflow = TextOverflow.Ellipsis,
//        modifier = Modifier
//            .padding(end = 4.dp)
//    ) {
//        onItemClick()
//    }

//    ConstraintLayout(
//        modifier = Modifier
//            .clickable(onClick = onItemClick)
//            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
//            .fillMaxWidth()
//            .height(feedItemHeight)
//    ) {
//        val (refFeed, refDate, refImage, refText, refVertSpace) = createRefs()
//        val hasImage = item.imageUrl != null
//
//        item.imageUrl?.let { imageUrl ->
//            val imageLoader: ImageLoader by instance()
//
//            Image(
//                painter = rememberCoilPainter(
//                    request = imageUrl,
//                    imageLoader = imageLoader,
//                    shouldRefetchOnSizeChange = { _, _ -> false },
//                ),
//                contentDescription = "Thumbnail for the article",
//                modifier = Modifier
//                    .width(64.dp)
//                    .constrainAs(refImage) {
//                        end.linkTo(parent.end)
//                        top.linkTo(parent.top)
//                        bottom.linkTo(parent.bottom)
//                        height = Dimension.fillToConstraints
//                    },
//            )
//        }
//
//        Text(
//            text = item.pubDate?.toLocalDate()?.format(shortDateTimeFormat) ?: "",
//            maxLines = 1,
//            modifier = Modifier
//                .padding(start = 2.dp, end = 4.dp)
//                .constrainAs(refDate) {
//                    top.linkTo(parent.top)
//                    end.linkTo(if (hasImage) refImage.start else parent.end)
//                }
//        )
//
//        Text(
//            text = item.feedDisplayTitle,
//            maxLines = 1,
//            modifier = Modifier
//                .padding(end = 2.dp)
//                .constrainAs(refFeed) {
//                    top.linkTo(parent.top)
//                    start.linkTo(parent.start)
//                    end.linkTo(refDate.start)
//                    width = Dimension.fillToConstraints
//                }
//        )
//        Spacer(
//            modifier = Modifier
//                .size(4.dp)
//                .constrainAs(refVertSpace) {
//                    top.linkTo(refFeed.bottom)
//                    start.linkTo(parent.start)
//                    width = Dimension.value(50.dp)
//                    height = Dimension.value(4.dp)
//                }
//                .size(4.dp)
//        )
//        Text(
//            text = "${item.plainTitle} — ${item.plainSnippet}…",
//            maxLines = 5,
//            overflow = TextOverflow.Ellipsis,
//            modifier = Modifier
//                .padding(end = 4.dp)
//                .constrainAs(refText) {
//                    top.linkTo(refVertSpace.bottom)
//                    bottom.linkTo(parent.bottom)
//                    start.linkTo(parent.start)
//                    end.linkTo(if (hasImage) refImage.start else parent.end)
//                    width = Dimension.fillToConstraints
//                    height = Dimension.fillToConstraints
//                }
//        )
//    }
//}

@Composable
fun FeedItemPreview(item: PreviewItem, onItemClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
            .height(feedItemHeight)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxHeight()
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
                    .weight(weight = 1.0f, fill = true)
//                    .border(1.dp, Color.Green)
            )

        }
        if (item.imageUrl == null) {
            Spacer(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
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
                    .fillMaxHeight()
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
