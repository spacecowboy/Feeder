package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.nononsenseapps.feeder.R

/**
 * A [BaseActivity] that simply contains a single fragment. The intent
 * used to invoke this
 * activity is forwarded to the fragment as arguments during fragment
 * instantiation.
 */
abstract class SimpleSinglePaneActivity : BaseActivity() {
    var fragment: Fragment? = null
        private set

    /**
     * @return The layout of the activity
     */
    @get:LayoutRes
    protected abstract val contentViewResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewResId)

        if (intent.hasExtra(Intent.EXTRA_TITLE)) {
            title = intent.getStringExtra(Intent.EXTRA_TITLE)
        }

        val customTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
        title = customTitle ?: title
        if (savedInstanceState == null) {
            fragment = onCreatePane()
            fragment?.let { fragment ->
                fragment.arguments = BaseActivity.intentToFragmentArguments(intent)
                supportFragmentManager.beginTransaction()
                        .add(R.id.container, fragment, "single_pane").commit()
            }
        } else {
            fragment = supportFragmentManager.findFragmentByTag("single_pane")
        }
    }

    /**
     * Called in `onCreate` when the fragment constituting this
     * activity is needed.
     * The returned fragment's arguments will be set to the intent used to
     * invoke this activity.
     */
    protected abstract fun onCreatePane(): Fragment
}
