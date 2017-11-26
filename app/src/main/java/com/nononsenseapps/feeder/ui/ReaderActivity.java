package com.nononsenseapps.feeder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout;

import static com.nononsenseapps.feeder.ui.FeedFragmentKt.ARG_FEED_URL;

/**
 * Displays feed items suitable for consumption.
 */
public class ReaderActivity extends BaseActivity {

    private static final String TAG = "ReaderActivity";
    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;
    private Fragment mFragment;
    private DrawShadowFrameLayout mDrawShadowFrameLayout;

    /**
     * Sets the extras in the intent suitable for opening the item in question.
     *
     * @param intent  to fill extras in
     * @param rssItem to read
     */
    public static void setRssExtras(Intent intent, FeedItemSQL rssItem) {
        intent.putExtra(ReaderFragmentKt.ARG_ID, rssItem.getId());
        intent.putExtra(ReaderFragmentKt.ARG_TITLE, rssItem.getTitle());
        intent.putExtra(ReaderFragmentKt.ARG_DESCRIPTION, rssItem.getDescription());
        intent.putExtra(ReaderFragmentKt.ARG_LINK, rssItem.getLink());
        intent.putExtra(ReaderFragmentKt.ARG_ENCLOSURE, rssItem.getEnclosurelink());
        intent.putExtra(ReaderFragmentKt.ARG_IMAGEURL, rssItem.getImageurl());
        intent.putExtra(ReaderFragmentKt.ARG_DATE, rssItem.getPubDateString());
        intent.putExtra(ReaderFragmentKt.ARG_AUTHOR, rssItem.getAuthor());
        intent.putExtra(ReaderFragmentKt.ARG_FEEDTITLE, rssItem.getFeedtitle());
        intent.putExtra(ARG_FEED_URL, rssItem.getFeedUrl());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        initializeActionBar();
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        mTitle = getTitle();

        if (savedInstanceState == null) {
            mFragment = getFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment, "single_pane").commit();
        } else {
            mFragment = getSupportFragmentManager().findFragmentByTag("single_pane");
        }

        mDrawShadowFrameLayout =
                (DrawShadowFrameLayout) findViewById(R.id.main_content);

    }

    /**
     * Initializes a fragment based on intent information
     *
     * @return ReaderFragment
     */
    private Fragment getFragment() {
        Intent i = getIntent();
        Fragment fragment = new ReaderFragment();
        fragment.setArguments(i.getExtras());
        return fragment;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        registerHideableHeaderView(findViewById(R.id.headerbar));
        //registerHideableFooterView(mCheckButton);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home && mShouldFinishBack) {
            // Handled by super, except animation
            boolean val = super.onOptionsItemSelected(item);
            // Only care about exit transition
            //overridePendingTransition(0, R.anim.contract_to_center);
            return val;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        mDrawShadowFrameLayout.setShadowVisible(shown, shown);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mShouldFinishBack) {
            // Only care about exit transition
            //overridePendingTransition(0, R.anim.contract_to_center);
        }
    }
}
