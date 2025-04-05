package com.nononsenseapps.feeder.ui.compose.font

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.feed.PlainTooltipBox
import com.nononsenseapps.feeder.ui.compose.settings.MenuSetting
import com.nononsenseapps.feeder.ui.compose.settings.SliderWithEndLabels
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.LocalWindowSizeMetrics
import com.nononsenseapps.feeder.ui.compose.utils.PreviewThemes
import com.nononsenseapps.feeder.ui.compose.utils.ProvideScaledText
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import com.nononsenseapps.feeder.ui.compose.utils.getScreenType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFontScreen(
    onNavigateUp: () -> Unit,
    addFontViewModel: AddFontViewModel,
    modifier: Modifier = Modifier,
) {
    val viewState by addFontViewModel.viewState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val windowSize = LocalWindowSizeMetrics.current
    val screenType by remember(windowSize) {
        derivedStateOf {
            getScreenType(windowSize)
        }
    }

    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SensibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = stringResource(R.string.text),
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
    ) { padding ->
        AddFontView(
            viewState = viewState,
            onEvent = addFontViewModel::onEvent,
            screenType = screenType,
            modifier = Modifier
                .padding(padding)
                .navigationBarsPadding(),
        )
    }
}

@Composable
fun AddFontView(
    viewState: FontSettingsState,
    onEvent: (FontSettingsEvent) -> Unit,
    screenType: ScreenType,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
                .fillMaxWidth(),
    ) {
        if (screenType == ScreenType.DUAL) {
            AddFontDualPane(
                viewState = viewState,
                onEvent = onEvent,
            )
        } else {
            AddFontSinglePane(
                viewState = viewState,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
fun AddFontSinglePane(
    viewState: FontSettingsState,
    onEvent: (FontSettingsEvent) -> Unit,
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
        AddFontContent(
            viewState = viewState,
            onEvent = onEvent,
        )
        PreviewFontContent(viewState)
    }
}

@Composable
fun AddFontDualPane(
    viewState: FontSettingsState,
    onEvent: (FontSettingsEvent) -> Unit,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .weight(1f, fill = true)
                    .padding(horizontal = dimens.margin, vertical = 8.dp),
        ) {
            AddFontContent(
                viewState = viewState,
                onEvent = onEvent,
            )
        }

        val scrollState = rememberScrollState()

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .weight(1f, fill = true)
                    .padding(horizontal = dimens.margin, vertical = 8.dp)
                    .verticalScroll(scrollState),
        ) {
            PreviewFontContent(viewState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.AddFontContent(
    viewState: FontSettingsState,
    onEvent: (FontSettingsEvent) -> Unit,
) {
    val systemDefaultString = stringResource(R.string.system_default)
    val addFontString = stringResource(R.string.add_font)

    val currentUiFontOption = remember(viewState.font, systemDefaultString) {
        UiFontOption.fromFontSelection(viewState.font, systemDefaultString)
    }

    val uiFontOptions: List<UiFontOption> = remember(viewState.fontOptions, systemDefaultString, addFontString) {
        sequence {
            yield(
                UiFontOption(
                    name = addFontString,
                    realOption = null,
                )
            )
            for (font in viewState.fontOptions) {
                yield(
                    UiFontOption.fromFontSelection(font, systemDefaultString),
                )
            }
        }
            .sortedBy { value ->
                when (value.realOption) {
                    FontSelection.AtkinsonHyperLegible -> "20"
                    FontSelection.Roboto -> "10"
                    FontSelection.SystemDefault -> "30"
                    is FontSelection.UserFont -> "40 ${value.name}"
                    null -> "50"
                }
            }
            .toList()
    }

    val addFontPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                onEvent(FontSettingsEvent.AddFont(uri))
            }
        }

    MenuSetting(
        title = stringResource(R.string.font),
        currentValue = currentUiFontOption,
        values = ImmutableHolder(uiFontOptions),
        onSelection = {
            if (it.realOption == null) {
                try {
                    addFontPicker.launch(
                        arrayOf(
                            "application/x-font-ttf",
                            "font/ttf",
                        )
                    )
                } catch (_: Exception) {
                    // ActivityNotFoundException
                    // TODO message
                }
            } else {
                onEvent(FontSettingsEvent.SetFont(it.realOption))
            }
        },
        icon = null,
    )

    Header(
        R.string.text_scale,
        modifier = Modifier.padding(top = 8.dp),
    )

    val textScaleText = stringResource(id = R.string.text_scale)
    ScaleSetting(
        currentValue = viewState.fontScale,
        onValueChange = {
            onEvent(FontSettingsEvent.SetFontScale(it))
        },
        valueRange = 1f..2f,
        steps = 9,
        modifier =
            Modifier.safeSemantics(mergeDescendants = true) {
                contentDescription = textScaleText
            },
    )


    Header(
        R.string.text_preview,
        modifier = Modifier.padding(top = 8.dp),
    )

    val boldLabel = stringResource(R.string.bold)
    val italicLabel = stringResource(R.string.italic)
    MultiChoiceSegmentedButtonRow {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            checked = viewState.previewBold,
            onCheckedChange = {
                onEvent(FontSettingsEvent.SetPreviewBold(!viewState.previewBold))
            }
        ) {
            Text(boldLabel)
        }

        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            checked = viewState.previewItalic,
            onCheckedChange = {
                onEvent(FontSettingsEvent.SetPreviewItalic(!viewState.previewItalic))
            }
        ) {
            Text(italicLabel)
        }
    }
}

