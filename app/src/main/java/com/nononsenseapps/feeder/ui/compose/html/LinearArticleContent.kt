package com.nononsenseapps.feeder.ui.compose.html

import androidx.collection.ArrayMap
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.html.Coordinate
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearAudio
import com.nononsenseapps.feeder.model.html.LinearBlockQuote
import com.nononsenseapps.feeder.model.html.LinearElement
import com.nononsenseapps.feeder.model.html.LinearImage
import com.nononsenseapps.feeder.model.html.LinearImageSource
import com.nononsenseapps.feeder.model.html.LinearList
import com.nononsenseapps.feeder.model.html.LinearListItem
import com.nononsenseapps.feeder.model.html.LinearTable
import com.nononsenseapps.feeder.model.html.LinearTableCellItem
import com.nononsenseapps.feeder.model.html.LinearTableCellItemType
import com.nononsenseapps.feeder.model.html.LinearText
import com.nononsenseapps.feeder.model.html.LinearTextAnnotation
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationBold
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationCode
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationFont
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationH1
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationH2
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationH3
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationH4
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationH5
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationH6
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationItalic
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationLink
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationMonospace
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationStrikethrough
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationSubscript
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationSuperscript
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationUnderline
import com.nononsenseapps.feeder.model.html.LinearTextBlockStyle
import com.nononsenseapps.feeder.model.html.LinearVideo
import com.nononsenseapps.feeder.ui.compose.coil.RestrainedFillWidthScaling
import com.nononsenseapps.feeder.ui.compose.coil.RestrainedFitScaling
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.layouts.Table
import com.nononsenseapps.feeder.ui.compose.layouts.TableCell
import com.nononsenseapps.feeder.ui.compose.layouts.TableData
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.text.WithTooltipIfNotBlank
import com.nononsenseapps.feeder.ui.compose.text.asFontFamily
import com.nononsenseapps.feeder.ui.compose.text.rememberMaxImageWidth
import com.nononsenseapps.feeder.ui.compose.theme.CodeBlockBackground
import com.nononsenseapps.feeder.ui.compose.theme.CodeInlineStyle
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.LocalTypographySettings
import com.nononsenseapps.feeder.ui.compose.theme.OnCodeBlockBackground
import com.nononsenseapps.feeder.ui.compose.theme.hasImageAspectRatioInReader
import com.nononsenseapps.feeder.ui.compose.utils.ProvideScaledText
import com.nononsenseapps.feeder.ui.compose.utils.WithAllPreviewProviders
import com.nononsenseapps.feeder.ui.compose.utils.focusableInNonTouchMode
import com.nononsenseapps.feeder.util.logDebug
import kotlin.math.abs

private const val LOG_TAG = "FEEDER_LINEARCON"

