package com.nononsenseapps.feeder.model

import android.view.WindowManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.rules.ExternalResource

/** Prevents device auto-lock during UI tests without changing system settings. */
class KeepScreenOnRule : ExternalResource() {
    private val callback =
        ActivityLifecycleCallback { activity, stage ->
            if (stage == Stage.RESUMED) activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

    override fun before() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback)
        }
    }

    override fun after() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback)
        }
    }
}
