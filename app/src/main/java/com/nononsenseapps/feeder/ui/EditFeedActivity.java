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
import android.view.Window;
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
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;
//import com.nononsenseapps.feeder.model.RssSyncHelper;
import com.nononsenseapps.feeder.model.apis.GoogleFeedAPIClient;
import com.nononsenseapps.feeder.model.RssSearchLoader;

import java.util.List;

public class EditFeedActivity extends Activity
        implements LoaderManager.LoaderCallbacks<GoogleFeedAPIClient.FindResponse> {

    public static final String SHOULD_FINISH_BACK = "SHOULD_FINISH_BACK";
    public static final String _ID = "_id";
    public static final String TITLE = "title";
    public static final String TAG = "tag";
    private static final int RSSFINDER = 1;
    private static final String SEARCHQUERY = "searchquery";
    private static final int LOADER_TAG_SUGGESTIONS = 1;
    private static final String TAGSFILTER = "TAGSFILTER";
    private boolean mShouldFinishBack = false;
    private long id = -1;
    // Views and shit
    private EditText mTextTitle;
    private AutoCompleteTextView mTextTag;
    private EditText mTextSearch;
    private View mDetailsFrame;
    private ListView mListResults;
    private ResultsAdapter mResultAdapter;
    private View mSearchFrame;
    private String mFeedUrl = null;
    private TextView mEmptyText;
    private View mLoadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_feed);

        // Setup views
        mTextTitle = (EditText) findViewById(R.id.feed_title);
        mTextTag = (AutoCompleteTextView) findViewById(R.id.feed_tag);
        mDetailsFrame = findViewById(R.id.feed_details_frame);
        mSearchFrame = findViewById(R.id.feed_search_frame);
        mTextSearch = (EditText) findViewById(R.id.search_view);
        mListResults = (ListView) findViewById(R.id.results_listview);
        mEmptyText = (TextView) findViewById(android.R.id.empty);
        mLoadingProgress = findViewById(R.id.loading_progress);
        mResultAdapter = new ResultsAdapter(this);
        mListResults.setEmptyView(mEmptyText);
        mListResults.setAdapter(mResultAdapter);
        mListResults
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> parent,
                            final View view, final int position,
                            final long id) {
                        GoogleFeedAPIClient.Entry entry =
                                mResultAdapter.getItem(position);
                        useEntry(entry.title, entry.url);
                    }
                });

        mTextSearch.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(final TextView v,
                            final int actionId, final KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
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
                            Bundle args = new Bundle();
                            args.putString(SEARCHQUERY,
                                    mTextSearch.getText().toString().trim());
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
                    public void onClick(final View v) {
                        // TODO error checking and stuff like that
                        ContentValues values = new ContentValues();

                        values.put(FeedSQL.COL_TITLE,
                                mTextTitle.getText().toString().trim());
                        values.put(FeedSQL.COL_URL,
                                mFeedUrl);
                        values.put(FeedSQL.COL_TAG,
                                mTextTag.getText().toString().trim());
                        if (id < 1) {
                            Uri uri = getContentResolver()
                                    .insert(FeedSQL.URI_FEEDS, values);
                            id = Long.parseLong(uri.getLastPathSegment());
                        } else {
                            getContentResolver().update(Uri.withAppendedPath(
                                            FeedSQL.URI_FEEDS,
                                            Long.toString(id)), values, null,
                                    null);
                        }

//                        RssSyncHelper.uploadFeedAsync(EditFeedActivity.this, id,
//                                mTextTitle.getText().toString().trim(),
//                                mFeedUrl,
//                                mTextTag.getText().toString().trim());

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

            // Existing item, do not allow URL to be edited
            if (id > 0) {
                mSearchFrame.setVisibility(View.GONE);
                mDetailsFrame.setVisibility(View.VISIBLE);
                // Focus on tag
                mTextTag.requestFocus();
                addButton.setText(getString(R.string.save));
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
            // Title
            if (i.hasExtra(TITLE)) {
                mTextTitle.setText(i.getStringExtra(TITLE));
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
                null, Util.ToStringArray(FeedSQL.COL_TAG),
                Util.ToIntArray(android.R.id.text1), 0);

        // Create a loader manager
        final LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks =
                new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(final int id,
                    final Bundle args) {
                String filter = null;
                if (args != null && args.containsKey(TAGSFILTER)) {
                    filter = FeedSQL.COL_TAG + " LIKE '" + args
                            .getCharSequence(TAGSFILTER, "") + "%'";
                }
                return new CursorLoader(EditFeedActivity.this,
                        FeedSQL.URI_TAGSWITHCOUNTS,
                        Util.ToStringArray(FeedSQL.COL_ID,
                                FeedSQL.COL_TAG), filter, null,
                        Util.SortAlphabeticNoCase(FeedSQL.COL_TAG));
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
            public CharSequence convertToString(final Cursor cursor) {
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
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<GoogleFeedAPIClient.FindResponse> onCreateLoader(final int id,
            final Bundle args) {
        mListResults.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        return new RssSearchLoader(this, args.getString(SEARCHQUERY));
    }

    @Override
    public void onLoadFinished(final Loader<GoogleFeedAPIClient.FindResponse> loader,
            final GoogleFeedAPIClient.FindResponse data) {
        mEmptyText.setText(R.string.no_feeds_found);
        mLoadingProgress.setVisibility(View.GONE);
        if (data.responseData.feed != null) {
            useEntry(data.responseData.feed.title,
                    data.responseData.feed.feedUrl);
        } else {
            mDetailsFrame.setVisibility(View.GONE);
            mSearchFrame.setVisibility(View.VISIBLE);
            mListResults.setVisibility(View.VISIBLE);
            mResultAdapter.setEntries(data.responseData.entries);
        }
    }

    @Override
    public void onLoaderReset(final Loader<GoogleFeedAPIClient.FindResponse> loader) {
        mResultAdapter.setEntries(null);
    }

    /**
     * Display feed search results
     */
    class ResultsAdapter extends BaseAdapter {

        private final Context mContext;
        List<GoogleFeedAPIClient.Entry> entries = null;

        public ResultsAdapter(final Context context) {
            mContext = context;
        }

        public void setEntries(List<GoogleFeedAPIClient.Entry> entries) {
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
         * @param position Position of the item whose data we want within the
         *                 adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public GoogleFeedAPIClient.Entry getItem(final int position) {
            if (entries == null) {
                return null;
            }

            return entries.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data
         *                 set
         *                 whose row id we want.
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
         * @param position    The position of the item within the adapter's data
         *                    set
         *                    of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You
         *                    should
         *                    check that this view
         *                    is non-null and of an appropriate type before
         *                    using.
         *                    If it is not possible to convert
         *                    this view to display the correct data, this method
         *                    can
         *                    create a new view.
         *                    Heterogeneous lists can specify their number of
         *                    view
         *                    types, so that this View is
         *                    always of the right type (see {@link
         *                    #getViewTypeCount()}
         *                    and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be
         *                    attached
         *                    to
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

            GoogleFeedAPIClient.Entry entry = entries.get(position);
            ViewHolder vh = (ViewHolder) v.getTag();

            vh.entry = entry;

            vh.textTitle.setText(android.text.Html.fromHtml(entry.title));
            vh.textDescription
                    .setText(android.text.Html.fromHtml(entry.contentSnippet));
            vh.textUrl.setText(entry.url);

            return v;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }

    class ViewHolder {
        public GoogleFeedAPIClient.Entry entry;
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