fun LazyListScope.linearArticleContent(
    articleContent: LinearArticle,
    onLinkClick: (url: String, index: Int?) -> Unit,
) {
    items(
        count = articleContent.elements.size,
        contentType = { index -> articleContent.elements[index].lazyListContentType },
    ) { index ->
        ProvideTextStyle(
            MaterialTheme.typography.bodyLarge.merge(
                TextStyle(color = MaterialTheme.colorScheme.onBackground),
            ),
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                LinearElementContent(
                    linearElement = articleContent.elements[index],
                    idToIndex = articleContent.idToIndex,
                    allowHorizontalScroll = true,
                    onLinkClick = onLinkClick,
                    modifier =
                        Modifier
                            .widthIn(max = minOf(maxWidth, LocalDimens.current.maxReaderWidth))
                            .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun ColumnArticleContent(
    articleContent: LinearArticle,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
    onElementPosition: (index: Int, yPosition: Float) -> Unit = { _, _ -> },
) {
    articleContent.elements.forEachIndexed { index, element ->
        ProvideTextStyle(
            MaterialTheme.typography.bodyLarge.merge(
                TextStyle(color = MaterialTheme.colorScheme.onBackground),
            ),
        ) {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            onElementPosition(index, coordinates.positionInRoot().y)
                        },
                contentAlignment = Alignment.Center,
            ) {
                LinearElementContent(
                    linearElement = element,
                    idToIndex = articleContent.idToIndex,
                    allowHorizontalScroll = true,
                    onLinkClick = onLinkClick,
                    modifier =
                        Modifier
                            .widthIn(max = minOf(maxWidth, LocalDimens.current.maxReaderWidth))
                            .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun LinearElementContent(
    linearElement: LinearElement,
    allowHorizontalScroll: Boolean,
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (linearElement) {
//        is LinearList ->
//            LinearListContent(
//                linearList = linearElement,
//                allowHorizontalScroll = allowHorizontalScroll,
//                onLinkClick = onLinkClick,
//                modifier = modifier,
//                idToIndex = idToIndex,
//            )
        is LinearListItem ->
            LinearListItemContent(
                listItem = linearElement,
                allowHorizontalScroll = allowHorizontalScroll,
                onLinkClick = onLinkClick,
                modifier = modifier,
                idToIndex = idToIndex,
            )

        is LinearImage ->
            LinearImageContent(
                linearImage = linearElement,
                onLinkClick = onLinkClick,
                modifier = modifier,
                idToIndex = idToIndex,
            )

        is LinearBlockQuote -> {
            LinearBlockQuoteContent(
                blockQuote = linearElement,
                onLinkClick = onLinkClick,
                modifier = modifier,
                idToIndex = idToIndex,
            )
        }

        is LinearText ->
            when (linearElement.blockStyle) {
                LinearTextBlockStyle.TEXT -> {
                    LinearTextContent(
                        linearText = linearElement,
                        onLinkClick = onLinkClick,
                        idToIndex = idToIndex,
                        modifier = modifier,
                    )
                }

                LinearTextBlockStyle.PRE_FORMATTED -> {
//                    PreFormattedBlock(
                    CodeBlock(
                        linearText = linearElement,
                        allowHorizontalScroll = allowHorizontalScroll,
                        onLinkClick = onLinkClick,
                        modifier = modifier,
                        idToIndex = idToIndex,
                    )
                }

                LinearTextBlockStyle.CODE_BLOCK -> {
                    CodeBlock(
                        linearText = linearElement,
                        allowHorizontalScroll = allowHorizontalScroll,
                        onLinkClick = onLinkClick,
                        modifier = modifier,
                        idToIndex = idToIndex,
                    )
                }
            }

        is LinearTable ->
            LinearTableContent(
                linearTable = linearElement,
                allowHorizontalScroll = allowHorizontalScroll,
                onLinkClick = onLinkClick,
                modifier = modifier,
                idToIndex = idToIndex,
            )

        is LinearAudio ->
            LinearAudioContent(
                linearAudio = linearElement,
                onLinkClick = onLinkClick,
                modifier = modifier,
            )

        is LinearVideo ->
            LinearVideoContent(
                linearVideo = linearElement,
                onLinkClick = onLinkClick,
                modifier = modifier,
            )
    }
}

@Composable
fun LinearAudioContent(
    linearAudio: LinearAudio,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DisableSelection {
            ProvideScaledText(
                style =
                    MaterialTheme.typography.bodyLarge.merge(
                        LinkTextStyle(),
                    ),
            ) {
                Text(
                    text = stringResource(R.string.touch_to_play_audio),
                    modifier =
                        Modifier.clickable {
                            onLinkClick(linearAudio.firstSource.uri, null)
                        },
                )
            }
        }
    }
}

@Composable
fun LinearVideoContent(
    linearVideo: LinearVideo,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DisableSelection {
            if (linearVideo.imageThumbnail != null) {
                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .clip(RectangleShape)
                            .clickable {
                                linearVideo.firstSource.link.let { onLinkClick(it, null) }
                            }.fillMaxWidth(),
                ) {
                    val maxImageWidth by rememberMaxImageWidth()
                    val pixelDensity = LocalDensity.current.density

                    val imageWidth: Int =
                        remember(linearVideo.firstSource) {
                            when {
                                linearVideo.firstSource.widthPx != null -> linearVideo.firstSource.widthPx!!
                                else -> maxImageWidth
                            }
                        }
                    val imageHeight: Int =
                        remember(linearVideo.firstSource) {
                            when {
                                linearVideo.firstSource.heightPx != null -> linearVideo.firstSource.heightPx!!
                                else -> imageWidth
                            }
                        }
                    val dimens = LocalDimens.current

                    val contentScale =
                        remember(pixelDensity, dimens.hasImageAspectRatioInReader) {
                            if (dimens.hasImageAspectRatioInReader) {
                                RestrainedFitScaling(pixelDensity)
                            } else {
                                RestrainedFillWidthScaling(pixelDensity)
                            }
                        }

                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(linearVideo.imageThumbnail)
                                .scale(Scale.FIT)
                                // DO NOT use the actualSize parameter here
                                .size(Size(imageWidth, imageHeight))
                                // If image is larger than requested size, scale down
                                // But if image is smaller, don't scale up
                                // Note that this is the pixels, not how it is scaled inside the ImageView
                                .precision(Precision.INEXACT)
                                .build(),
                        contentDescription = stringResource(R.string.touch_to_play_video),
                        placeholder =
                            rememberTintedVectorPainter(
                                Icons.Outlined.PlayCircleOutline,
                            ),
                        error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                        contentScale = contentScale,
                        modifier =
                            Modifier
                                .widthIn(max = maxWidth)
                                .fillMaxWidth(),
                    )
                }
            }

            ProvideScaledText(
                style =
                    MaterialTheme.typography.bodyLarge.merge(
                        LinkTextStyle(),
                    ),
            ) {
                Text(
                    text = stringResource(R.string.touch_to_play_video),
                    modifier =
                        Modifier.clickable {
                            onLinkClick(linearVideo.firstSource.link, null)
                        },
                )
            }
        }
    }
}

@Composable
fun LinearListContent(
    linearList: LinearList,
    allowHorizontalScroll: Boolean,
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        linearList.items.forEachIndexed { itemIndex, item ->
            LinearListItemContent(
                listItem = item,
                allowHorizontalScroll = allowHorizontalScroll,
                idToIndex = idToIndex,
                onLinkClick = onLinkClick,
            )
        }
    }
}

@Composable
fun LinearListItemContent(
    listItem: LinearListItem,
    allowHorizontalScroll: Boolean,
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        // List item indicator here
        if (listItem.orderedIndex != null) {
            Text("${listItem.orderedIndex}.")
        } else {
            Text("•")
        }

        // Then the item content
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            listItem.content.forEach { element ->
                LinearElementContent(
                    linearElement = element,
                    allowHorizontalScroll = allowHorizontalScroll,
                    onLinkClick = onLinkClick,
                    idToIndex = idToIndex,
                )
            }
        }
    }
}

