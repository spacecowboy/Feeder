package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout

/**
 * Displays feed items suitable for consumption.
 */
class ReaderActivity : BaseActivity() {
    private var mFragment: androidx.fragment.app.Fragment? = null
    private var mDrawShadowFrameLayout: DrawShadowFrameLayout? = null

    /**
     * Initializes a fragment based on intent information
     *
     * @return ReaderFragment
     */
    private val fragment: androidx.fragment.app.Fragment
        get() {
            val i = intent
            val fragment = ReaderFragment()
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
                    .add(R.id.container, mFragment!!, "single_pane").commit()
        } else {
            mFragment = supportFragmentManager.findFragmentByTag("single_pane")
        }

        mDrawShadowFrameLayout = findViewById(R.id.main_content)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        registerHideableHeaderView(findViewById<View>(R.id.headerbar))
        //registerHideableFooterView(mCheckButton);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == android.R.id.home && mShouldFinishBack) {
            // Handled by super, except animation
            // Only care about exit transition
            //overridePendingTransition(0, R.anim.contract_to_center);
            super.onOptionsItemSelected(item)
        } else super.onOptionsItemSelected(item)
    }

    override fun onActionBarAutoShowOrHide(shown: Boolean) {
        super.onActionBarAutoShowOrHide(shown)
        mDrawShadowFrameLayout!!.setShadowVisible(shown, shown)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mShouldFinishBack) {
            // Only care about exit transition
            //overridePendingTransition(0, R.anim.contract_to_center);
        }
    }

    companion object {

        /**
         * Sets the extras in the intent suitable for opening the item in question.
         *
         * @param intent  to fill extras in
         * @param rssItem to read
         */
        fun setRssExtras(intent: Intent, rssItem: FeedItemSQL) {
            intent.putExtra(ARG_ID, rssItem.id)
            intent.putExtra(ARG_TITLE, rssItem.title)
            intent.putExtra(ARG_DESCRIPTION, rssItem.description)
            intent.putExtra(ARG_LINK, rssItem.link)
            intent.putExtra(ARG_ENCLOSURE, rssItem.enclosurelink)
            intent.putExtra(ARG_IMAGEURL, rssItem.imageurl)
            intent.putExtra(ARG_DATE, rssItem.pubDateString)
            intent.putExtra(ARG_AUTHOR, rssItem.author)
            intent.putExtra(ARG_FEEDTITLE, rssItem.feedtitle)
            intent.putExtra(ARG_FEED_URL, rssItem.feedUrl.toString())
        }
    }
}
