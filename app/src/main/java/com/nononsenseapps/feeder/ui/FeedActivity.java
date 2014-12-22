package com.nononsenseapps.feeder.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.AuthHelper;
import com.nononsenseapps.feeder.model.RssSyncAdapter;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout;


public class FeedActivity extends BaseActivity {

    private Fragment mFragment;
    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mCheckAllButton;
    private View mSyncIndicator1;
    private View mSyncIndicator2;
    private boolean isSyncing = false;

    // Broadcast receiver for sync events
    private BroadcastReceiver mSyncMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (RssSyncAdapter.SYNC_BROADCAST.equals(intent.getAction())) {
                isSyncing = intent
                        .getBooleanExtra(RssSyncAdapter.SYNC_BROADCAST_IS_ACTIVE, false);
                showHideSyncIndicators(isSyncing);

                if (mFragment == null) {
                    // Load first feed if nothing is showing
                    loadFirstFeedInDB(false);
                }
            } else if (RssSyncAdapter.FEED_ADDED_BROADCAST.equals(intent.getAction())) {
                // If nothing is loaded, select this first feed
                if (mFragment == null && intent.getLongExtra(FeedSQL.COL_ID, -1) > 0) {
                        onNavigationDrawerItemSelected(intent.getLongExtra(FeedSQL.COL_ID, -1),
                                "", "", null);
                }
            }
        }
    };
    private View mEmptyView;
    private View mSnackBar;
    private View mActionFooter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        getActionBarToolbar();

        overridePendingTransition(0, 0);

        if (savedInstanceState == null) {
            mFragment = getDefaultFragment();
            if (mFragment == null) {
                loadFirstFeedInDB(false);
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, mFragment, "single_pane").commit();
            }
        } else {
            mFragment = getSupportFragmentManager().findFragmentByTag("single_pane");
        }

        mDrawShadowFrameLayout =
                (DrawShadowFrameLayout) findViewById(R.id.main_content);

        // For add buttons
        View.OnClickListener onAddListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent i = new Intent(FeedActivity.this, EditFeedActivity.class);
//        if (view == mAddButton) {
//          i.putExtra(EditFeedActivity.SHOULD_FINISH_BACK, true);
//          ActivityOptions options = ActivityOptions
//              .makeScaleUpAnimation(view, 0, 0, view.getWidth(),
//                  view.getHeight());
//          startActivity(i, options.toBundle());
//        } else {
                startActivity(i);
//        }
            }
        };
        // Empty view
        mEmptyView = findViewById(android.R.id.empty);
        mEmptyView.setVisibility(mFragment == null ? View.VISIBLE : View.GONE);

        TextView emptyLogin = (TextView) findViewById(R.id.empty_login);
        emptyLogin.setVisibility(null == AuthHelper.getSavedAccountName(this) ?
                View.VISIBLE :
                View.GONE);
        emptyLogin.setText(
                android.text.Html.fromHtml(getString(R.string.empty_no_feeds_login)));
        emptyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                askForLogin();
            }
        });

        TextView emptyAddFeed = (TextView) findViewById(R.id.empty_add_feed);
        emptyAddFeed.setText(
                android.text.Html.fromHtml(getString(R.string.empty_no_feeds_add)));
        emptyAddFeed.setOnClickListener(onAddListener);

        // Add button
        //mAddButton = findViewById(R.id.add_button);
        //mAddButton.setOnClickListener(onAddListener);
        mCheckAllButton = findViewById(R.id.checkall_button);
        ViewCompat.setElevation(mCheckAllButton, getResources().getDimension(R.dimen.elevation1));

        // New items button
        mActionFooter = findViewById(R.id.action_footer);
        mSnackBar = findViewById(R.id.snackbar);
        mSnackBar.setVisibility(View.GONE);

        // Sync indicators
        mSyncIndicator1 = findViewById(R.id.sync_indicator_1);
        mSyncIndicator2 = findViewById(R.id.sync_indicator_2);
    }

    /**
     * Load list of all feeds in DB and open the first one returned.
     * @param overrideCurrent if True, will always open the first feed. If False, will only open the first feed if no feed is currently showing (first boot).
     */
    public void loadFirstFeedInDB(final boolean overrideCurrent) {
        final int loaderId = 2523;
        // See if we have any feeds at all in the DB
        getLoaderManager().restartLoader(loaderId, Bundle.EMPTY, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(FeedActivity.this, FeedSQL.URI_FEEDS,
                        FeedSQL.FIELDS, null, null, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if (loader.getId() == loaderId) {
                    if (cursor.moveToNext() && (overrideCurrent || mFragment == null)) {
                        FeedSQL feed = new FeedSQL(cursor);
                        onNavigationDrawerItemSelected(feed.id, feed.title, feed.url, feed.tag);
                    }
                    getLoaderManager().destroyLoader(loader.getId());
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // Nothing
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
            loadFirstFeedInDB(false);

            return null;
        }
    }

    private void askForLogin() {
        DialogFragment dialog = new AccountDialog();
        dialog.show(getSupportFragmentManager(), "account_dialog");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        registerHideableHeaderView(findViewById(R.id.headerbar));
        registerHideableHeaderView(mSyncIndicator2);
//        registerHideableFooterView(mCheckAllButton);
//        registerHideableFooterView(mSnackBar);
        registerHideableFooterView(mActionFooter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            syncOrConfig();
            return true;
        } else if (id == R.id.action_add) {
            startActivity(new Intent(FeedActivity.this, EditFeedActivity.class));
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
            askForLogin();
            return false;
        } else {
            Toast.makeText(this, "Syncing feeds...", Toast.LENGTH_SHORT).show();
            //RssSyncHelper.syncFeeds(this);
            RssContentProvider.RequestSync(this);

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
        mEmptyView.setVisibility(View.GONE);
        mFragment = FeedFragment.newInstance(id, title, url, tag);
        getSupportFragmentManager().beginTransaction()
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

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mSyncMsgReceiver,
                new IntentFilter(RssSyncAdapter.SYNC_BROADCAST));
    }

    @Override
    public void onPause() {
        // Stop listening to broadcasts
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mSyncMsgReceiver);
        // hide sync indicators
        showHideSyncIndicators(false);

        super.onPause();
    }

    private void showHideSyncIndicators(final boolean isSyncing) {
        mSyncIndicator1.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
        mSyncIndicator2.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.feed, menu);
        return true;
    }
}
