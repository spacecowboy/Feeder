package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.localtranslation.BergamotModelDownloadProgress

@Composable
fun TranslationProgressContent(
    progress: BergamotModelDownloadProgress,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(12.dp),
    ) {
        Text(
            text =
                if (progress.isIndeterminate) {
                    stringResource(
                        R.string.preparing_offline_translation_model,
                        progress.sourceLanguage,
                        progress.targetLanguage,
                    )
                } else {
                    stringResource(
                        R.string.downloading_offline_translation_model,
                        progress.sourceLanguage,
                        progress.targetLanguage,
                        (progress.fraction * 100).toInt(),
                    )
                },
            style = MaterialTheme.typography.bodySmall,
        )
        if (progress.isIndeterminate) {
            LinearProgressIndicator(
                modifier =
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
            )
        } else {
            LinearProgressIndicator(
                progress = { progress.fraction },
                modifier =
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
            )
        }
        Text(
            text = stringResource(R.string.offline_translation_model_download_hint),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
