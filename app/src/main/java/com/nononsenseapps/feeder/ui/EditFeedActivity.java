package com.nononsenseapps.feeder.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.model.RssSyncService;
import com.nononsenseapps.feeder.model.SyncHelper;
import com.nononsenseapps.feeder.model.apis.BackendAPIClient;
import com.nononsenseapps.feeder.model.apis.GoogleFeedAPIClient;
import com.nononsenseapps.feeder.model.RssSearchLoader;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditFeedActivity extends Activity
        implements LoaderManager.LoaderCallbacks<GoogleFeedAPIClient.FindResponse> {

    public static final String SHOULD_FINISH_BACK = "SHOULD_FINISH_BACK";
    public static final String _ID = "_id";
    public static final String TITLE = "title";
    public static final String TAG = "tag";
    private static final int RSSFINDER = 1;
    private static final String SEARCHQUERY = "searchquery";
    private boolean mShouldFinishBack = false;
    private long id = -1;
    // Views and shit
    private EditText mTextUrl;
    private EditText mTextTitle;
    private EditText mTextTag;
    private EditText mTextSearch;
    private View mDetailsFrame;
    private ListView mListResults;
    private ResultsAdapter mResultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_feed);

        // Setup views
        mTextUrl = (EditText) findViewById(R.id.feed_url);
        mTextTitle = (EditText) findViewById(R.id.feed_title);
        mTextTag = (EditText) findViewById(R.id.feed_tag);
        mDetailsFrame = findViewById(R.id.feed_details_frame);
        mTextSearch = (EditText) findViewById(R.id.search_view);
        mListResults = (ListView) findViewById(R.id.results_listview);
        mResultAdapter = new ResultsAdapter(this);
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

        findViewById(R.id.add_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        // TODO error checking and stuff like that
                        ContentValues values = new ContentValues();

                        values.put(FeedSQL.COL_TITLE,
                                mTextTitle.getText().toString().trim());
                        values.put(FeedSQL.COL_URL,
                                mTextUrl.getText().toString().trim());
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

                        RssSyncService.uploadFeed(EditFeedActivity.this,
                                id,
                                mTextTitle.getText().toString().trim(),
                                mTextUrl.getText().toString().trim(),
                                mTextTag.getText().toString().trim());

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
            mDetailsFrame.setVisibility(View.VISIBLE);
            mListResults.setVisibility(View.GONE);
            // Existing id
            id = i.getLongExtra(_ID, -1);

            // Link
            if (i.getDataString() != null) {
                mTextUrl.setText(i.getDataString());
            } else if (i.hasExtra(Intent.EXTRA_TEXT)) {
                mTextUrl.setText(i.getStringExtra(Intent.EXTRA_TEXT));
            }
            // Title
            if (i.hasExtra(TITLE)) {
                mTextTitle.setText(i.getStringExtra(TITLE));
            }
            // Tag
            if (i.hasExtra(TAG)) {
                mTextTag.setText(i.getStringExtra(TAG));
            }
        }
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
        mTextTitle.setText(android.text.Html.fromHtml(title).toString());
        mTextUrl.setText(url);
        mDetailsFrame.setVisibility(View.VISIBLE);
        mListResults.setVisibility(View.GONE);
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
        return new RssSearchLoader(this, args.getString(SEARCHQUERY));
    }

    @Override
    public void onLoadFinished(final Loader<GoogleFeedAPIClient.FindResponse> loader,
            final GoogleFeedAPIClient.FindResponse data) {
        if (data.responseData.feed != null) {
            useEntry(data.responseData.feed.title,
                    data.responseData.feed.feedUrl);
        } else {
            mResultAdapter.setEntries(data.responseData.entries);
            mDetailsFrame.setVisibility(View.GONE);
            mListResults.setVisibility(View.VISIBLE);
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
