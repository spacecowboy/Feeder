package com.nononsenseapps.feeder.ui.filepicker

import android.os.Environment
import com.nononsenseapps.filepicker.AbstractFilePickerActivity
import com.nononsenseapps.filepicker.AbstractFilePickerFragment
import java.io.File

class MyFilePickerActivity : AbstractFilePickerActivity<File>() {
    override fun getFragment(
        startPath: String?,
        mode: Int,
        allowMultiple: Boolean,
        allowCreateDir: Boolean,
        allowExistingFile: Boolean,
        singleClick: Boolean
    ): AbstractFilePickerFragment<File> {
        val fragment = MyFilePickerFragment()
        fragment.setArgs(
            startPath ?: Environment.getExternalStorageDirectory().path,
            mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick
        )
        return fragment
    }
}