@Composable
fun Header(
    @StringRes text: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.labelMedium.merge(
            TextStyle(color = MaterialTheme.colorScheme.primary),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(Modifier.semantics { heading() })
            .then(modifier),
    )
}

@Composable
fun ScaleSetting(
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current
    val safeCurrentValue = currentValue.coerceIn(valueRange)
    // People using screen readers probably don't care that much about text size
    // so no point in adding screen reader action?
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .heightIn(min = 64.dp)
                .safeSemantics(mergeDescendants = true) {
                    stateDescription = "%.1fx".format(safeCurrentValue)
                },
    ) {
        SliderWithEndLabels(
            value = safeCurrentValue,
            startLabel = {
                Text(
                    "A",
                    style =
                        MaterialTheme.typography.bodyLarge
                            .merge(
                                TextStyle(
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * valueRange.start,
                                ),
                            ),
                    modifier = Modifier.alignByBaseline(),
                )
            },
            endLabel = {
                Text(
                    "A",
                    style =
                        MaterialTheme.typography.bodyLarge
                            .merge(
                                TextStyle(
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * valueRange.endInclusive,
                                ),
                            ),
                    modifier = Modifier.alignByBaseline(),
                )
            },
            valueRange = valueRange,
            onValueChange = onValueChange,
            steps = steps,
        )
    }
}

@Composable
fun ColumnScope.PreviewFontContent(
    viewState: FontSettingsState,
) {
    val dimens = LocalDimens.current
    val textPreviewText = stringResource(id = R.string.text_preview)
    val previewStyle = MaterialTheme.typography.bodyMedium

    val fontWeight = remember(viewState.previewBold) {
        if (viewState.previewBold) {
            FontWeight.Bold
        } else {
            FontWeight.Normal
        }
    }

    val fontStyle = remember(viewState.previewItalic) {
        if (viewState.previewItalic) {
            FontStyle.Italic
        } else {
            FontStyle.Normal
        }
    }

    ProvideScaledText(style = previewStyle) {
        Text(
            "The quick brown fox jumps over the lazy dog.",
            textAlign = TextAlign.Left,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Left,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Left,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Right,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Right,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Left,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Left,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Left,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Left,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
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
            textAlign = TextAlign.Left,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            modifier =
                Modifier
                    .width(dimens.maxContentWidth)
                    .padding(4.dp)
                    .safeSemantics {
                        contentDescription = textPreviewText
                    },
        )
    }
}

@Immutable
data class UiFontOption(
    val name: String,
    val realOption: FontSelection?,
) {
    override fun toString(): String {
        return name
    }

    companion object {
        fun fromFontSelection(
            fontSelection: FontSelection,
            systemDefaultString: String,
        ): UiFontOption {
            return UiFontOption(
                name = when (fontSelection) {
                    is FontSelection.Roboto -> "Roboto"
                    is FontSelection.AtkinsonHyperLegible -> "Atkinson Hyper Legible"
                    is FontSelection.SystemDefault -> systemDefaultString
                    is FontSelection.UserFont -> fontSelection.path.substringAfter("/")
                },
                realOption = fontSelection,
            )
        }
    }
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
                AddFontSinglePane(
                    viewState = FontSettingsState(),
                    onEvent = {},
                )
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
                AddFontDualPane(
                    viewState = FontSettingsState(),
                    onEvent = {},
                )
            }
        }
    }
}
