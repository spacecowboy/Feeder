package com.nononsenseapps.feeder.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout;
import com.shirwa.simplistic_rss.RssItem;

/**
 * Displays feed items suitable for consumption.
 */
public class ReaderActivity extends BaseActivity {

    /**
     * Sets the extras in the intent suitable for opening the item in question.
     * @param intent to fill extras in
     * @param id database id of item, if applicable
     * @param rssItem to read
     */
    public static void setRssExtras(Intent intent, long id, RssItem rssItem) {
        intent.putExtra(ReaderFragment.ARG_ID, id);
        intent.putExtra(ReaderFragment.ARG_TITLE, rssItem.getTitle());
        intent.putExtra(ReaderFragment.ARG_DESCRIPTION,
                rssItem.getDescription());
        intent.putExtra(ReaderFragment.ARG_LINK, rssItem.getLink());
        intent.putExtra(ReaderFragment.ARG_IMAGEURL, rssItem.getImageUrl());
    }

    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;
    private Fragment mFragment;
    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mCheckButton;

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

        // Handle plus icon press
        mCheckButton = findViewById(R.id.add_button);
        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // TODO Mark as read
                // TODO animate?
                finish();
               if (mShouldFinishBack) {
                   // Only care about exit transition
                   overridePendingTransition(0, R.anim.contract_to_center);
               }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if (mShouldFinishBack) {
            // Only care about exit transition
            overridePendingTransition(0, R.anim.contract_to_center);
        }
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

    /**
     * Initializes a fragment based on intent information
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
        registerHideableFooterView(mCheckButton);
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        mDrawShadowFrameLayout.setShadowVisible(shown, shown);
    }
}
