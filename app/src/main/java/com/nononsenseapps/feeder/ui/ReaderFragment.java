/*
 * Copyright (c) 2015 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.ui;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedItemSQLKt;
import com.nononsenseapps.feeder.ui.text.HtmlConverter;
import com.nononsenseapps.feeder.ui.text.ImageTextLoader;
import com.nononsenseapps.feeder.util.BundleExtensionsKt;
import com.nononsenseapps.feeder.util.ContentResolverExtensionsKt;
import com.nononsenseapps.feeder.util.FileLog;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.util.TabletUtils;
import com.nononsenseapps.feeder.views.ObservableScrollView;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

import static com.nononsenseapps.feeder.db.FeedItemSQLKt.FEED_ITEM_FIELDS;
import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_ID;
import static com.nononsenseapps.feeder.db.UriKt.URI_FEEDITEMS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderFragment extends Fragment
        implements LoaderManager.LoaderCallbacks {
    public static final String ARG_TITLE = "title";
    public static final String ARG_DESCRIPTION = "body";
    public static final String ARG_LINK = "link";
    public static final String ARG_ENCLOSURE = "enclosure";
    public static final String ARG_IMAGEURL = "imageurl";
    public static final String ARG_ID = "dbid";
    public static final String ARG_FEEDTITLE = "feedtitle";
    public static final String ARG_AUTHOR = "author";
    public static final String ARG_DATE = "date";
    // TODO Change
    static final DateTimeFormatter dateTimeFormat =
            DateTimeFormat.mediumDate().withLocale(Locale.getDefault());
    private static final int TEXT_LOADER = 1;
    private static final int ITEM_LOADER = 2;
    // TODO database id
    private long _id = -1;
    // All content contained in RssItem
    private FeedItemSQL mRssItem;
    private TextView mBodyTextView;
    private ObservableScrollView mScrollView;
    private TextView mTitleTextView;


    public ReaderFragment() {
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
        fragment._id = rssItem.getId();

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
        bundle.putLong(ARG_ID, rssItem.getId());
        bundle.putString(ARG_TITLE, rssItem.getTitle());
        bundle.putString(ARG_LINK, rssItem.getLink());
        bundle.putString(ARG_ENCLOSURE, rssItem.getEnclosurelink());
        bundle.putString(ARG_IMAGEURL, rssItem.getImageurl());
        bundle.putString(ARG_FEEDTITLE, rssItem.getFeedtitle());
        bundle.putString(ARG_AUTHOR, rssItem.getAuthor());
        bundle.putString(ARG_DATE, rssItem.getPubDateString());
        return bundle;
    }

    public static FeedItemSQL RssItemFromBundle(Bundle bundle) {
        return BundleExtensionsKt.asFeedItem(bundle);
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

        if (_id > 0) {
            ContentResolverExtensionsKt.markItemAsRead(getActivity().getContentResolver(), _id, true);
            getLoaderManager().restartLoader(ITEM_LOADER, new Bundle(), this);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final int theLayout;
        if (TabletUtils.isTablet(getActivity())) {
            theLayout = R.layout.fragment_reader_tablet;
        } else {
            theLayout = R.layout.fragment_reader;
        }
        View rootView = inflater.inflate(theLayout, container, false);

        mScrollView =
                (ObservableScrollView) rootView.findViewById(R.id.scroll_view);
        mTitleTextView = (TextView) rootView.findViewById(R.id.story_title);
        mBodyTextView = (TextView) rootView.findViewById(R.id.story_body);
        TextView mAuthorTextView = (TextView) rootView.findViewById(R.id.story_author);
        TextView mFeedTitleTextView = (TextView) rootView.findViewById(R.id
                .story_feedtitle);

        setViewTitle();

        mFeedTitleTextView.setText(mRssItem.getFeedtitle());

        if (mRssItem.getAuthor() == null && mRssItem.getPubDate() != null) {
            mAuthorTextView.setText(getString(R.string.on_date,
                    mRssItem.getPubDate().withZone(DateTimeZone.getDefault())
                            .toString(dateTimeFormat)));
        } else if (mRssItem.getPubDate() != null) {
            mAuthorTextView.setText(getString(R.string.by_author_on_date,
                    mRssItem.getAuthor(),
                    mRssItem.getPubDate().withZone(DateTimeZone.getDefault())
                            .toString(dateTimeFormat)));
        } else {
            mAuthorTextView.setVisibility(View.GONE);
        }

        setViewBody();

        return rootView;
    }

    private void setViewTitle() {
        if (mRssItem.getTitle().isEmpty()) {
            mTitleTextView.setText(mRssItem.getPlaintitle());
        } else {
            mTitleTextView.setText(HtmlConverter.toSpannedWithNoImages(mRssItem.getTitle(), getActivity()));
        }
    }

    private void setViewBody() {
        if (!mRssItem.getDescription().isEmpty()) {
            // Set without images as a place holder
            mBodyTextView.setText(HtmlConverter.toSpannedWithNoImages(mRssItem.getDescription(), getActivity()));

            // Load images in text
            getLoaderManager().restartLoader(TEXT_LOADER, new Bundle(), this);
        }
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, mRssItem.getLink());
        shareActionProvider.setShareIntent(shareIntent);

        // Show/Hide enclosure
        menu.findItem(R.id.action_open_enclosure).setVisible(mRssItem.getEnclosurelink() != null);
        // Add filename to tooltip
        if (mRssItem.getEnclosurelink() != null) {
            String filename = mRssItem.getEnclosureFilename();
            if (filename != null) {
                menu.findItem(R.id.action_open_enclosure).setTitle(filename);
            }
        }

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Uri uri;
        switch (menuItem.getItemId()) {
            case R.id.action_open_in_browser:
                uri = Uri.parse(mRssItem.getLink());
                if (uri.isRelative()) {
                    Toast.makeText(getActivity(), "Sorry, can't handle relative links yet.", Toast.LENGTH_SHORT).show();
                }
                // Open in browser
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), "Couldn't find an activity to open that link with", Toast.LENGTH_SHORT).show();
                    FileLog.singleton.getInstance(getActivity()).d("No such activity: " + e);
                }
                return true;
            case R.id.action_open_enclosure:
                uri = Uri.parse(mRssItem.getEnclosurelink());
                if (uri.isRelative()) {
                    Toast.makeText(getActivity(), R.string.no_activity_for_link, Toast.LENGTH_SHORT).show();
                }
                // Open enclosure link
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.no_activity_for_link, Toast.LENGTH_SHORT).show();
                    FileLog.singleton.getInstance(getActivity()).d("No such activity: " + e);
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        if (id == ITEM_LOADER) {
            return new CursorLoader(getContext(), URI_FEEDITEMS,
                    FEED_ITEM_FIELDS,
                    COL_ID + " IS ?",
                    new String[]{Long.toString(mRssItem.getId())},
                    null);
        } else if (id == TEXT_LOADER) {
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            // Using twice window height since we do scroll vertically
            if (TabletUtils.isTablet(getActivity())) {
                // Tablet has fixed width
                return new ImageTextLoader(getActivity(), mRssItem.getDescription(),
                        new Point(Math.round(getResources().getDimension(R.dimen.reader_tablet_width)), 2 * size.y), PrefUtils.shouldLoadImages(getActivity()));
            } else {
                // Base it on window size
                return new ImageTextLoader(getActivity(), mRssItem.getDescription(),
                        new Point(size.x - 2 * Math.round(getResources().getDimension(R.dimen.keyline_1)), 2 * size.y), PrefUtils.shouldLoadImages(getActivity()));
            }
        }
        return null;
    }


    @Override
    public void onLoadFinished(final Loader loader,
                               final Object data) {
        if (loader.getId() == ITEM_LOADER) {
            Cursor cursor = (Cursor) data;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mRssItem = FeedItemSQLKt.asFeedItem((Cursor) data);
                    setViewTitle();
                    setViewBody();
                }
                cursor.close();
            }
            loader.reset();
        } else if (loader.getId() == TEXT_LOADER) {
            if (data != null) {
                mBodyTextView.setText((Spanned) data);
            }
            loader.reset();
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        // nothing really
    }
}