@Composable
fun LinearImageContent(
    linearImage: LinearImage,
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (linearImage.sources.isEmpty()) {
        return
    }

    val dimens = LocalDimens.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        DisableSelection {
            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .clip(RectangleShape)
                        .clickable(
                            enabled = linearImage.link != null,
                        ) {
                            linearImage.link?.let {
                                val hashSplit = it.split("#")
                                val index =
                                    when {
                                        hashSplit.size > 1 -> idToIndex[hashSplit.last()]
                                        else -> null
                                    }
                                onLinkClick(it, index)
                            }
                        }.fillMaxWidth(),
            ) {
                val maxImageWidth by rememberMaxImageWidth()
                val pixelDensity = LocalDensity.current.density
                val bestImage =
                    remember {
                        linearImage.getBestImageForMaxSize(
                            pixelDensity = pixelDensity,
                            maxWidth = maxImageWidth,
                        )
                    } ?: return@BoxWithConstraints

                val imageWidth: Int =
                    remember(bestImage) {
                        when {
                            bestImage.pixelDensity != null -> maxImageWidth
                            bestImage.screenWidth != null -> bestImage.screenWidth
                            bestImage.widthPx != null -> bestImage.widthPx
                            else -> maxImageWidth
                        }
                    }
                val imageHeight: Int? =
                    remember(bestImage) {
                        when {
                            bestImage.heightPx != null -> bestImage.heightPx
                            else -> null
                        }
                    }

                WithTooltipIfNotBlank(tooltip = linearImage.caption?.text ?: "") {
                    val contentScale =
                        remember(pixelDensity, dimens.hasImageAspectRatioInReader) {
                            if (dimens.hasImageAspectRatioInReader) {
                                RestrainedFitScaling(pixelDensity)
                            } else {
                                RestrainedFillWidthScaling(pixelDensity)
                            }
                        }

                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(bestImage.imgUri)
                                .scale(Scale.FIT)
                                // DO NOT use the actualSize parameter here
                                .size(Size(imageWidth, imageHeight ?: imageWidth))
                                // If image is larger than requested size, scale down
                                // But if image is smaller, don't scale up
                                // Note that this is the pixels, not how it is scaled inside the ImageView
                                .precision(Precision.INEXACT)
                                .build(),
                        contentDescription = linearImage.caption?.text,
                        placeholder =
                            rememberTintedVectorPainter(
                                Icons.Outlined.Terrain,
                            ),
                        error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                        contentScale = contentScale,
                        modifier =
                            Modifier
                                .widthIn(max = maxWidth)
                                .fillMaxWidth(),
                    )
                }
            }
        }

        linearImage.caption?.let { caption ->
            ProvideTextStyle(
                LocalTextStyle.current.merge(
                    MaterialTheme.typography.labelMedium.merge(
                        TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    ),
                ),
            ) {
                LinearTextContent(
                    linearText = caption,
                    idToIndex = idToIndex,
                    onLinkClick = onLinkClick,
                )
            }
        }
    }
}

