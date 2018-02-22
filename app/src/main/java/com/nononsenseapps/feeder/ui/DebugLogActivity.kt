package com.nononsenseapps.feeder.ui

import android.os.Bundle
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.FileLog


class DebugLogActivity : BaseActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_log)
    }

    override fun onResume() {
        val log = FileLog.getInstance(this)

        findViewById<TextView>(android.R.id.text1).text = log.log

        super.onResume()
    }
}
