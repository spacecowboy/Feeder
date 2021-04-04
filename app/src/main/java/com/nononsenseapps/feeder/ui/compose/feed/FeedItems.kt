package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.coil.CoilImage
import com.nononsenseapps.feeder.model.PreviewItem
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

private val shortDateTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

private val feedItemHeight = 92.dp

@Composable
fun FeedItemPreview(item: PreviewItem, onItemClick: () -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .height(feedItemHeight)
    ) {
        val (refFeed, refDate, refImage, refText, refVertSpace) = createRefs()
        val hasImage = item.imageUrl != null

        item.imageUrl?.let { imageUrl ->
            // TODO use same image loader as in AsyncImageLoader
            CoilImage(
                data = imageUrl,
                contentDescription = "Thumbnail for the article",
                modifier = Modifier
                    .width(64.dp)
                    .constrainAs(refImage) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }
            )
        }

        Text(
            text = item.pubDate?.toLocalDate()?.format(shortDateTimeFormat) ?: "",
            maxLines = 1,
            modifier = Modifier
                .padding(start = 2.dp, end = 4.dp)
                .constrainAs(refDate) {
                    top.linkTo(parent.top)
                    end.linkTo(if (hasImage) refImage.start else parent.end)
                }
        )

        Text(
            text = item.feedDisplayTitle,
            maxLines = 1,
            modifier = Modifier
                .padding(end = 2.dp)
                .constrainAs(refFeed) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(refDate.start)
                    width = Dimension.fillToConstraints
                }
        )
        Spacer(
            modifier = Modifier
                .size(4.dp)
                .constrainAs(refVertSpace) {
                    top.linkTo(refFeed.bottom)
                    start.linkTo(parent.start)
                    width = Dimension.value(50.dp)
                    height = Dimension.value(4.dp)
                }
                .size(4.dp)
        )
        Text(
            text = "${item.plainTitle} — ${item.plainSnippet}…",
            maxLines = 5,
            modifier = Modifier
                .padding(end = 4.dp)
                .constrainAs(refText) {
                    top.linkTo(refVertSpace.bottom)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(if (hasImage) refImage.start else parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
        )
    }
}

@Composable
@Preview
private fun preview() =
    FeedItemPreview(
        item = PreviewItem(
            plainTitle = "title",
            plainSnippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going ",
            feedTitle = "Super Duper Feed",
            pubDate = null
        )
    ) {

    }
