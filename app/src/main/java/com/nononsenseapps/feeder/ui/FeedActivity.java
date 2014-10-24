package com.nononsenseapps.feeder.ui;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.views.DrawShadowFrameLayout;


public class FeedActivity extends BaseActivity {

  private Fragment mFragment;
  private DrawShadowFrameLayout mDrawShadowFrameLayout;
  private View mCheckAllButton;
  private View mSyncIndicator1;
  private View mSyncIndicator2;
  private boolean isSyncing = false;

  // Broadcastreceiver for sync events
  private BroadcastReceiver mSyncMsgReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, final Intent intent) {
      isSyncing = intent
          .getBooleanExtra(RssSyncAdapter.SYNC_BROADCAST_IS_ACTIVE, false);
      showHideSyncIndicators(isSyncing);
    }
  };
  private View mEmptyView;
  private View mNewItemsButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed);
    getActionBarToolbar();

    overridePendingTransition(0, 0);

    if (savedInstanceState == null) {
      mFragment = getDefaultFragment();
      if (mFragment != null) {
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


    // New items button
    mNewItemsButton = findViewById(R.id.new_items_button);
    mNewItemsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        // TODO
      }
    });

    // Sync indicators
    mSyncIndicator1 = findViewById(R.id.sync_indicator_1);
    mSyncIndicator2 = findViewById(R.id.sync_indicator_2);
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

  private void askForLogin() {
    DialogFragment dialog = new AccountDialog();
    dialog.show(getSupportFragmentManager(), "account_dialog");
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    registerHideableHeaderView(findViewById(R.id.headerbar));
    registerHideableHeaderView(mSyncIndicator2);
    registerHideableFooterView(mCheckAllButton);
    registerHideableFooterView(mNewItemsButton);
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
