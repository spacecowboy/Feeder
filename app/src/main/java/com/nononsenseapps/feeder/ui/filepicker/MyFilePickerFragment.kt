package com.nononsenseapps.feeder.ui.filepicker

import android.net.Uri
import androidx.core.content.FileProvider
import com.nononsenseapps.filepicker.FilePickerFragment
import java.io.File


class MyFilePickerFragment : FilePickerFragment() {
    /**
     * Override the name of the provider due to conflict with the regular content provider
     */
    override fun toUri(file: File): Uri {
        return FileProvider.getUriForFile(context!!,
                "${context!!.applicationContext.packageName}.filepicker_provider", file)
    }
}
