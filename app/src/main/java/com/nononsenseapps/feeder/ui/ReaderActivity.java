package com.nononsenseapps.feeder.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout;

/**
 * Displays feed items suitable for consumption.
 */
public class ReaderActivity extends BaseActivity {

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
        intent.putExtra(ReaderFragment.ARG_ID, rssItem.id);
        intent.putExtra(ReaderFragment.ARG_TITLE, rssItem.title);
        intent.putExtra(ReaderFragment.ARG_DESCRIPTION, rssItem.description);
        intent.putExtra(ReaderFragment.ARG_LINK, rssItem.enclosurelink !=
                                                 null ? rssItem.enclosurelink
                                                      : rssItem.link);
        intent.putExtra(ReaderFragment.ARG_IMAGEURL, rssItem.imageurl);
        intent.putExtra(ReaderFragment.ARG_DATE, rssItem.getPubDateString());
        intent.putExtra(ReaderFragment.ARG_AUTHOR, rssItem.author);
        intent.putExtra(ReaderFragment.ARG_FEEDTITLE, rssItem.feedtitle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        mTitle = getTitle();

        if (savedInstanceState == null) {
            mFragment = getFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment, "single_pane").commit();
        } else {
            mFragment = getFragmentManager().findFragmentByTag("single_pane");
        }

        getLPreviewUtils().trySetActionBar();

        mDrawShadowFrameLayout =
                (DrawShadowFrameLayout) findViewById(R.id.main_content);

        //mNavigationDrawerFragment =
        //        (NavigationDrawerFragment) getFragmentManager()
        //                .findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        //mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
        //        (DrawerLayout) findViewById(R.id.drawer_layout));


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
            overridePendingTransition(0, R.anim.contract_to_center);
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
            overridePendingTransition(0, R.anim.contract_to_center);
        }
    }
}
