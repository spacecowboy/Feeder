package com.nononsenseapps.feeder.ui;

import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.model.RssSyncService;
import com.nononsenseapps.feeder.model.SyncHelper;
import com.nononsenseapps.feeder.model.apis.BackendAPIClient;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout;

import java.util.ArrayList;


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
        // TODO
        /*
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.feed, menu);
            restoreActionBar();
            return true;
        }*/

        getMenuInflater().inflate(R.menu.feed, menu);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        mTitle = getTitle();

        overridePendingTransition(0, 0);

        if (savedInstanceState == null) {
            mFragment = getDefaultFragment();
            if (mFragment != null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, mFragment, "single_pane").commit();
            }
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
                Intent i =
                        new Intent(FeedActivity.this, EditFeedActivity.class);
                i.putExtra(EditFeedActivity.SHOULD_FINISH_BACK, true);
                ActivityOptions options = ActivityOptions
                        .makeScaleUpAnimation(view, 0, 0, view.getWidth(),
                                view.getHeight());
                startActivity(i, options.toBundle());
            }
        });
    }

    private Fragment getDefaultFragment() {
        final String tag = PrefUtils.getLastOpenFeedTag(this);
        final long id = PrefUtils.getLastOpenFeedId(this);
        // Will load title and url in fragment
        if (tag != null || id > 0) {
            return FeedFragment.newInstance(id, "", "", tag);
        } else {
            return null;
        }
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
        } else if (id == R.id.action_sync) {
            if (null == SyncHelper.getSavedAccountName(this)) {
                DialogFragment dialog = new AccountDialog();
                dialog.show(getFragmentManager(), "account_dialog");
            } else {
                Toast.makeText(this, "Syncing feeds...",
                        Toast.LENGTH_SHORT).show();
                RssSyncService.syncFeeds(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return 0;
    }

    @Override
    protected void onNavigationDrawerItemSelected(long id, String title,
            String url, String tag) {
        // update the main content by replacing fragments
        mFragment = FeedFragment.newInstance(id, title, url, tag);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mFragment, "single_pane").commit();
        // Remember choice in future
        PrefUtils.setLastOpenFeed(this, id, tag);
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        mDrawShadowFrameLayout.setShadowVisible(shown, shown);
    }
}
