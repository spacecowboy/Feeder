package com.nononsenseapps.feeder.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.db.Util;
import com.nononsenseapps.feeder.model.RssSyncHelper;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.util.TabletUtils;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

import retrofit.http.HEAD;


public class FeedFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // TODO change format possibly
    static final DateTimeFormatter shortDateTimeFormat =
            DateTimeFormat.mediumDate().withLocale(Locale.getDefault());

    private static final int FEEDITEMS_LOADER = 1;
    private static final int FEED_LOADER = 2;

    private static final String ARG_FEED_ID = "feed_id";
    private static final String ARG_FEED_TITLE = "feed_title";
    private static final String ARG_FEED_URL = "feed_url";
    private static final String ARG_FEED_TAG = "feed_tag";
    // Filter for database loader
    private static final String ONLY_UNREAD = FeedItemSQL.COL_UNREAD + " IS 1 ";
    private static final String AND_UNREAD = " AND " + ONLY_UNREAD;
    private FeedAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private View mEmptyAddFeed;
    private View mEmptyOpenFeeds;
    // TODO change this
    private long id = -1;
    private String title = "Android Police Dummy";
    private String url = "http://feeds.feedburner.com/AndroidPolice";
    private String tag = "";
    private LinearLayoutManager mLayoutManager;
    private View mCheckAllButton;

    public FeedFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FeedFragment newInstance(long id, String title, String url,
                                           String tag) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_FEED_ID, id);
        args.putString(ARG_FEED_TITLE, title);
        args.putString(ARG_FEED_URL, url);
        args.putString(ARG_FEED_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getLong(ARG_FEED_ID, -1);
            title = getArguments().getString(ARG_FEED_TITLE);
            url = getArguments().getString(ARG_FEED_URL);
            tag = getArguments().getString(ARG_FEED_TAG);
        }

        setHasOptionsMenu(true);

        // Load some RSS
        getLoaderManager().restartLoader(FEEDITEMS_LOADER, new Bundle(), this);
        // Load feed itself if missing info
        if (id > 0 && (title.isEmpty() || url.isEmpty())) {
            getLoaderManager().restartLoader(FEED_LOADER, new Bundle(), this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.fragment_feed, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(android.R.id.list);

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        if (TabletUtils.isTablet(getActivity())) {
            final int cols = TabletUtils.numberOfFeedColumns(getActivity());
            // use a grid layout
            mLayoutManager = new GridLayoutManager(getActivity(),
                    cols);
            // I want the padding header to span the entire width
            ((GridLayoutManager) mLayoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (FeedAdapter.HEADERTYPE == mAdapter.getItemViewType(position)) {
                        return cols;
                    } else {
                        return 1;
                    }
                }
            });
            // TODO, use better dividers such as simple padding
            // I want some dividers
            mRecyclerView.addItemDecoration(new DividerItemDecoration
                    (getActivity(), DividerItemDecoration.VERTICAL_LIST));
            // I want some dividers
            mRecyclerView.addItemDecoration(new DividerItemDecoration
                    (getActivity(), DividerItemDecoration.HORIZONTAL_LIST));
        } else {
            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(getActivity());
            // I want some dividers
            mRecyclerView.addItemDecoration(new DividerItemDecoration
                    (getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Set up the empty view
        mEmptyView = rootView.findViewById(android.R.id.empty);
        mEmptyAddFeed = mEmptyView.findViewById(R.id.empty_add_feed);
        ((TextView) mEmptyAddFeed).setText(android.text.Html.fromHtml
                (getString(R.string.empty_feed_add)));
        mEmptyOpenFeeds = mEmptyView.findViewById(R.id.empty_open_feeds);
        ((TextView) mEmptyOpenFeeds).setText(android.text.Html.fromHtml
                (getString(R.string.empty_feed_open)));

        mEmptyAddFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(getActivity(),
                        EditFeedActivity.class));
            }
        });

        mEmptyOpenFeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ((BaseActivity) getActivity()).openNavDrawer();
            }
        });

        // specify an adapter
        mAdapter = new FeedAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        // check all button is in activity
        mCheckAllButton = getActivity().findViewById(R.id.checkall_button);
        mCheckAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAsRead();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        ((BaseActivity) getActivity()).enableActionBarAutoHide(mRecyclerView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feed_fragment, menu);

        if (id < 1) {
            menu.findItem(R.id.action_edit_feed).setVisible(false);
            menu.findItem(R.id.action_delete_feed).setVisible(false);
        }

        // Set toggleable state
        MenuItem menuItem = menu.findItem(R.id.action_only_unread);
        final boolean onlyUnread = PrefUtils.isShowOnlyUnread(getActivity());
        menuItem.setChecked(onlyUnread);
        menuItem.setTitle(onlyUnread ? R.string.show_all_items : R.string.show_unread_items);
        if (onlyUnread) {
            menuItem.setIcon(R.drawable.ic_action_visibility);
        } else {
            menuItem.setIcon(R.drawable.ic_action_visibility_off);
        }

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void markAsRead() {
        if (this.id > 0) {
            RssContentProvider.MarkFeedAsRead(getActivity(), this.id);
        } else if (this.tag != null) {
            RssContentProvider.MarkItemsAsRead(getActivity(), this.tag);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        final long id = menuItem.getItemId();
        if (id == R.id.action_edit_feed && this.id > 0) {
            Intent i = new Intent(getActivity(), EditFeedActivity.class);
            // TODO do not animate the back movement here
            i.putExtra(EditFeedActivity.SHOULD_FINISH_BACK, true);
            i.putExtra(EditFeedActivity._ID, this.id);
            i.putExtra(EditFeedActivity.TITLE, title);
            i.putExtra(EditFeedActivity.TAG, tag);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        } else if (id == R.id.action_delete_feed && this.id > 0) {
            getActivity().getContentResolver()
                    .delete(FeedSQL.URI_FEEDS, Util.WHEREIDIS,
                            Util.LongsToStringArray(this.id));
            // Upload change
            RssSyncHelper.deleteFeedAsync(getActivity(), url);
            // TODO close fragment
            return true;
        }
//        else if (id == R.id.action_mark_as_read) {
//            markAsRead();
//            return true;
//        }
        else if (id == R.id.action_only_unread) {
            final boolean onlyUnread = !menuItem.isChecked();
            PrefUtils.setPrefShowOnlyUnread(getActivity(), onlyUnread);
            menuItem.setChecked(onlyUnread);
            if (onlyUnread) {
                menuItem.setIcon(R.drawable.ic_action_visibility);
            } else {
                menuItem.setIcon(R.drawable.ic_action_visibility_off);
            }

            menuItem.setTitle(onlyUnread ? R.string.show_all_items : R.string.show_unread_items);
            //getActivity().invalidateOptionsMenu();
            // Restart loader
            getLoaderManager().restartLoader(FEEDITEMS_LOADER, new Bundle(), this);
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    /**
     * @return SQL selection
     */
    protected String getLoaderSelection() {
        String filter = null;
        if (id > 0) {
            filter = FeedItemSQL.COL_FEED + " IS ? ";
        } else if (tag != null) {
            filter = FeedItemSQL.COL_TAG + " IS ? ";
        }

        final boolean onlyUnread = PrefUtils.isShowOnlyUnread(getActivity());
        if (onlyUnread && filter != null) {
            filter += AND_UNREAD;
        } else if (onlyUnread) {
            filter = ONLY_UNREAD;
        }

        return filter;
    }

    /**
     * @return args that match getLoaderSelection
     */
    protected String[] getLoaderSelectionArgs() {
        String[] args = null;
        if (id > 0) {
            args = Util.LongsToStringArray(this.id);
        } else if (tag != null) {
            args = Util.ToStringArray(this.tag);
        }

        return args;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int ID, final Bundle bundle) {
        if (ID == FEEDITEMS_LOADER) {
            return new CursorLoader(getActivity(), FeedItemSQL.URI_FEED_ITEMS,
                    FeedItemSQL.FIELDS, getLoaderSelection(),
                    getLoaderSelectionArgs(),
                    FeedItemSQL.COL_PUBDATE + " DESC");
        } else if (ID == FEED_LOADER) {
            return new CursorLoader(getActivity(),
                    Uri.withAppendedPath(FeedSQL.URI_FEEDS, Long.toString(id)),
                    FeedSQL.FIELDS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> cursorLoader,
                               final Cursor cursor) {
        if (FEEDITEMS_LOADER == cursorLoader.getId()) {
            mAdapter.swapCursor(cursor);
            boolean empty = mAdapter.getItemCount() < 1;
            mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            mRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        } else if (FEED_LOADER == cursorLoader.getId()) {
            if (cursor.moveToNext()) {
                FeedSQL feed = new FeedSQL(cursor);
                this.title = feed.title;
                this.url = feed.url;
            }
            // Reset loader
            getLoaderManager().destroyLoader(cursorLoader.getId());
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {
        if (FEEDITEMS_LOADER == cursorLoader.getId()) {
            mAdapter.swapCursor(null);
        }
    }


    class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int HEADERTYPE = 0;
        public static final int ITEMTYPE = 1;

        // 64dp at xhdpi is 128 pixels
        private static final int defImgWidth = 2 * 128;
        private static final int defImgHeight = 2 * 128;

        private final boolean isGrid;

        private final int unreadTextColor;
        private final int readTextColor;
        private final int linkColor;
        private final Drawable bgProtection;

        String temps;
        private Cursor cursor;

        public FeedAdapter(final Context context) {
            super();

            isGrid = TabletUtils.isTablet(context);

            unreadTextColor = context.getResources()
                    .getColor(R.color.primary_text_default_material_dark);
            readTextColor = context.getResources()
                    .getColor(R.color.secondary_text_material_dark);
            linkColor = context.getResources().getColor(R.color
                    .linked_text_blue);
            bgProtection = context.getResources().getDrawable(R.drawable.bg_protect);
        }

        public void swapCursor(Cursor cursor) {
            // TODO notify about updates
            this.cursor = cursor;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            if (cursor == null || cursor.getCount() == 0) {
                return 0;
            } else {
                // header + the rest
                return 1 + cursor.getCount();
            }
        }


        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return HEADERTYPE;
            } else {
                return ITEMTYPE;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                          final int position) {
            if (getItemViewType(position) == HEADERTYPE) {
                // Header
                return new HeaderHolder(LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.padding_header_item, parent, false));
            } else {
                // normal item
                final int item_layout = R.layout.view_story;
                if (TabletUtils.isTablet(parent.getContext())) {
                    // TODO
                } else {
                    //item_layout = R.layout.view_story;
                }
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(item_layout, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder vHolder,
                                     final int hposition) {
            if (getItemViewType(hposition) == HEADERTYPE) {
                // Nothing to bind for padding
                return;
            }

            final ViewHolder holder = (ViewHolder) vHolder;

            // Compensate for header
            final int position = hposition -1 ;

            // Get item
            cursor.moveToPosition(position);
            final FeedItemSQL item =
                    new FeedItemSQL((Cursor) cursor);

            holder.rssItem = item;

            // Set the title first
            SpannableStringBuilder titleText = new SpannableStringBuilder
                    (item.feedtitle);
            // If no body, display domain of link to be opened
            if (holder.rssItem.description == null ||
                    holder.rssItem.description.isEmpty()) {
                // append to title field
                titleText.append(" \u2014 " +
                        holder.rssItem.getDomain());
                titleText.setSpan(new ForegroundColorSpan(linkColor),
                        item.feedtitle.length() + 3, titleText.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            holder.authorTextView.setText(titleText);

            if (item.getPubDate() == null) {
                holder.dateTextView.setVisibility(View.GONE);
            } else {
                holder.dateTextView.setVisibility(View.VISIBLE);
                holder.dateTextView.setText(
                        item.getPubDate().withZone(DateTimeZone.getDefault())
                                .toString(shortDateTimeFormat));
            }
            if (item.plaintitle == null) {
                holder.titleTextView.setVisibility(View.GONE);
            } else {
                holder.titleTextView.setVisibility(View.VISIBLE);
                // \u2014 is a EM-dash, basically a long version of '-'
                temps = (item.plainsnippet == null || item.plainsnippet.isEmpty()) ?
                        item.plaintitle :
                        item.plaintitle + " \u2014 " + item.plainsnippet;
                Spannable textSpan = new SpannableString(temps);
                // Body is always grey
                textSpan.setSpan(new ForegroundColorSpan(readTextColor),
                        item.plaintitle.length(), temps.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Title depends on status
                textSpan.setSpan(new ForegroundColorSpan(holder.rssItem.isUnread() ?
                                unreadTextColor :
                                readTextColor),
                        0, item.plaintitle.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.titleTextView.setText(textSpan);
            }
            if (item.imageurl == null || item.imageurl.isEmpty()) {
                holder.imageView.setVisibility(View.GONE);
                holder.textGroup.setBackground(null);
            } else {
                int w = holder.imageView.getWidth();
                if (w <= 0) {
                    w = defImgWidth;
                }
                // TODO correct thing to measure height on?
                int h = holder.itemView.getHeight();
                if (h <= 0) {
                    h = defImgHeight;
                }
                Picasso.with(getActivity()).load(item.imageurl).resize(w, h)
                        .centerCrop().into(holder.imageView);
                holder.imageView.setVisibility(View.VISIBLE);
                if (isGrid) {
                    holder.textGroup.setBackground(bgProtection);
                }
            }
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {
            public final TextView titleTextView;
            public final TextView bodyTextView;
            public final TextView dateTextView;
            public final TextView authorTextView;
            public final View textGroup;
            public final ImageView imageView;

            public FeedItemSQL rssItem;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                textGroup = v.findViewById(R.id.story_text);
                titleTextView = (TextView) v.findViewById(R.id.story_title);
                bodyTextView = (TextView) v.findViewById(R.id.story_body);
                dateTextView = (TextView) v.findViewById(R.id.story_date);
                authorTextView = (TextView) v.findViewById(R.id.story_author);
                imageView = (ImageView) v.findViewById(R.id.story_image);
            }

            /**
             * OnItemClickListener replacement.
             * <p/>
             * If a feeditem does not have any content,
             * then it opens the link in the browser directly.
             *
             * @param view
             */
            @Override
            public void onClick(final View view) {

                // Open item if not empty
                if (rssItem.description != null &&
                        !rssItem.description.isEmpty()) {
                    Intent i = new Intent(getActivity(), ReaderActivity.class);
                    //i.setData(Uri.parse(link));
                    i.putExtra(BaseActivity.SHOULD_FINISH_BACK, true);
                    ReaderActivity.setRssExtras(i, rssItem);

                    // TODO add animation
                    Log.d("JONAS", "View size: w: " + view.getWidth() +
                            ", h: " + view.getHeight());
                    Log.d("JONAS", "View pos: l: " + view.getLeft() +
                            ", t: " + view.getTop());
                    ActivityOptions options = ActivityOptions
                            .makeScaleUpAnimation(view, 0, 0, view.getWidth(),
                                    view.getHeight());

                    getActivity().startActivity(i, options.toBundle());
                } else {
                    // Mark as read
                    RssContentProvider.MarkItemAsRead(getActivity(), rssItem.id);
                    // Open in browser since no content was posted
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(rssItem.enclosurelink != null ?
                                    rssItem.enclosurelink :
                                    rssItem.link)));
                }
            }

                /*
                Intent story = new Intent(getActivity(), StoryActivity.class);
                story.putExtra("title", titleTextView.getText());
                story.putExtra("body", bodyTextView.getText());

                Bundle activityOptions = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(getActivity(),
                                    new Pair<View, String>(titleTextView,
                                            "title"),
                                    new Pair<View, String>(bodyTextView,
                                            "body"),
                                    new Pair<View, String>(imageView, "image"));

                    getActivity().setExitSharedElementListener(new SharedElementListener() {
                                @Override
                                public void remapSharedElements(List<String> names,
                                        Map<String, View> sharedElements) {
                                    super.remapSharedElements(names,
                                            sharedElements);
                                    sharedElements.put("title", titleTextView);
                                    sharedElements.put("body", bodyTextView);
                                    sharedElements.put("image", imageView);
                                }
                            });
                    activityOptions = options.toBundle();
                }

                startActivity(story, activityOptions);*/
            //            }
        }
    }

    public class HeaderHolder extends RecyclerView.ViewHolder {

        public HeaderHolder(final View itemView) {
            super(itemView);
        }
    }
}
