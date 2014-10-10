package com.nononsenseapps.feeder.ui;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.AuthHelper;
import com.nononsenseapps.feeder.model.RssSyncAdapter;
import com.nononsenseapps.feeder.model.RssSyncHelper;
import com.nononsenseapps.feeder.util.PrefUtils;
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
    private View mEmptyAddFeed;
    private View mSyncIndicator1;
    private View mSyncIndicator2;
    private boolean isSyncing = false;

    // Broadcastreceiver for sync events
    private BroadcastReceiver mSyncMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            isSyncing = intent.getBooleanExtra
                                (RssSyncAdapter.SYNC_BROADCAST_IS_ACTIVE,
                                        false);
            showHideSyncIndicators(isSyncing);
        }
    };

    private void showHideSyncIndicators(final boolean isSyncing) {
        mSyncIndicator1.setVisibility(isSyncing ?
                                      View.VISIBLE :
                                      View.GONE);
        mSyncIndicator2.setVisibility(isSyncing ?
                                      View.VISIBLE :
                                      View.GONE);
    }

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

        // Handle add buttons
        View.OnClickListener onAddListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent i =
                        new Intent(FeedActivity.this, EditFeedActivity.class);
                if (view == mAddButton) {
                    i.putExtra(EditFeedActivity.SHOULD_FINISH_BACK, true);
                    ActivityOptions options = ActivityOptions
                            .makeScaleUpAnimation(view, 0, 0, view.getWidth(),
                                    view.getHeight());
                    startActivity(i, options.toBundle());
                } else {
                    startActivity(i);
                }
            }
        };
        mEmptyAddFeed = findViewById(R.id.empty_add_feed);
        ((TextView) mEmptyAddFeed).setText(android.text.Html.fromHtml
                (getString(R.string.empty_no_feeds)));
        mEmptyAddFeed.setVisibility(mFragment == null ? View.VISIBLE : View.GONE);
        mEmptyAddFeed.setOnClickListener(onAddListener);
        mAddButton = findViewById(R.id.add_button);
        mAddButton.setOnClickListener(onAddListener);

        // Sync indicators
        mSyncIndicator1 = findViewById(R.id.sync_indicator_1);
        mSyncIndicator2 = findViewById(R.id.sync_indicator_2);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver
                (mSyncMsgReceiver,
                        new IntentFilter(RssSyncAdapter.SYNC_BROADCAST));
    }

    @Override
    public void onPause() {
        // Stop listening to broadcasts
        LocalBroadcastManager.getInstance(this).unregisterReceiver
                (mSyncMsgReceiver);
        // hide sync indicators
        showHideSyncIndicators(false);

        super.onPause();
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
        registerHideableHeaderView(mSyncIndicator2);
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
            syncOrConfig();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Request a sync or ask for account if required.
     *
     * @return true if sync is underway, or false otherwise
     */
    public boolean syncOrConfig() {
        if (null == AuthHelper.getSavedAccountName(this)) {
            DialogFragment dialog = new AccountDialog();
            dialog.show(getFragmentManager(), "account_dialog");
            return false;
        } else {
            Toast.makeText(this, "Syncing feeds...",
                    Toast.LENGTH_SHORT).show();
            //RssSyncHelper.syncFeeds(this);
            final Account account = AuthHelper.getSavedAccount(this);
            // Enable syncing
            ContentResolver.setIsSyncable(account,
                    RssContentProvider.AUTHORITY, 1);
            // Set sync automatic
            ContentResolver.setSyncAutomatically(account,
                    RssContentProvider.AUTHORITY, true);
            // Once per hour: mins * secs
            ContentResolver.addPeriodicSync(account,
                    RssContentProvider.AUTHORITY,
                    Bundle.EMPTY,
                    60L * 60L);
            // And sync manually
            final Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(account,
                    RssContentProvider.AUTHORITY, settingsBundle);

            return true;
        }
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

        // Hide progress bar if no sync underway
        // header bar overrides this, hence this call
        showHideSyncIndicators(isSyncing);
    }
}
