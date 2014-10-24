package com.nononsenseapps.feeder.ui;


import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.ClickableImageSpan;
import com.nononsenseapps.feeder.model.ImageTextLoader;
import com.nononsenseapps.feeder.views.ObservableScrollView;
import com.shirwa.simplistic_rss.RssItem;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Spanned> {
    public static final String ARG_TITLE = "title";
    public static final String ARG_DESCRIPTION = "body";
    public static final String ARG_LINK = "link";
    public static final String ARG_IMAGEURL = "imageurl";
    public static final String ARG_ID = "dbid";
    public static final String ARG_FEEDTITLE = "feedtitle";
    public static final String ARG_AUTHOR = "author";
    public static final String ARG_DATE = "date";

    private static final int TEXT_LOADER = 1;

    // TODO database id
    private long _id = -1;
    // All content contained in RssItem
    private FeedItemSQL mRssItem;
    private TextView mTitleTextView;
    private TextView mBodyTextView;
    private ObservableScrollView mScrollView;
    private Spanned mBodyText = null;
    private TextView mAuthorTextView;
    private TextView mFeedTitleTextView;

    // TODO Change
    static final DateTimeFormatter dateTimeFormat =
            DateTimeFormat.mediumDate().withLocale(Locale.getDefault());


    public ReaderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param rssItem the Item to open in the reader
     * @return A new instance of fragment ReaderFragment.
     */
    public static ReaderFragment newInstance(FeedItemSQL rssItem) {
        ReaderFragment fragment = new ReaderFragment();
        // Save some time on load
        fragment.mRssItem = rssItem;
        fragment._id = rssItem.id;

        fragment.setArguments(RssItemToBundle(rssItem, null));
        return fragment;
    }

    /**
     * Convert an RssItem into a Bundle for use with Fragment Arguments
     *
     * @param rssItem to convert
     * @param bundle  may be null
     * @return bundle of rssItem plus id
     */
    public static Bundle RssItemToBundle(FeedItemSQL rssItem,
            Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putLong(ARG_ID, rssItem.id);
        bundle.putString(ARG_TITLE, rssItem.title);
        bundle.putString(ARG_DESCRIPTION, rssItem.description);
        bundle.putString(ARG_LINK, rssItem.link);
        bundle.putString(ARG_IMAGEURL, rssItem.imageurl);
        bundle.putString(ARG_FEEDTITLE, rssItem.feedtitle);
        bundle.putString(ARG_AUTHOR, rssItem.author);
        bundle.putString(ARG_DATE, rssItem.getPubDateString());
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            _id = savedInstanceState.getLong(ARG_ID);
            mRssItem = RssItemFromBundle(savedInstanceState);

        } else if (mRssItem == null) {
            // Construct from arguments
            _id = getArguments().getLong(ARG_ID, -1);
            mRssItem = RssItemFromBundle(getArguments());
        }

        setHasOptionsMenu(true);

        if (_id > 0) {
            // Mark as read
            RssContentProvider.MarkItemAsRead(getActivity(), _id);
        }
    }

    public static FeedItemSQL RssItemFromBundle(Bundle bundle) {
        FeedItemSQL rssItem = new FeedItemSQL();
        rssItem.title = bundle.getString(ARG_TITLE);
        rssItem.description = (bundle.getString(ARG_DESCRIPTION));
        rssItem.link = (bundle.getString(ARG_LINK));
        rssItem.imageurl = (bundle.getString(ARG_IMAGEURL));
        rssItem.author = (bundle.getString(ARG_AUTHOR));
        rssItem.setPubDate(bundle.getString(ARG_DATE));
        rssItem.feedtitle = (bundle.getString(ARG_FEEDTITLE));
        return rssItem;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =
                inflater.inflate(R.layout.fragment_reader, container, false);

        mScrollView =
                (ObservableScrollView) rootView.findViewById(R.id.scroll_view);
        mTitleTextView = (TextView) rootView.findViewById(R.id.story_title);
        mBodyTextView = (TextView) rootView.findViewById(R.id.story_body);
        mAuthorTextView = (TextView) rootView.findViewById(R.id.story_author);
        mFeedTitleTextView = (TextView) rootView.findViewById(R.id
                .story_feedtitle);

        if (mRssItem.title == null) {
            mTitleTextView.setText("Nothing to display!");
        } else {
            mTitleTextView
                    .setText(android.text.Html.fromHtml(mRssItem.title));
        }
        if (mRssItem.description == null) {
            mBodyTextView.setText("Nothing to display!");
        } else {
            Log.d("JONAS", "Text is:\n" + mRssItem.description);
            // Set without images as a place holder
            mBodyTextView.setText(
                    android.text.Html.fromHtml(mRssItem.description));
        }

        if (mRssItem.feedtitle == null) {
            mFeedTitleTextView.setText("NOthing to display!");
        } else {
            mFeedTitleTextView.setText(mRssItem.feedtitle);
        }

        if (mRssItem.author == null && mRssItem.getPubDate() != null) {
            mAuthorTextView.setText(getString(R.string.on_date,
                    mRssItem.getPubDate().withZone(DateTimeZone.getDefault())
                            .toString(dateTimeFormat)));
        } else if (mRssItem.getPubDate() != null) {
            mAuthorTextView.setText(getString(R.string.by_author_on_date,
                    mRssItem.author,
                    mRssItem.getPubDate().withZone(DateTimeZone.getDefault())
                            .toString(dateTimeFormat)));
        } else {
            mAuthorTextView.setVisibility(View.GONE);
        }

        // Load images in text
        getLoaderManager().restartLoader(TEXT_LOADER, new Bundle(), this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        // TODO set for scrollview
        ((BaseActivity) getActivity()).enableActionBarAutoHide(mScrollView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        RssItemToBundle(mRssItem, outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        getLoaderManager().destroyLoader(TEXT_LOADER);
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reader, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem shareItem = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        // Set intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mRssItem.link);
        shareActionProvider.setShareIntent(shareIntent);

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        final long id = menuItem.getItemId();
        if (id == R.id.action_open_in_browser) {
            // Open in browser
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(mRssItem.link)));
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Spanned> onCreateLoader(final int id, final Bundle args) {
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        // TODO use actual size and not window size
        // Using twice window height since we do scroll vertically
        return new ImageTextLoader(getActivity(), mRssItem.description,
                new Point((5 * size.x) / 6, 2 * size.y));
    }


    @Override
    public void onLoadFinished(final Loader<Spanned> loader,
            final Spanned data) {
        mBodyText = data;
        mBodyTextView.setText(data);
    }

    @Override
    public void onLoaderReset(final Loader<Spanned> loader) {
        // nothing really
    }
}
