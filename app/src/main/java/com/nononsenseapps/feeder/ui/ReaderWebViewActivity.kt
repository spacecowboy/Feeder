package com.nononsenseapps.feeder.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout

/**
 * Displays feed items suitable for consumption.
 */
class ReaderWebViewActivity : BaseActivity() {
    private var mFragment: ReaderWebViewFragment? = null
    private var mDrawShadowFrameLayout: DrawShadowFrameLayout? = null

    /**
     * Initializes a fragment based on intent information
     *
     * @return ReaderFragment
     */
    private val fragment: ReaderWebViewFragment
        get() {
            val i = intent
            val fragment = ReaderWebViewFragment()
            fragment.arguments = i.extras
            return fragment
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)
        initializeActionBar()
        val ab = supportActionBar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true)
            ab.setHomeButtonEnabled(true)
            ab.setDisplayShowTitleEnabled(false)
        }

        if (savedInstanceState == null) {
            mFragment = fragment
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, mFragment!!, "webview").commit()
        } else {
            mFragment = supportFragmentManager.findFragmentByTag("webview") as ReaderWebViewFragment?
        }

        mDrawShadowFrameLayout = findViewById(R.id.main_content)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        registerHideableHeaderView(findViewById<View>(R.id.headerbar))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == android.R.id.home && mShouldFinishBack) {
            super.onOptionsItemSelected(item)
        } else super.onOptionsItemSelected(item)
    }

    override fun onActionBarAutoShowOrHide(shown: Boolean) {
        super.onActionBarAutoShowOrHide(shown)
        mDrawShadowFrameLayout!!.setShadowVisible(shown, shown)
    }

    override fun onBackPressed() {
        if (mFragment?.webView?.canGoBack() == true) {
            mFragment?.webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
