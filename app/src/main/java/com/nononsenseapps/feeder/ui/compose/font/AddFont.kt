package com.nononsenseapps.feeder.ui.compose.font

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.feed.PlainTooltipBox
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.utils.LocalWindowSizeMetrics
import com.nononsenseapps.feeder.ui.compose.utils.PreviewThemes
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import com.nononsenseapps.feeder.ui.compose.utils.getScreenType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFontScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSize = LocalWindowSizeMetrics.current
    val screenType by remember(windowSize) {
        derivedStateOf {
            getScreenType(windowSize)
        }
    }

    Scaffold(
        modifier =
            modifier
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SensibleTopAppBar(
                title = "Add font",
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            PlainTooltipBox(tooltip = { Text("Add font") }) {
                FloatingActionButton(
                    onClick = {},
                    modifier =
                        Modifier
                            .navigationBarsPadding(),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add font",
                    )
                }
            }
        }
    ) { padding ->
        AddFontView(
            screenType = screenType,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun AddFontView(
    screenType: ScreenType,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(modifier),
    ) {
        if (screenType == ScreenType.DUAL) {
            AddFontDualPane()
        } else {
            AddFontSinglePane()
        }
    }
}

@Composable
fun AddFontSinglePane(
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimens.current
    val scrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(horizontal = dimens.margin, vertical = 8.dp)
                .width(dimens.maxContentWidth)
                .verticalScroll(scrollState)
                .then(modifier),
    ) {
        AddFontContent()
        PreviewFontContent()
    }
}

@Composable
fun AddFontDualPane(
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current

    Row(
        modifier = Modifier
            .width(dimens.maxContentWidth)
            .then(modifier),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .weight(1f, fill = true)
                    .padding(horizontal = dimens.margin, vertical = 8.dp),
        ) {
            AddFontContent()
        }

        val scrollState = rememberScrollState()

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .weight(1f, fill = true)
                    .padding(horizontal = dimens.margin, vertical = 8.dp)
                    .verticalScroll(scrollState),
        ) {
            PreviewFontContent()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.AddFontContent() {
    val dimens = LocalDimens.current

    OutlinedTextField(
        value = "font_file_name.ttf",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.width(dimens.maxContentWidth),
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(dimens.maxContentWidth),
    ) {
        // Default weight to true to indicate that it is clickable?
        // Also is quite common
        FilterChip(
            selected = true,
            onClick = {},
            label = {
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                )
            },
        )
        FilterChip(
            selected = false,
            onClick = {},
            label = {
                Text(
                    text = "Italic",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
    }
}

@Composable
fun ColumnScope.PreviewFontContent() {
    val dimens = LocalDimens.current
    val textPreviewText = stringResource(id = R.string.text_preview)
    val previewStyle = MaterialTheme.typography.bodyMedium

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    val animatedWeight by infiniteTransition.animateFloat(
        initialValue = 400f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "animated weight",
    )

    val animatedItalic by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "animated italic",
    )

    Text(
        "The quick brown fox jumps over the lazy dog.",
        style = previewStyle,
        textAlign = TextAlign.Left,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "В чащах юга жил-был цитрус... да, но фальшивый экземпляр!",
        style = previewStyle,
        textAlign = TextAlign.Left,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "Η γρήγορη καφέ αλεπού πηδάει πάνω από τον τεμπέλη σκύλο",
        style = previewStyle,
        textAlign = TextAlign.Left,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "كل إنسان يولد حراً ومتساوياً في الكرامة والحقوق",
        style = previewStyle,
        textAlign = TextAlign.Right,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "כל בני האדם נולדו חופשיים ושווים בכבודם ובזכויותיהם",
        style = previewStyle,
        textAlign = TextAlign.Right,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "सभी मनुष्य स्वतंत्र और समान अधिकारों के साथ जन्म लेते",
        style = previewStyle,
        textAlign = TextAlign.Left,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "人人生而自由",
        style = previewStyle,
        textAlign = TextAlign.Left,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "日本語の漢字ひらがなカタカナ",
        style = previewStyle,
        textAlign = TextAlign.Left,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "모든 인간은 태어날 때부터 자유롭고 평등하다",
        style = previewStyle,
        textAlign = TextAlign.Left,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
    Text(
        "มนุษย์ทุกคนเกิดมาอิสระและเท่าเทียมกันในศักดิ์ศรีและสิทธิ",
        style = previewStyle,
        textAlign = TextAlign.Left,
        fontWeight = FontWeight(animatedWeight.toInt()),
        fontStyle = if (animatedItalic > 0.5f) FontStyle.Italic else FontStyle.Normal,
        modifier =
            Modifier
                .width(dimens.maxContentWidth)
                .padding(4.dp)
                .safeSemantics {
                    contentDescription = textPreviewText
                },
    )
}

@Composable
@PreviewThemes
fun PreviewSingle() {
    PreviewTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier,
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                AddFontSinglePane()
            }
        }
    }
}

@Preview(
    name = "Search with results Foldable",
    device = Devices.PIXEL_FOLD,
    uiMode = UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Search with results Tablet",
    device = Devices.PIXEL_C,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
fun PreviewDual() {
    PreviewTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier,
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                AddFontDualPane()
            }
        }
    }
}
