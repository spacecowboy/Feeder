package com.nononsenseapps.feeder.ui.compose.readaloud

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.coil.RestrainedCropScaling
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme

@Composable
fun PlayListView() {
    // TODO a max size
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        PlayerView()
    }
}

@Composable
fun PlayerView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        ArticleImage()
        Divider()
        PlayProgress()
        PlayButtons()
        Divider()
        // TODO article list
    }
}

@Composable
fun ArticleImage() {
    val pixelDensity = LocalDensity.current.density
    val contentScale =
        remember(pixelDensity) {
            RestrainedCropScaling(pixelDensity)
        }
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(150.dp),
    ) {
        val pixels =
            with(LocalDensity.current) {
                Size(maxWidth.roundToPx(), maxHeight.roundToPx())
            }
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data("https://cowboyprogrammer.org/images/2017/10/gimp_image_mode_index.png")
                    .listener(
                        onError = { a, b ->
                            Log.e("FEEDER_PLAYLIST", "error ${a.data}", b.throwable)
                        },
                    )
                    .scale(Scale.FIT)
                    .size(pixels)
                    .precision(Precision.INEXACT)
                    .build(),
            placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
            error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
            contentDescription = stringResource(id = R.string.article_image),
            contentScale = contentScale,
            alignment = Alignment.Center,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .safeSemantics {
                        testTag = "article_image"
                    },
        )
    }
}

@Composable
fun PlayButtons() {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                Icons.Default.Speed,
                contentDescription = "TODO",
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                Icons.Default.Replay10,
                contentDescription = "TODO",
            )
        }
        IconButton(
            onClick = { /*TODO*/ },
        ) {
            Icon(
                Icons.Default.PauseCircleOutline,
                contentDescription = "TODO",
                modifier = Modifier.size(36.dp),
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                Icons.Default.Forward10,
                contentDescription = "TODO",
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                Icons.Default.Done,
                contentDescription = "TODO",
            )
        }
    }
}

@Composable
fun PlayProgress() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "0:11")
            Text(text = "-9:42")
        }
        Slider(value = 11f, valueRange = 0f..600f, onValueChange = {})
    }
}

@Preview
@Composable
private fun PreviewPlayListView() {
    FeederTheme {
        Surface {
            PlayListView()
        }
    }
}
