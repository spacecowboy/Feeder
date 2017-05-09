package com.nononsenseapps.feeder.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.Util;
import com.nononsenseapps.feeder.model.FeedParseLoader;
import com.nononsenseapps.feeder.util.ContentResolverExtensionsKt;
import com.nononsenseapps.feeder.util.LoaderResult;
import com.nononsenseapps.feeder.views.FloatLabelLayout;
import com.rometools.rome.feed.synd.SyndFeed;

import java.util.Collections;
import java.util.List;

import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_CUSTOM_TITLE;
import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_ID;
import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_TAG;
import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_TITLE;
import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_URL;
import static com.nononsenseapps.feeder.db.UriKt.URI_FEEDS;
import static com.nononsenseapps.feeder.db.UriKt.URI_TAGSWITHCOUNTS;

public class EditFeedActivity extends Activity
        implements LoaderManager.LoaderCallbacks<LoaderResult<SyndFeed>> {

    public static final String SHOULD_FINISH_BACK = "SHOULD_FINISH_BACK";
    public static final String _ID = "_id";
    public static final String CUSTOM_TITLE = "custom_title";
    public static final String FEED_TITLE = "feed_title";
    public static final String TAG = "tag";
    public static final String TEMPLATE = "template";
    private static final int RSSFINDER = 1;
    private static final String SEARCHQUERY = "searchquery";
    private static final int LOADER_TAG_SUGGESTIONS = 1;
    private static final String TAGSFILTER = "TAGSFILTER";
    private boolean mShouldFinishBack = false;
    private long id = -1;
    // Views and shit
    private EditText mTextTitle;
    private EditText mTextUrl;
    private AutoCompleteTextView mTextTag;
    private EditText mTextSearch;
    private View mDetailsFrame;
    private ListView mListResults;
    private ResultsAdapter mResultAdapter;
    private View mSearchFrame;
    private String mFeedUrl = null;
    private TextView mEmptyText;
    private View mLoadingProgress;
    private FloatLabelLayout mUrlLabel;
    private FloatLabelLayout mTitleLabel;
    private FloatLabelLayout mTagLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_feed);

        // Setup views
        mTextTitle = (EditText) findViewById(R.id.feed_title);
        mTitleLabel = (FloatLabelLayout) mTextTitle.getParent();
        mTextUrl = (EditText) findViewById(R.id.feed_url);
        mUrlLabel = (FloatLabelLayout) mTextUrl.getParent();
        mTextTag = (AutoCompleteTextView) findViewById(R.id.feed_tag);
        mTagLabel = (FloatLabelLayout) mTextTag.getParent();
        mDetailsFrame = findViewById(R.id.feed_details_frame);
        mSearchFrame = findViewById(R.id.feed_search_frame);
        mTextSearch = (EditText) findViewById(R.id.search_view);
        mListResults = (ListView) findViewById(R.id.results_listview);
        mEmptyText = (TextView) findViewById(android.R.id.empty);
        mLoadingProgress = findViewById(R.id.loading_progress);
        mResultAdapter = new ResultsAdapter(this);
        mListResults.setEmptyView(mEmptyText);
        mListResults.setAdapter(mResultAdapter);
        mListResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SyndFeed entry =
                        mResultAdapter.getItem(position);
                useEntry(entry.getTitle(), mFeedUrl);
            }
        });

        mTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    // Hide keyboard
                    View f = getCurrentFocus();
                    if (f != null) {
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(f.getWindowToken(),
                                0);
                    }

                    // Issue search
                    mFeedUrl = mTextSearch.getText().toString().trim();
                    Bundle args = new Bundle();
                    args.putString(SEARCHQUERY, mFeedUrl);
                    getLoaderManager().restartLoader(RSSFINDER, args,
                            EditFeedActivity.this);
                    return true;
                }
                return false;
            }
        });

        Button addButton = (Button) findViewById(R.id.add_button);
        addButton
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO error checking and stuff like that
                        ContentValues values = new ContentValues();

                        values.put(COL_TITLE, mTextTitle.getHint().toString().trim());
                        values.put(COL_CUSTOM_TITLE, mTextTitle.getText().toString().trim());
                        values.put(COL_TAG,
                                mTextTag.getText().toString().trim());
                        values.put(COL_URL,
                                mTextUrl.getText().toString().trim());
                        if (id < 1) {
                            Uri uri = getContentResolver()
                                    .insert(URI_FEEDS, values);
                            id = Long.parseLong(uri.getLastPathSegment());
                        } else {
                            getContentResolver().update(Uri.withAppendedPath(
                                    URI_FEEDS,
                                    Long.toString(id)), values, null,
                                    null);
                        }
                        ContentResolverExtensionsKt.notifyAllUris(getContentResolver());
                        ContentResolverExtensionsKt.requestFeedSync(getContentResolver(), id);

                        finish();
                        if (mShouldFinishBack) {
                            // Only care about exit transition
                            overridePendingTransition(R.anim.to_bottom_right,
                                    R.anim.to_bottom_right);
                        }
                    }
                });

        // Consider start intent
        Intent i = getIntent();
        if (i != null) {
            mShouldFinishBack = i.getBooleanExtra(SHOULD_FINISH_BACK, false);
            // Existing id
            id = i.getLongExtra(_ID, -1);
            // Edit like existing, but it's really new
            final boolean template = i.getBooleanExtra(TEMPLATE, false);

            // Existing item
            if (id > 0 || template) {
                mSearchFrame.setVisibility(View.GONE);
                mDetailsFrame.setVisibility(View.VISIBLE);
                if (id > 0) {
                    // Don't allow editing url, but allow copying the text
                    //mTextUrl.setInputType(InputType.TYPE_NULL);
                    //mTextUrl.setTextIsSelectable(true);
                    // Focus on tag
                    mTextTag.requestFocus();
                    addButton.setText(getString(R.string.save));
                } else {
                    mTextUrl.requestFocus();
                }
            } else {
                mSearchFrame.setVisibility(View.VISIBLE);
                mDetailsFrame.setVisibility(View.GONE);
                // Focus on search
                mSearchFrame.requestFocus();
            }

            // Link
            if (i.getDataString() != null) {
                mFeedUrl = i.getDataString().trim();
                mTextSearch.setText(mFeedUrl);
            } else if (i.hasExtra(Intent.EXTRA_TEXT)) {
                mFeedUrl = i.getStringExtra(Intent.EXTRA_TEXT).trim();
                mTextSearch.setText(mFeedUrl);
            }
            // URL
            mTextUrl.setText(mFeedUrl);
            // Title
            if (i.hasExtra(CUSTOM_TITLE)) {
                mTextTitle.setText(i.getStringExtra(CUSTOM_TITLE));
            }
            if (i.hasExtra(FEED_TITLE) && !i.getStringExtra(FEED_TITLE).isEmpty()) {
                mTextTitle.setHint(i.getStringExtra(FEED_TITLE));
            }

            // Tag
            if (i.hasExtra(TAG)) {
                // Use append instead of setText to make sure cursor is at end
                mTextTag.append(i.getStringExtra(TAG));
            }
        }

        // Create an adapter
        final SimpleCursorAdapter tagsAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null, Util.ToStringArray(COL_TAG),
                Util.ToIntArray(android.R.id.text1), 0);

        // Create a loader manager
        final LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks =
                new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(final int id,
                                                         final Bundle args) {
                        String filter = null;
                        if (args != null && args.containsKey(TAGSFILTER)) {
                            filter = COL_TAG + " LIKE '" + args
                                    .getCharSequence(TAGSFILTER, "") + "%'";
                        }
                        return new CursorLoader(EditFeedActivity.this,
                                URI_TAGSWITHCOUNTS,
                                Util.ToStringArray(COL_ID,
                                        COL_TAG), filter, null,
                                Util.SortAlphabeticNoCase(COL_TAG));
                    }

                    @Override
                    public void onLoadFinished(final Loader<Cursor> loader,
                                               final Cursor data) {
                        tagsAdapter.swapCursor(data);
                    }

                    @Override
                    public void onLoaderReset(final Loader<Cursor> loader) {
                        tagsAdapter.swapCursor(null);
                    }
                };

        // Tell adapter how to return result
        tagsAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                if (cursor == null) {
                    return null;
                }

                return cursor.getString(1);
            }
        });

        // Tell adapter how to filter
        tagsAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(final CharSequence constraint) {
                // Restart loader with filter
                Bundle filter = new Bundle();
                filter.putCharSequence(TAGSFILTER, constraint);
                getLoaderManager().restartLoader(LOADER_TAG_SUGGESTIONS,
                        filter, loaderCallbacks);
                // Return null since existing cursor is going to be closed
                return null;
            }
        });

        // Set the adapter
        mTextTag.setAdapter(tagsAdapter);

        // Start suggestions loader
        Bundle args = new Bundle();
        args.putCharSequence(TAGSFILTER, mTextTag.getText());
        getLoaderManager().restartLoader(LOADER_TAG_SUGGESTIONS,
                args, loaderCallbacks);
    }

    private boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();
        if (theme == null ||
                !theme.resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag,
                        true)) {
            // isFloatingWindow flag is not defined in theme
            return false;
        }
        return (floatingWindowFlag.data != 0);
    }

    private void setupFloatingWindow() {
        // configure this Activity as a floating window, dimming the background
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources()
                .getDimensionPixelSize(R.dimen.session_details_floating_width);
        params.height = getResources()
                .getDimensionPixelSize(R.dimen.session_details_floating_height);
        params.alpha = 1;
        params.dimAmount = 0.7f;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    void useEntry(final String title, final String url) {
        mFeedUrl = url.trim();
        mTextUrl.setText(mFeedUrl);
        mTextTitle.setText(android.text.Html.fromHtml(title).toString());
        mDetailsFrame.setVisibility(View.VISIBLE);
        mSearchFrame.setVisibility(View.GONE);
        // Focus on tag
        mTextTag.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTextTag, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mShouldFinishBack) {
            // Only care about exit transition
            overridePendingTransition(0, R.anim.to_bottom_right);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.edit_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home && mShouldFinishBack) {
            // Was launched from inside app, should just go back
            // Action bar handles other cases.
            finish();
            // Only care about exit transition
            overridePendingTransition(R.anim.to_bottom_right,
                    R.anim.to_bottom_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id
     *         The ID whose loader is to be created.
     * @param args
     *         Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<LoaderResult<SyndFeed>> onCreateLoader(final int id,
                                                         final Bundle args) {
        mListResults.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        return new FeedParseLoader(this, args.getString(SEARCHQUERY));
    }

    @Override
    public void onLoadFinished(final Loader<LoaderResult<SyndFeed>> loader,
                               final LoaderResult<SyndFeed> data) {
        mEmptyText.setText(R.string.no_feeds_found);
        mLoadingProgress.setVisibility(View.GONE);
        SyndFeed feed = data.result();
        if (feed != null) {
            mDetailsFrame.setVisibility(View.GONE);
            mSearchFrame.setVisibility(View.VISIBLE);
            mListResults.setVisibility(View.VISIBLE);
            mResultAdapter.setEntries(Collections.singletonList(feed));
        } else {
            mEmptyText.setText(getString(R.string.no_such_feed));
            mEmptyText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(final Loader<LoaderResult<SyndFeed>> loader) {
        mResultAdapter.setEntries(null);
    }

    /**
     * Display feed search results
     */
    class ResultsAdapter extends BaseAdapter {

        private final Context mContext;
        List<SyndFeed> entries = null;

        public ResultsAdapter(final Context context) {
            mContext = context;
        }

        public void setEntries(List<SyndFeed> entries) {
            this.entries = entries;
            notifyDataSetChanged();
        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            if (entries == null) {
                return 0;
            }

            return entries.size();
        }

        /**
         * Get the data item associated with the specified position in the data
         * set.
         *
         * @param position
         *         Position of the item whose data we want within the
         *         adapter's
         *         data set.
         * @return The data at the specified position.
         */
        @Override
        public SyndFeed getItem(final int position) {
            if (entries == null) {
                return null;
            }

            return entries.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position
         *         The position of the item within the adapter's data
         *         set
         *         whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(final int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the
         * data
         * set. You can either
         * create a View manually or inflate it from an XML layout file. When
         * the
         * View is inflated, the
         * parent View (GridView, ListView...) will apply default layout
         * parameters
         * unless you use
         * {@link android.view.LayoutInflater#inflate(int,
         * android.view.ViewGroup,
         * boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position
         *         The position of the item within the adapter's data
         *         set
         *         of the item whose view
         *         we want.
         * @param convertView
         *         The old view to reuse, if possible. Note: You
         *         should
         *         check that this view
         *         is non-null and of an appropriate type before
         *         using.
         *         If it is not possible to convert
         *         this view to display the correct data, this method
         *         can
         *         create a new view.
         *         Heterogeneous lists can specify their number of
         *         view
         *         types, so that this View is
         *         always of the right type (see {@link
         *         #getViewTypeCount()}
         *         and
         *         {@link #getItemViewType(int)}).
         * @param parent
         *         The parent that this view will eventually be
         *         attached
         *         to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(final int position, final View convertView,
                            final ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(mContext)
                                  .inflate(R.layout.view_feed_result, parent, false);
                v.setTag(new ViewHolder(v));
            }

            SyndFeed entry = entries.get(position);
            ViewHolder vh = (ViewHolder) v.getTag();

            vh.entry = entry;

            vh.textTitle.setText(entry.getTitle());
            vh.textDescription.setText(entry.getDescription());
            // Really do want to use the normal link here and not self link
            vh.textUrl.setText(entry.getLink());

            return v;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }

    class ViewHolder {
        public SyndFeed entry;
        public TextView textTitle;
        public TextView textUrl;
        public TextView textDescription;

        public ViewHolder(View v) {
            textTitle = (TextView) v.findViewById(R.id.feed_title);
            textUrl = (TextView) v.findViewById(R.id.feed_url);
            textDescription = (TextView) v.findViewById(R.id.feed_description);
        }
    }
}
