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


import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.ui.text.ImageTextLoader;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.util.SystemUtils;
import com.nononsenseapps.feeder.util.TabletUtils;
import com.nononsenseapps.feeder.views.ObservableScrollView;
import com.nononsenseapps.feeder.ui.text.HtmlConverter;

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
    // TODO database id
    private long _id = -1;
    // All content contained in RssItem
    private FeedItemSQL mRssItem;
    private TextView mBodyTextView;
    private ObservableScrollView mScrollView;


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
        bundle.putString(ARG_ENCLOSURE, rssItem.enclosurelink);
        bundle.putString(ARG_IMAGEURL, rssItem.imageurl);
        bundle.putString(ARG_FEEDTITLE, rssItem.feedtitle);
        bundle.putString(ARG_AUTHOR, rssItem.author);
        bundle.putString(ARG_DATE, rssItem.getPubDateString());
        return bundle;
    }

    public static FeedItemSQL RssItemFromBundle(Bundle bundle) {
        FeedItemSQL rssItem = new FeedItemSQL();
        rssItem.id = bundle.getLong(ARG_ID, -1);
        rssItem.title = bundle.getString(ARG_TITLE);
        rssItem.description = (bundle.getString(ARG_DESCRIPTION));
        rssItem.link = (bundle.getString(ARG_LINK));
        rssItem.enclosurelink = (bundle.getString(ARG_ENCLOSURE));
        rssItem.imageurl = (bundle.getString(ARG_IMAGEURL));
        rssItem.author = (bundle.getString(ARG_AUTHOR));
        rssItem.setPubDate(bundle.getString(ARG_DATE));
        rssItem.feedtitle = (bundle.getString(ARG_FEEDTITLE));
        return rssItem;
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
            RssContentProvider.MarkItemAsRead(getActivity(), _id);
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
        TextView mTitleTextView = (TextView) rootView.findViewById(R.id.story_title);
        mBodyTextView = (TextView) rootView.findViewById(R.id.story_body);
        TextView mAuthorTextView = (TextView) rootView.findViewById(R.id.story_author);
        TextView mFeedTitleTextView = (TextView) rootView.findViewById(R.id
                .story_feedtitle);

        if (mRssItem.title == null) {
            mTitleTextView.setText(R.string.nothing_to_display);
        } else {
            mTitleTextView.setText(HtmlConverter.toSpannedWithNoImages(mRssItem.title, getActivity()));
        }
        if (mRssItem.description == null) {
            mBodyTextView.setText(R.string.nothing_to_display);
        } else {
            // Set without images as a place holder
            mBodyTextView.setText(HtmlConverter.toSpannedWithNoImages(mRssItem.description, getActivity()));
        }

        if (mRssItem.feedtitle == null) {
            mFeedTitleTextView.setText(R.string.nothing_to_display);
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

        // Show/Hide enclosure
        menu.findItem(R.id.action_open_enclosure).setVisible(mRssItem.enclosurelink != null);
        // Add filename to tooltip
        if (mRssItem.enclosurelink != null) {
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
        switch (menuItem.getItemId()) {
            case R.id.action_open_in_browser:
                // Open in browser
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(mRssItem.link)));
                return true;
            case R.id.action_open_enclosure:
                // Open enclosure link
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(mRssItem.enclosurelink)));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public Loader<Spanned> onCreateLoader(final int id, final Bundle args) {
        if (PrefUtils.shouldLoadImagesOnlyOnWIfi(getActivity()) && !SystemUtils.currentlyOnWifi(getActivity())) {
            return new NothingLoader(getActivity());
        }

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        // Using twice window height since we do scroll vertically
        if (TabletUtils.isTablet(getActivity())) {
            // Tablet has fixed width
            return new ImageTextLoader(getActivity(), mRssItem.description,
                    new Point(Math.round(getResources().getDimension(R.dimen.reader_tablet_width)),
                            2 * size.y));
        } else {
            // Base it on window size
            return new ImageTextLoader(getActivity(), mRssItem.description,
                    new Point(size.x - 2 * Math.round(getResources().getDimension(R.dimen.keyline_1)),
                            2 * size.y));
        }
    }


    @Override
    public void onLoadFinished(final Loader<Spanned> loader,
                               final Spanned data) {
        if (data != null) {
            mBodyTextView.setText(data);
        }
    }

    @Override
    public void onLoaderReset(final Loader<Spanned> loader) {
        // nothing really
    }
}