private fun LinearImage.getBestImageForMaxSize(
    pixelDensity: Float,
    maxWidth: Int,
): LinearImageSource? =
    sources.minByOrNull { candidate ->
        val candidateSize =
            when {
                candidate.pixelDensity != null -> candidate.pixelDensity / pixelDensity
                candidate.screenWidth != null -> candidate.screenWidth / maxWidth.toFloat()
                candidate.widthPx != null -> candidate.widthPx / maxWidth.toFloat()
                // Assume it corresponds to 1x pixel density
                else -> 1.0f / pixelDensity
            }

        abs(candidateSize - 1.0f)
    }

@Composable
fun LinearTextContent(
    linearText: LinearText,
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
    softWrap: Boolean = true,
) {
    ProvideScaledText {
        WithBidiDeterminedLayoutDirection(linearText.text) {
            val interactionSource = remember { MutableInteractionSource() }
            val annotatedString = linearText.toAnnotatedString(idToIndex = idToIndex, onLinkClick = onLinkClick)

            Text(
                text = annotatedString,
                softWrap = softWrap,
                modifier =
                    modifier
                        .indication(interactionSource, LocalIndication.current)
                        .focusableInNonTouchMode(interactionSource = interactionSource),
            )
        }
    }
}

@Composable
fun LinearBlockQuoteContent(
    blockQuote: LinearBlockQuote,
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier =
            modifier
                .padding(start = 8.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(8.dp),
        ) {
            blockQuote.content
                .filterIsInstance<LinearText>()
                .forEach { element ->
                    ProvideTextStyle(
                        MaterialTheme.typography.bodyLarge.merge(
                            SpanStyle(
                                fontWeight = FontWeight.Light,
                            ),
                        ),
                    ) {
                        LinearTextContent(
                            linearText = element,
                            idToIndex = idToIndex,
                            onLinkClick = onLinkClick,
                        )
                    }
                }

            blockQuote.cite?.let { cite ->
                val annotatedText =
                    buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = cite,
                                styles =
                                    TextLinkStyles(
                                        style =
                                            MaterialTheme.typography.bodySmall.toSpanStyle().merge(
                                                SpanStyle(
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontStyle = FontStyle.Italic,
                                                ),
                                            ),
                                    ),
                                linkInteractionListener = {
                                    val hashSplit = cite.split("#")
                                    val index =
                                        when {
                                            hashSplit.size > 1 -> idToIndex[hashSplit.last()]
                                            else -> null
                                        }
                                    onLinkClick(cite, index)
                                },
                            ),
                        ) {
                            append(cite)
                        }
                    }

                Text(
                    text = annotatedText,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

// @Composable
// fun PreFormattedBlock(
//    linearText: LinearText,
//    allowHorizontalScroll: Boolean,
//    modifier: Modifier = Modifier,
//    onLinkClick: (url: String, index: Int?) -> Unit,
// ) {
//    val scrollState = rememberScrollState()
//    val interactionSource =
//        remember { MutableInteractionSource() }
//    Surface(
//        color = MaterialTheme.colorScheme.surface,
//        shape = MaterialTheme.shapes.medium,
//        modifier =
//        modifier
//            .run {
//                if (allowHorizontalScroll) {
//                    horizontalScroll(scrollState)
//                } else {
//                    this
//                }
//            }
//            .indication(
//                interactionSource,
//                LocalIndication.current,
//            )
//            .focusableInNonTouchMode(interactionSource = interactionSource)
//    ) {
//        Box(modifier = Modifier.padding(all = 4.dp)) {
//            ProvideTextStyle(
//                MaterialTheme.typography.bodyLarge.merge(
//                    MaterialTheme.colorScheme.onSurface,
//                ),
//            ) {
//                LinearTextContent(
//                    linearText = linearText,
//                    onLinkClick = onLinkClick,
//                    softWrap = false,
//                )
//            }
//        }
//    }
// }

@Composable
fun CodeBlock(
    linearText: LinearText,
    allowHorizontalScroll: Boolean,
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val interactionSource =
        remember { MutableInteractionSource() }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart,
    ) {
        Surface(
            color = CodeBlockBackground(),
            shape = MaterialTheme.shapes.medium,
            modifier =
                Modifier
                    .run {
                        if (allowHorizontalScroll) {
                            horizontalScroll(scrollState)
                        } else {
                            this
                        }
                    }.indication(
                        interactionSource,
                        LocalIndication.current,
                    ).focusableInNonTouchMode(interactionSource = interactionSource),
        ) {
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier.padding(8.dp),
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyLarge.merge(
                        OnCodeBlockBackground(),
                    ),
                ) {
                    LinearTextContent(
                        linearText = linearText,
                        idToIndex = idToIndex,
                        onLinkClick = onLinkClick,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
fun LinearTableContent(
    linearTable: LinearTable,
    allowHorizontalScroll: Boolean,
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Table(
        tableData = linearTable.toTableData(),
        modifier = modifier,
        allowHorizontalScroll = allowHorizontalScroll,
        content = { row, column ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
                modifier =
                    Modifier
                        .background(
                            // if table contains image, don't color rows alternatively
                            if (row % 2 == 0 || linearTable.cells.values.any { cell -> cell.content.any { it is LinearImage } }) {
                                MaterialTheme.colorScheme.background
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        )
//                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        .drawWithContent {
                            drawContent()
//                            if (row < linearTable.rowCount - 1) {
//                                drawLine(
//                                    color = borderColor,
//                                    strokeWidth = 1.dp.toPx(),
//                                    start = Offset(0f, size.height),
//                                    end = Offset(size.width, size.height),
//                                )
//                            }
                            // As a side effect, only draws borders if more than one column which is good
                            if (column < linearTable.colCount - 1) {
                                drawLine(
                                    color = borderColor,
                                    strokeWidth = 1.dp.toPx(),
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                )
                            }
                        }.padding(4.dp),
            ) {
                val cellItem = linearTable.cellAt(row = row, col = column)
                cellItem?.let {
                    ProvideTextStyle(
                        value =
                            if (cellItem.type == LinearTableCellItemType.HEADER) {
                                MaterialTheme.typography.bodyLarge.merge(
                                    TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color =
                                            if (row % 2 == 0) {
                                                MaterialTheme.colorScheme.onBackground
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    ),
                                )
                            } else {
                                MaterialTheme.typography.bodyLarge.merge(
                                    TextStyle(
                                        color =
                                            if (row % 2 == 0) {
                                                MaterialTheme.colorScheme.onBackground
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                    ),
                                )
                            },
                    ) {
                        for (element in it.content) {
                            LinearElementContent(
                                linearElement = element,
                                allowHorizontalScroll = false,
                                onLinkClick = onLinkClick,
                                modifier = Modifier.fillMaxWidth(),
                                idToIndex = idToIndex,
                            )
                        }
                    }
                }
            }
        },
    )
}

val LinearElement.lazyListContentType: String
    get() =
        when (this) {
//            is LinearList -> "LinearList"
            is LinearListItem -> "LinearListItem"
            is LinearImage -> "LinearImage"
            is LinearText -> "LinearText"
            is LinearTable -> "LinearTable"
            is LinearBlockQuote -> "LinearBlockQuote"
            is LinearAudio -> "LinearAudio"
            is LinearVideo -> "LinearVideo"
        }

@Composable
fun LinearText.toAnnotatedString(
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, index: Int?) -> Unit,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(text)
    annotations.forEach { annotation ->
        when (val data = annotation.data) {
            LinearTextAnnotationBold -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                )
            }

            is LinearTextAnnotationFont -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = SpanStyle(fontFamily = data.face.asFontFamily(LocalTypographySettings.current)),
                )
            }

            LinearTextAnnotationH1,
            LinearTextAnnotationH2,
            LinearTextAnnotationH3,
            LinearTextAnnotationH4,
            LinearTextAnnotationH5,
            LinearTextAnnotationH6,
            -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = MaterialTheme.typography.headlineSmall.toSpanStyle(),
                )
            }

            LinearTextAnnotationItalic -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = SpanStyle(fontStyle = FontStyle.Italic),
                )
            }

            is LinearTextAnnotationLink -> {
                builder.addLink(
                    clickable =
                        LinkAnnotation.Clickable(
                            tag = data.href,
                            styles =
                                TextLinkStyles(
                                    style = LinkTextStyle().toSpanStyle(),
                                ),
                            linkInteractionListener = {
                                // Looks like data.href=http://www.example.com/feed#footnote-1
                                val hashSplit = data.href.split("#")
                                val index =
                                    when {
                                        hashSplit.size > 1 -> idToIndex[hashSplit.last()]
                                        else -> null
                                    }
                                logDebug(LOG_TAG, "Link clicked: ${data.href}, index: $index")
                                onLinkClick(data.href, index)
                            },
                        ),
                    start = annotation.start,
                    end = annotation.endExclusive,
                )
            }

            LinearTextAnnotationMonospace -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = SpanStyle(fontFamily = LocalTypographySettings.current.monoFontFamily),
                )
            }

            LinearTextAnnotationCode -> {
                // Code blocks are already styled on the block level
                if (blockStyle != LinearTextBlockStyle.CODE_BLOCK) {
                    builder.addStyle(
                        start = annotation.start,
                        end = annotation.endExclusive,
                        style = CodeInlineStyle(),
                    )
                }
            }

            LinearTextAnnotationSubscript -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = SpanStyle(baselineShift = BaselineShift.Subscript),
                )
            }

            LinearTextAnnotationSuperscript -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = SpanStyle(baselineShift = BaselineShift.Superscript),
                )
            }

            LinearTextAnnotationUnderline -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = SpanStyle(textDecoration = TextDecoration.Underline),
                )
            }

            LinearTextAnnotationStrikethrough -> {
                builder.addStyle(
                    start = annotation.start,
                    end = annotation.endExclusive,
                    style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                )
            }
        }
    }
    return builder.toAnnotatedString()
}

