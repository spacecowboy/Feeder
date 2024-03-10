package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        // TODO item image with right scaling etc
        PlayProgress()
        PlayButtons()
        Divider()
        // TODO article list
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
