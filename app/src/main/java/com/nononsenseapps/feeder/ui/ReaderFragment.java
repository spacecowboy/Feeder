package com.nononsenseapps.feeder.ui;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
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
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.ImageTextLoader;
import com.nononsenseapps.feeder.views.ObservableScrollView;
import com.shirwa.simplistic_rss.RssItem;

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

    private static final int TEXT_LOADER = 1;

    // TODO database id
    private long _id = -1;
    // All content contained in RssItem
    private RssItem mRssItem;
    private TextView mTitleTextView;
    private TextView mBodyTextView;
    private ObservableScrollView mScrollView;
    private Spanned mBodyText = null;


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
    public static ReaderFragment newInstance(long id, RssItem rssItem) {
        ReaderFragment fragment = new ReaderFragment();
        // Save some time on load
        fragment.mRssItem = rssItem;
        fragment._id = id;

        fragment.setArguments(RssItemToBundle(id, rssItem, null));
        return fragment;
    }

    /**
     * Convert an RssItem into a Bundle for use with Fragment Arguments
     *
     * @param id      potential database id
     * @param rssItem to convert
     * @param bundle  may be null
     * @return bundle of rssItem plus id
     */
    public static Bundle RssItemToBundle(long id, RssItem rssItem,
            Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putLong(ARG_ID, id);
        bundle.putString(ARG_TITLE, rssItem.getTitle());
        bundle.putString(ARG_DESCRIPTION, rssItem.getDescription());
        bundle.putString(ARG_LINK, rssItem.getLink());
        bundle.putString(ARG_IMAGEURL, rssItem.getImageUrl());
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

    public static RssItem RssItemFromBundle(Bundle bundle) {
        RssItem rssItem = new RssItem();
        rssItem.setTitle(bundle.getString(ARG_TITLE));
        rssItem.setDescription(bundle.getString(ARG_DESCRIPTION));
        rssItem.setLink(bundle.getString(ARG_LINK));
        rssItem.setImageUrl(bundle.getString(ARG_IMAGEURL));
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

        if (mRssItem.getTitle() == null) {
            mTitleTextView.setText("Nothing to display!");
        } else {
            mTitleTextView
                    .setText(android.text.Html.fromHtml(mRssItem.getTitle()));
        }
        if (mRssItem.getDescription() == null) {
            mBodyTextView.setText("Nothing to display!");
        } else {
            Log.d("JONAS", "Text is:\n" + mRssItem.getDescription());
            // Set without images as a place holder
            mBodyTextView.setText(
                    android.text.Html.fromHtml(mRssItem.getDescription()));
        }

        // Catch clicks on links
        mBodyTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                TextView widget = (TextView) v;
                Object text = widget.getText();
                if (text instanceof Spanned) {
                    Spanned buffer = (Spanned) text;

                    int action = event.getAction();

                    if (action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_DOWN) {
                        int x = (int) event.getX();
                        int y = (int) event.getY();

                        x -= widget.getTotalPaddingLeft();
                        y -= widget.getTotalPaddingTop();

                        x += widget.getScrollX();
                        y += widget.getScrollY();

                        Layout layout = widget.getLayout();
                        int line = layout.getLineForVertical(y);
                        int off = layout.getOffsetForHorizontal(line, x);

                        ClickableSpan[] link =
                                buffer.getSpans(off, off, ClickableSpan.class);

                        // Cant click to the right of a span, if the line ends with the span!
                        if (x > layout.getLineRight(line)) {
                            // Don't call the span
                        } else if (link.length != 0) {
                            //if (action == MotionEvent.ACTION_UP) {
                                link[0].onClick(widget);
                                return true;
 //                           }
//                            else if (action == MotionEvent.ACTION_DOWN) {
//                                Selection.setSelection(buffer,
//                                        buffer.getSpanStart(link[0]),
//                                        buffer.getSpanEnd(link[0]));
//                            return true;
//                            }
                        }
                    }
                }
                return false;
            }
        });

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
        RssItemToBundle(_id, mRssItem, outState);
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
                (ShareActionProvider) shareItem.getActionProvider();

        // Set intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mRssItem.getLink());
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
                    Uri.parse(mRssItem.getLink())));
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
        return new ImageTextLoader(getActivity(), mRssItem.getDescription(),
                new Point((4 * size.x) / 5, (4 * size.y) / 5));
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