@Composable
private fun PreviewContent(element: LinearElement) {
    WithAllPreviewProviders {
        Surface {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(8.dp),
            ) {
                LinearElementContent(
                    linearElement = element,
                    allowHorizontalScroll = true,
                    onLinkClick = { _, _ -> },
                    idToIndex = emptyMap(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTextElement() {
    val linearText =
        LinearText(
            ids = emptySet(),
            text = "Hello, world future!",
            blockStyle = LinearTextBlockStyle.TEXT,
            LinearTextAnnotation(
                data = LinearTextAnnotationStrikethrough,
                start = 7,
                end = 12,
            ),
            LinearTextAnnotation(
                data = LinearTextAnnotationUnderline,
                start = 14,
                end = 20,
            ),
        )

    PreviewContent(linearText)
}

@PreviewLightDark
@Composable
private fun PreviewBlockQuote() {
    val blockQuote =
        LinearBlockQuote(
            ids = emptySet(),
            cite = "https://example.com",
            content =
                listOf(
                    LinearText(
                        ids = emptySet(),
                        text = "This is a block quote",
                        blockStyle = LinearTextBlockStyle.TEXT,
                    ),
                ),
        )

    PreviewContent(blockQuote)
}

@PreviewLightDark
@Composable
private fun PreviewCodeBlock() {
    val codeBlock =
        LinearText(
            ids = emptySet(),
            text = "fun main() {\n    println(\"Hello, world!\")\n}",
            blockStyle = LinearTextBlockStyle.CODE_BLOCK,
        )

    PreviewContent(codeBlock)
}

@PreviewLightDark
@Composable
private fun PreviewPreFormatted() {
    val preFormatted =
        LinearText(
            ids = emptySet(),
            text = "This is pre-formatted text\n    with some indentation",
            blockStyle = LinearTextBlockStyle.PRE_FORMATTED,
        )

    PreviewContent(preFormatted)
}

@PreviewLightDark
@Composable
private fun PreviewLinearOrderedListContent() {
    PreviewContent(
        LinearListItem(
            ids = emptySet(),
            orderedIndex = 1,
            content =
                listOf(
                    LinearText(
                        ids = emptySet(),
                        text = "List Item 1",
                        blockStyle = LinearTextBlockStyle.TEXT,
                    ),
                ),
        ),
    )
}

@PreviewLightDark
@Composable
private fun PreviewLinearUnorderedListContent() {
    PreviewContent(
        LinearListItem(
            ids = emptySet(),
            orderedIndex = null,
            content =
                listOf(
                    LinearText(
                        ids = emptySet(),
                        text = "List Item 1",
                        blockStyle = LinearTextBlockStyle.TEXT,
                    ),
                ),
        ),
    )
}

@PreviewLightDark
@Composable
private fun PreviewLinearImageContent() {
    val linearImage =
        LinearImage(
            ids = emptySet(),
            sources =
                listOf(
                    LinearImageSource(
                        imgUri = "https://example.com/image.jpg",
                        widthPx = 200,
                        heightPx = 200,
                        pixelDensity = 1f,
                        screenWidth = 200,
                    ),
                ),
            caption =
                LinearText(
                    ids = emptySet(),
                    text = "This is an image caption",
                    blockStyle = LinearTextBlockStyle.TEXT,
                ),
            link = "https://example.com/image.jpg",
        )

    PreviewContent(linearImage)
}

@PreviewLightDark
@Composable
private fun PreviewLinearTableContent() {
    val linearTable =
        LinearTable(
            ids = emptySet(),
            rowCount = 2,
            colCount = 2,
            leftToRight = false,
            cells =
                listOf(
                    LinearTableCellItem(
                        type = LinearTableCellItemType.HEADER,
                        colSpan = 1,
                        rowSpan = 1,
                        content =
                            listOf(
                                LinearText(
                                    ids = emptySet(),
                                    text = "Cell 1",
                                    blockStyle = LinearTextBlockStyle.TEXT,
                                ),
                            ),
                    ),
                    LinearTableCellItem(
                        type = LinearTableCellItemType.DATA,
                        colSpan = 1,
                        rowSpan = 1,
                        content =
                            listOf(
                                LinearText(
                                    ids = emptySet(),
                                    text = "Cell 2",
                                    blockStyle = LinearTextBlockStyle.TEXT,
                                ),
                            ),
                    ),
                    LinearTableCellItem(
                        type = LinearTableCellItemType.HEADER,
                        colSpan = 1,
                        rowSpan = 1,
                        content =
                            listOf(
                                LinearText(
                                    ids = emptySet(),
                                    text = "Cell 3",
                                    blockStyle = LinearTextBlockStyle.TEXT,
                                ),
                            ),
                    ),
                    LinearTableCellItem(
                        type = LinearTableCellItemType.DATA,
                        colSpan = 1,
                        rowSpan = 1,
                        content =
                            listOf(
                                LinearText(
                                    ids = emptySet(),
                                    text = "Cell 4",
                                    blockStyle = LinearTextBlockStyle.TEXT,
                                ),
                            ),
                    ),
                ),
        )

    PreviewContent(linearTable)
}

@Preview
@Composable
private fun PreviewNestedTableContent() {
    val linearTable =
        LinearTable(
            ids = emptySet(),
            rowCount = 2,
            colCount = 2,
            leftToRight = false,
            cells =
                listOf(
                    LinearTableCellItem(
                        colSpan = 1,
                        rowSpan = 1,
                        type = LinearTableCellItemType.DATA,
                        content =
                            listOf(
                                LinearImage(
                                    ids = emptySet(),
                                    sources =
                                        listOf(
                                            LinearImageSource(
                                                imgUri = "https://example.com/image.jpg",
                                                widthPx = null,
                                                heightPx = null,
                                                pixelDensity = null,
                                                screenWidth = null,
                                            ),
                                        ),
                                    caption =
                                        LinearText(
                                            ids = emptySet(),
                                            text = "This is an image caption",
                                            blockStyle = LinearTextBlockStyle.TEXT,
                                        ),
                                    link = "https://example.com/image.jpg",
                                ),
                            ),
                    ),
                    LinearTableCellItem(
                        colSpan = 1,
                        rowSpan = 1,
                        type = LinearTableCellItemType.DATA,
                        content =
                            listOf(
                                LinearListItem(
                                    ids = emptySet(),
                                    orderedIndex = 1,
                                    content =
                                        listOf(
                                            LinearText(
                                                ids = emptySet(),
                                                text = "List Item 1",
                                                blockStyle = LinearTextBlockStyle.TEXT,
                                            ),
                                        ),
                                ),
                                LinearListItem(
                                    ids = emptySet(),
                                    orderedIndex = 2,
                                    content =
                                        listOf(
                                            LinearText(
                                                ids = emptySet(),
                                                text = "List Item 2",
                                                blockStyle = LinearTextBlockStyle.TEXT,
                                            ),
                                        ),
                                ),
                                LinearListItem(
                                    ids = emptySet(),
                                    orderedIndex = 3,
                                    content =
                                        listOf(
                                            LinearText(
                                                ids = emptySet(),
                                                text = "List Item 3",
                                                blockStyle = LinearTextBlockStyle.TEXT,
                                            ),
                                        ),
                                ),
                            ),
                    ),
                    LinearTableCellItem(
                        colSpan = 1,
                        rowSpan = 1,
                        type = LinearTableCellItemType.DATA,
                        content =
                            listOf(
                                LinearText(
                                    ids = emptySet(),
                                    text = "fun main() {\n    println(\"Hello, world!\")\n}",
                                    blockStyle = LinearTextBlockStyle.CODE_BLOCK,
                                ),
                            ),
                    ),
                    LinearTableCellItem(
                        colSpan = 1,
                        rowSpan = 1,
                        type = LinearTableCellItemType.DATA,
                        content =
                            listOf(
                                LinearTable(
                                    ids = emptySet(),
                                    rowCount = 2,
                                    colCount = 2,
                                    leftToRight = false,
                                    cells =
                                        listOf(
                                            LinearTableCellItem(
                                                colSpan = 1,
                                                rowSpan = 1,
                                                type = LinearTableCellItemType.HEADER,
                                                content =
                                                    listOf(
                                                        LinearText(
                                                            ids = emptySet(),
                                                            text = "Cell 1",
                                                            blockStyle = LinearTextBlockStyle.TEXT,
                                                        ),
                                                    ),
                                            ),
                                            LinearTableCellItem(
                                                colSpan = 1,
                                                rowSpan = 1,
                                                type = LinearTableCellItemType.HEADER,
                                                content =
                                                    listOf(
                                                        LinearText(
                                                            ids = emptySet(),
                                                            text = "Cell 2",
                                                            blockStyle = LinearTextBlockStyle.TEXT,
                                                        ),
                                                    ),
                                            ),
                                            LinearTableCellItem(
                                                colSpan = 1,
                                                rowSpan = 1,
                                                type = LinearTableCellItemType.DATA,
                                                content =
                                                    listOf(
                                                        LinearText(
                                                            ids = emptySet(),
                                                            text = "Cell 3",
                                                            blockStyle = LinearTextBlockStyle.TEXT,
                                                        ),
                                                    ),
                                            ),
                                            LinearTableCellItem(
                                                colSpan = 1,
                                                rowSpan = 1,
                                                type = LinearTableCellItemType.DATA,
                                                content =
                                                    listOf(
                                                        LinearText(
                                                            ids = emptySet(),
                                                            text = "Cell 4",
                                                            blockStyle = LinearTextBlockStyle.TEXT,
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            ),
                    ),
                ),
        )

    PreviewContent(element = linearTable)
}

@Preview
@Composable
private fun PreviewColSpanningTable() {
    val linearTable =
        LinearTable(
            ids = emptySet(),
            rowCount = 2,
            colCount = 2,
            cellsReal =
                ArrayMap<Coordinate, LinearTableCellItem>().apply {
                    putAll(
                        listOf(
                            Coordinate(row = 0, col = 0) to
                                LinearTableCellItem(
                                    type = LinearTableCellItemType.HEADER,
                                    colSpan = 2,
                                    rowSpan = 1,
                                    content =
                                        listOf(
                                            LinearText(
                                                ids = emptySet(),
                                                text = "Header 1 and 2",
                                                blockStyle = LinearTextBlockStyle.TEXT,
                                            ),
                                        ),
                                ),
                            Coordinate(row = 1, col = 0) to
                                LinearTableCellItem(
                                    type = LinearTableCellItemType.DATA,
                                    colSpan = 1,
                                    rowSpan = 1,
                                    content =
                                        listOf(
                                            LinearText(
                                                ids = emptySet(),
                                                text = "Cell 1",
                                                blockStyle = LinearTextBlockStyle.TEXT,
                                            ),
                                        ),
                                ),
                            Coordinate(row = 1, col = 1) to
                                LinearTableCellItem(
                                    type = LinearTableCellItemType.DATA,
                                    colSpan = 1,
                                    rowSpan = 1,
                                    content =
                                        listOf(
                                            LinearText(
                                                ids = emptySet(),
                                                text = "Cell 2",
                                                blockStyle = LinearTextBlockStyle.TEXT,
                                            ),
                                        ),
                                ),
                        ),
                    )
                },
        )

    PreviewContent(linearTable)
}

fun LinearTable.toTableData(): TableData =
    TableData.fromCells(
        cells =
            cells
                .asSequence()
                .filterNot { (_, cell) -> cell.isFiller }
                .map { (coord, cell) ->
                    TableCell(
                        row = coord.row,
                        column = coord.col,
                        colSpan =
                            if (cell.colSpan == 0) {
                                colCount - coord.col
                            } else {
                                cell.colSpan
                            },
                        rowSpan =
                            if (cell.rowSpan == 0) {
                                rowCount - coord.row
                            } else {
                                cell.rowSpan
                            },
                    )
                }.toList(),
    )
