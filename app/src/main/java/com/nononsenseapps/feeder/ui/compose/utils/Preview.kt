package com.nononsenseapps.feeder.ui.compose.utils

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Phone portrait",
    device = Devices.PIXEL_2,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Foldable light",
    device = Devices.FOLDABLE,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Tablet light",
    device = Devices.PIXEL_C,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
annotation class DevicePreviews()

@Preview(
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class ThemePreviews()
