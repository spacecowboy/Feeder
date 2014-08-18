package com.nononsenseapps.feeder.ui;

import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout;


public class FeedActivity extends BaseActivity {

    /**
     * Used to store the last screen title. For use in {@link
     * #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Fragment mFragment;
    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mAddButton;

    public void onFragmentAttached(String title) {
        mTitle = title;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.feed, menu);
            restoreActionBar();
            return true;
        }*/
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        mTitle = getTitle();

        overridePendingTransition(0, 0);

        if (savedInstanceState == null) {
            mFragment = getDefaultFragment();
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
        mAddButton = findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent i = new Intent(FeedActivity.this,
                        EditFeedActivity.class);
                i.putExtra(EditFeedActivity.SHOULD_FINISH_BACK, true);
                ActivityOptions options = ActivityOptions
                        .makeScaleUpAnimation(view, 0, 0, view.getWidth(),
                                view.getHeight());
                startActivity(i, options.toBundle());
            }
        });
    }

    private Fragment getDefaultFragment() {
        // TODO do something better
        return FeedFragment.newInstance(-1, "Android Police",
                "http://feeds.feedburner.com/AndroidPolice");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        registerHideableHeaderView(findViewById(R.id.headerbar));
        registerHideableFooterView(mAddButton);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return 0;
    }

    @Override
    protected void onNavigationDrawerItemSelected(long id, String title, String url) {
        // update the main content by replacing fragments
        mFragment = FeedFragment.newInstance(id, title, url);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mFragment, "single_pane").commit();
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        mDrawShadowFrameLayout.setShadowVisible(shown, shown);
    }
}
