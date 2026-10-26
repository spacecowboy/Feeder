package com.nononsenseapps.feeder.ui.compose.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Scale
import com.nononsenseapps.feeder.R

/** Keeps the linked-image choice local, preserving the reader's original link callback. */
@Composable
fun rememberImageViewer(
    imageUrl: String,
    description: String?,
    onOpenLink: (() -> Unit)? = null,
): () -> Unit {
    var showChoice by remember(imageUrl) { mutableStateOf(false) }
    var showViewer by remember(imageUrl) { mutableStateOf(false) }
    if (showChoice) {
        AlertDialog(
            onDismissRequest = { showChoice = false },
            title = { Text(stringResource(R.string.article_image)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showChoice = false
                        showViewer = true
                    },
                ) {
                    Text(stringResource(R.string.view_image))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChoice = false
                        onOpenLink?.invoke()
                    },
                ) {
                    Text(stringResource(R.string.open_image_link))
                }
            },
        )
    }
    if (showViewer) {
        ImageViewer(
            imageUrl = imageUrl,
            description = description,
            onDismiss = { showViewer = false },
        )
    }
    return {
        if (onOpenLink == null) {
            showViewer = true
        } else {
            showChoice = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageViewer(
    imageUrl: String,
    description: String?,
    onDismiss: () -> Unit,
) {
    var transform by remember(imageUrl) { mutableStateOf(ImageTransform()) }
    var viewport by remember { mutableStateOf(Size.Zero) }
    var imageSize by remember(imageUrl) { mutableStateOf(Size.Zero) }
    var attempt by remember(imageUrl) { mutableIntStateOf(0) }
    val context = LocalContext.current
    val request =
        remember(imageUrl, attempt, context) {
            ImageRequest
                .Builder(context)
                .data(imageUrl)
                // Reuse the application's loader, cache and network policy, including its decode cap.
                .size(2500, 2500)
                .scale(Scale.FIT)
                .precision(Precision.INEXACT)
                .crossfade(false)
                .build()
        }

    fun zoom(factor: Float) {
        transform =
            transform.transformed(
                zoom = factor,
                pan = Offset.Zero,
                centroid = Offset(viewport.width / 2, viewport.height / 2),
                viewport = viewport,
                image = imageSize,
            )
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.article_image)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.image_close),
                            )
                        }
                    },
                )
            },
            bottomBar = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(8.dp),
                ) {
                    IconButton(
                        onClick = { zoom(0.5f) },
                        enabled = imageSize != Size.Zero && transform.scale > 1f,
                    ) {
                        Icon(
                            Icons.Outlined.ZoomOut,
                            contentDescription = stringResource(R.string.image_zoom_out),
                        )
                    }
                    IconButton(
                        onClick = { transform = ImageTransform() },
                        enabled = imageSize != Size.Zero,
                    ) {
                        Icon(
                            Icons.Outlined.RestartAlt,
                            contentDescription = stringResource(R.string.image_reset_zoom),
                        )
                    }
                    IconButton(
                        onClick = { zoom(2f) },
                        enabled = imageSize != Size.Zero && transform.scale < 8f,
                    ) {
                        Icon(
                            Icons.Outlined.ZoomIn,
                            contentDescription = stringResource(R.string.image_zoom_in),
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .clipToBounds()
                        .onSizeChanged {
                            viewport = Size(it.width.toFloat(), it.height.toFloat())
                            transform =
                                transform.transformed(
                                    zoom = 1f,
                                    pan = Offset.Zero,
                                    centroid = Offset.Zero,
                                    viewport = viewport,
                                    image = imageSize,
                                )
                        }.semantics {
                            stateDescription = "${(transform.scale * 100).toInt()}%"
                        }.pointerInput(imageUrl, viewport, imageSize) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                transform =
                                    transform.transformed(
                                        zoom = zoom,
                                        pan = pan,
                                        centroid = centroid,
                                        viewport = viewport,
                                        image = imageSize,
                                    )
                            }
                        }.pointerInput(imageUrl, viewport, imageSize) {
                            detectTapGestures(
                                onDoubleTap = { position ->
                                    transform =
                                        if (transform.scale > 1f) {
                                            ImageTransform()
                                        } else {
                                            transform.transformed(
                                                zoom = 3f,
                                                pan = Offset.Zero,
                                                centroid = position,
                                                viewport = viewport,
                                                image = imageSize,
                                            )
                                        }
                                },
                            )
                        },
            ) {
                key(attempt) {
                    SubcomposeAsyncImage(
                        model = request,
                        contentDescription = description ?: stringResource(R.string.article_image),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                CircularProgressIndicator(Modifier.size(48.dp))
                            }
                        },
                        error = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(stringResource(R.string.image_load_failed))
                                TextButton(onClick = { attempt++ }) {
                                    Text(stringResource(R.string.image_retry))
                                }
                            }
                        },
                        onSuccess = { state ->
                            val image = state.result.image
                            imageSize = Size(image.width.toFloat(), image.height.toFloat())
                        },
                        success = { state ->
                            Image(
                                painter = state.painter,
                                contentDescription = description ?: stringResource(R.string.article_image),
                                contentScale = ContentScale.Fit,
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = transform.scale
                                            scaleY = transform.scale
                                            translationX = transform.offset.x
                                            translationY = transform.offset.y
                                        },
                            )
                        },
                    )
                }
            }
        }
    }
}

/** Image coordinates stay under the gesture centroid; bounds use the fitted image, not its letterbox. */
internal data class ImageTransform(
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
) {
    fun transformed(
        zoom: Float,
        pan: Offset,
        centroid: Offset,
        viewport: Size,
        image: Size,
    ): ImageTransform {
        if (viewport.width <= 0 || viewport.height <= 0 || image.width <= 0 || image.height <= 0) {
            return ImageTransform()
        }
        val nextScale = (scale * zoom).coerceIn(1f, 8f)
        if (nextScale == 1f) {
            return ImageTransform()
        }
        val ratio = nextScale / scale
        val center = Offset(viewport.width / 2, viewport.height / 2)
        val nextOffset = offset * ratio + (centroid - center) * (1 - ratio) + pan
        val fit = minOf(viewport.width / image.width, viewport.height / image.height)
        val maxX = ((image.width * fit * nextScale - viewport.width) / 2).coerceAtLeast(0f)
        val maxY = ((image.height * fit * nextScale - viewport.height) / 2).coerceAtLeast(0f)
        return ImageTransform(
            scale = nextScale,
            offset =
                Offset(
                    nextOffset.x.coerceIn(-maxX, maxX),
                    nextOffset.y.coerceIn(-maxY, maxY),
                ),
        )
    }
}
