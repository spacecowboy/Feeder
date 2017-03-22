package com.nononsenseapps.feeder.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.db.Util;
import com.nononsenseapps.feeder.model.OPMLContenProvider;
import com.nononsenseapps.feeder.model.OPMLParser;
import com.nononsenseapps.feeder.model.OPMLWriter;
import com.nononsenseapps.feeder.model.RssSyncAdapter;
import com.nononsenseapps.feeder.util.Function;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.util.Supplier;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class FeedActivity extends BaseActivity {

    private static final int EXPORT_OPML_CODE = 101;
    private static final int IMPORT_OPML_CODE = 102;
    private Fragment mFragment;

    // Broadcast receiver for sync events
    private BroadcastReceiver mSyncMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (RssSyncAdapter.SYNC_BROADCAST.equals(intent.getAction())) {

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

        TextView emptyAddFeed = (TextView) findViewById(R.id.empty_add_feed);
        emptyAddFeed.setText(
                android.text.Html.fromHtml(getString(R.string.empty_no_feeds_add)));
        emptyAddFeed.setOnClickListener(onAddListener);

        // Night mode
        final CheckedTextView nightCheck = (CheckedTextView) findViewById(R.id.nightcheck);
        nightCheck.setChecked(PrefUtils.isNightMode(this));
        nightCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle icon first
                nightCheck.toggle();
                // Toggle prefs
                PrefUtils.setNightMode(FeedActivity.this, nightCheck.isChecked());

                // Change background
                setNightBackground();
            }
        });
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        registerHideableHeaderView(findViewById(R.id.headerbar));
        //registerHideableFooterView(mActionFooter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_sync) {
//            syncOrConfig();
//            return true;
//        } else
        if (id == R.id.action_add) {
            startActivity(new Intent(FeedActivity.this, EditFeedActivity.class));
            return true;
        } else if (R.id.action_opml_export == id) {
            // Choose file, then export
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("text/opml");
            intent.putExtra(Intent.EXTRA_TITLE, "feeder.opml");
            startActivityForResult(intent, EXPORT_OPML_CODE);
            return true;
        } else if (R.id.action_opml_import == id) {
            // Choose file
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES,
                    Util.ToStringArray("text/plain", "text/xml", "text/opml", "*/*"));
            startActivityForResult(intent, IMPORT_OPML_CODE);
            return true;
        } else if (R.id.action_debug_log == id) {
            startActivity(new Intent(this, DebugLogActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (EXPORT_OPML_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            // TODO avoid UI-thread
            if (resultData != null) {
                Uri uri = resultData.getData();

                try {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    OPMLWriter writer = new OPMLWriter();
                    writer.writeOutputStream(os,
                            new Supplier<Iterable<String>>() {
                                @Override
                                public Iterable<String> get() {
                                    ArrayList<String> tags = new ArrayList<>();

                                    Cursor c = FeedActivity.this.getContentResolver()
                                            .query(FeedSQL.URI_TAGSWITHCOUNTS,
                                                    Util.ToStringArray(FeedSQL.COL_TAG), null, null,
                                                    null);

                                    try {
                                        while (c.moveToNext()) {
                                            tags.add(c.getString(0));
                                        }
                                    } finally {
                                        if (c != null) {
                                            c.close();
                                        }
                                    }

                                    return tags;
                                }
                            },
                            new Function<String, Iterable<FeedSQL>>() {
                                @Override
                                public Iterable<FeedSQL> apply(String tag) {
                                    ArrayList<FeedSQL> feeds = new ArrayList<>();

                                    final String where = FeedSQL.COL_TAG + " IS ?";
                                    final String[] args = Util.ToStringArray(tag == null ? "" : tag);
                                    Cursor c = FeedActivity.this.getContentResolver()
                                            .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS,
                                                    where, args, null);

                                    try {
                                        while (c.moveToNext()) {
                                            feeds.add(new FeedSQL(c));
                                        }
                                    } finally {
                                        if (c != null) {
                                            c.close();
                                        }
                                    }

                                    return feeds;
                                }
                            }
                    );
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO tell user about error
                }
            }
        } else if (IMPORT_OPML_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            // TODO avoid UI-thread
            if (resultData != null) {
                Uri uri = resultData.getData();
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    OPMLParser parser = new OPMLParser(new OPMLContenProvider(this));
                    parser.parseInputStream(is);
                    is.close();
                    RssContentProvider.notifyAllUris(this);
                    RssContentProvider.RequestSync();
                } catch (SAXException | IOException e) {
                    // TODO tell user about error
                }
            }
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

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.feed, menu);
        return true;
    }
}
