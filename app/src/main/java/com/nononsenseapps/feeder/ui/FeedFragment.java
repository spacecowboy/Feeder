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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.util.SortedList;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.db.RssDatabaseService;
import com.nononsenseapps.feeder.db.Util;
import com.nononsenseapps.feeder.model.RssSyncAdapter;
import com.nononsenseapps.feeder.util.FeedItemDeltaCursorLoader;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.util.TabletUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Locale;


public class FeedFragment extends Fragment
        implements LoaderManager.LoaderCallbacks {

    // TODO change format possibly
    static final DateTimeFormatter shortDateTimeFormat =
            DateTimeFormat.mediumDate().withLocale(Locale.getDefault());

    private static final int FEEDITEMS_LOADER = 1;
    private static final int FEED_LOADER = 2;
    private static final int FEED_SETTINGS_LOADER = 3;

    private static final String ARG_FEED_ID = "feed_id";
    private static final String ARG_FEED_TITLE = "feed_title";
    private static final String ARG_FEED_URL = "feed_url";
    private static final String ARG_FEED_TAG = "feed_tag";
    // Filter for database loader
    private static final String ONLY_UNREAD = FeedItemSQL.COL_UNREAD + " IS 1 ";
    private static final String AND_UNREAD = " AND " + ONLY_UNREAD;
    private static final String TAG = "FeedFragment";
    private FeedAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mEmptyView;
    private View mEmptyAddFeed;
    private View mEmptyOpenFeeds;

    private final BroadcastReceiver mSyncReceiver;

    private long id = -1;
    private String title = "";
    private String url = "";
    private String tag = "";
    private String customTitle = "";
    private LinearLayoutManager mLayoutManager;
    private View mCheckAllButton;
    private int notify = 0;
    private CheckedTextView mNotifyCheck;
    private ActionMode mActionMode = null;
    private FeedItemSQL mSelectedItem = null;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextmenu_feedfragment, menu);

            // Show/Hide enclosure
            menu.findItem(R.id.action_open_enclosure).setVisible(mSelectedItem.enclosurelink != null);
            // Add filename to tooltip
            if (mSelectedItem.enclosurelink != null) {
                String filename = mSelectedItem.getEnclosureFilename();
                if (filename != null) {
                    menu.findItem(R.id.action_open_enclosure).setTitle(filename);
                }
            }

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_open_in_browser:
                    // Open in browser
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(mSelectedItem.link)));
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_open_enclosure:
                    // Open enclosure link
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(mSelectedItem.enclosurelink)));
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_toggle_unread:
                    //
                    if (mSelectedItem.isUnread()) {
                        RssDatabaseService.markItemAsRead(getActivity(), mSelectedItem.id);
                    } else {
                        RssDatabaseService.markItemAsUnread(getActivity(), mSelectedItem.id);
                    }
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mSelectedItem = null;
        }
    };

    public FeedFragment() {
        // Listens on sync broadcasts
        mSyncReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (RssSyncAdapter.SYNC_BROADCAST.equals(intent.getAction())) {
                    onSyncBroadcast(intent.getBooleanExtra(RssSyncAdapter.SYNC_BROADCAST_IS_ACTIVE, false));
                }
            }
        };
    }

    /**
     * Returns a new instance of this fragment
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

            // It's a tag, use as title
            if (id < 1) {
                title = tag;
            }

            // Special tag
            if (id < 1 && (title == null || title.isEmpty())) {
                title = getString(R.string.no_tag);
            }
        }

        setHasOptionsMenu(true);

        // Load some RSS
        getLoaderManager().restartLoader(FEEDITEMS_LOADER, Bundle.EMPTY, this);
        // Load feed itself if missing info
        if (id > 0 && (title == null || title.isEmpty())) {
            getLoaderManager().restartLoader(FEED_LOADER, Bundle.EMPTY, this);
        } else {
            // Get notification settings at least
            getLoaderManager().restartLoader(FEED_SETTINGS_LOADER, Bundle.EMPTY, this);
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
            mRecyclerView.addItemDecoration(new DividerColor
                    (getActivity(), DividerColor.VERTICAL_LIST, 0, cols));
            // I want some dividers
            mRecyclerView.addItemDecoration(new DividerColor
                    (getActivity(), DividerColor.HORIZONTAL_LIST));
        } else {
            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(getActivity());
            // I want some dividers
//            mRecyclerView.addItemDecoration(new DividerColor
//                    (getActivity(), DividerColor.VERTICAL_LIST, 0, 1));
        }
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setup swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        // Set the offset so it comes out of the correct place
        final int toolbarHeight = getResources().getDimensionPixelOffset(R.dimen.toolbar_height);
        final int totalToolbarHeight = getResources().getDimensionPixelOffset(R.dimen.total_toolbar_height);
        mSwipeRefreshLayout.setProgressViewOffset(false, toolbarHeight, Math.round(1.5f * totalToolbarHeight));

        // The arrow will cycle between these colors (in order)
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            // Sync this specific feed(s)
            if (id > 0) {
                RssContentProvider.RequestSync(id);
            } else if (tag != null) {
                RssContentProvider.RequestSync(tag);
            } else {
                RssContentProvider.RequestSync();
            }
        });

        // Set up the empty view
        mEmptyView = rootView.findViewById(android.R.id.empty);
        mEmptyAddFeed = mEmptyView.findViewById(R.id.empty_add_feed);
        ((TextView) mEmptyAddFeed).setText(android.text.Html.fromHtml
                (getString(R.string.empty_feed_add)));
        mEmptyOpenFeeds = mEmptyView.findViewById(R.id.empty_open_feeds);
        ((TextView) mEmptyOpenFeeds).setText(android.text.Html.fromHtml
                (getString(R.string.empty_feed_open)));

        mEmptyAddFeed.setOnClickListener(v -> startActivity(new Intent(getActivity(),
                EditFeedActivity.class)));

        mEmptyOpenFeeds.setOnClickListener(v -> ((BaseActivity) getActivity()).openNavDrawer());

        // specify an adapter
        mAdapter = new FeedAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        // check all button
        mCheckAllButton = rootView.findViewById(R.id.checkall_button);
        mCheckAllButton.setOnClickListener(v -> markAsRead());

        // So is toolbar buttons
        mNotifyCheck = (CheckedTextView) getActivity().findViewById(R.id.notifycheck);
        mNotifyCheck.setOnClickListener(v -> {
            // Remember that we are switching to opposite
            notify = mNotifyCheck.isChecked() ? 0 : 1;
            mNotifyCheck.setChecked(notify == 1);
            setNotifications(notify == 1);
        });

        return rootView;
    }

    private void onSyncBroadcast(boolean syncing) {
        // Background syncs trigger the sync layout
        if (mSwipeRefreshLayout.isRefreshing() != syncing) {
            mSwipeRefreshLayout.setRefreshing(syncing);
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        ActionBar ab = ((BaseActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(title);
        }
        ((BaseActivity) getActivity()).enableActionBarAutoHide(mRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        // List might be shorter than screen once item has been read
        ((BaseActivity) getActivity()).showActionBar();
        // Listen on broadcasts
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSyncReceiver, new IntentFilter(RssSyncAdapter.SYNC_BROADCAST));
    }

    @Override
    public void onPause() {
        // Unregister receiver
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSyncReceiver);
        mSwipeRefreshLayout.setRefreshing(false);
        super.onPause();
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
            menu.findItem(R.id.action_add_templated).setVisible(false);
        }

        // Set toggleable state
        MenuItem menuItem = menu.findItem(R.id.action_only_unread);
        final boolean onlyUnread = PrefUtils.isShowOnlyUnread(getActivity());
        menuItem.setChecked(onlyUnread);
        menuItem.setTitle(onlyUnread ? R.string.show_unread_items : R.string.show_all_items);
        if (onlyUnread) {
            menuItem.setIcon(R.drawable.ic_action_visibility_off);
        } else {
            menuItem.setIcon(R.drawable.ic_action_visibility);
        }

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setNotifications(boolean on) {
        RssDatabaseService.setNotify(getActivity(), on, this.id, this.tag);
    }

    private void markAsRead() {
        RssDatabaseService.markFeedAsRead(getActivity(), this.id, this.tag);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        final long id = menuItem.getItemId();
        if (id == R.id.action_sync) {
            // Sync all feeds when menu button pressed
            RssContentProvider.RequestSync();
            return true;
        } else if (id == R.id.action_edit_feed && this.id > 0) {
            Intent i = new Intent(getActivity(), EditFeedActivity.class);
            // TODO do not animate the back movement here
            i.putExtra(EditFeedActivity.SHOULD_FINISH_BACK, true);
            i.putExtra(EditFeedActivity._ID, this.id);
            i.putExtra(EditFeedActivity.TITLE, customTitle);
            i.putExtra(EditFeedActivity.TAG, tag);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        } else if (id == R.id.action_add_templated && this.id > 0) {
            Intent i = new Intent(getActivity(), EditFeedActivity.class);
            // TODO do not animate the back movement here
            i.putExtra(EditFeedActivity.SHOULD_FINISH_BACK, true);
            i.putExtra(EditFeedActivity.TEMPLATE, true);
            //i.putExtra(EditFeedActivity.TITLE, title);
            i.putExtra(EditFeedActivity.TAG, tag);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        } else if (id == R.id.action_delete_feed && this.id > 0) {
            getActivity().getContentResolver()
                    .delete(FeedSQL.URI_FEEDS, Util.WHEREIDIS,
                            Util.LongsToStringArray(this.id));
            RssContentProvider.notifyAllUris(getActivity());

            // Tell activity to open another fragment
            ((FeedActivity) getActivity()).loadFirstFeedInDB(true);
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
                menuItem.setIcon(R.drawable.ic_action_visibility_off);
            } else {
                menuItem.setIcon(R.drawable.ic_action_visibility);
            }

            menuItem.setTitle(onlyUnread ? R.string.show_unread_items : R.string.show_all_items);
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
    public Loader onCreateLoader(final int ID, final Bundle bundle) {
        if (ID == FEEDITEMS_LOADER) {
            return new FeedItemDeltaCursorLoader(getActivity(),
                    FeedItemSQL.URI_FEED_ITEMS.buildUpon()
                            .appendQueryParameter(RssContentProvider.QUERY_PARAM_LIMIT, "50").build(),
                    FeedItemSQL.FIELDS, getLoaderSelection(),
                    getLoaderSelectionArgs(),
                    FeedItemSQL.COL_PUBDATE + " DESC");
        } else if (ID == FEED_LOADER) {
            return new CursorLoader(getActivity(),
                    Uri.withAppendedPath(FeedSQL.URI_FEEDS, Long.toString(id)),
                    FeedSQL.FIELDS, null, null, null);
        } else if (ID == FEED_SETTINGS_LOADER) {
            String where;
            String[] whereArgs;
            if (id > 0) {
                where = Util.WHEREIDIS;
                whereArgs = Util.LongsToStringArray(id);
            } else {
                where = FeedSQL.COL_TAG + " IS ?";
                whereArgs = Util.ToStringArray(tag);
            }
            return new CursorLoader(getActivity(), FeedSQL.URI_FEEDS,
                    Util.ToStringArray("DISTINCT " + FeedSQL.COL_NOTIFY),
                    where, whereArgs, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader cursorLoader,
                               final Object result) {
        if (FEEDITEMS_LOADER == cursorLoader.getId()) {
            HashMap<FeedItemSQL, Integer> map = (HashMap<FeedItemSQL, Integer>) result;
            mAdapter.updateData(map);
            boolean empty = mAdapter.getItemCount() <= 2;
            mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            mSwipeRefreshLayout.setVisibility(empty ? View.GONE : View.VISIBLE);
        } else if (FEED_LOADER == cursorLoader.getId()) {
            Cursor cursor = (Cursor) result;
            if (cursor.moveToFirst()) {
                FeedSQL feed = new FeedSQL(cursor);
                this.title = feed.title;
                this.customTitle = feed.customTitle;
                this.url = feed.url;
                this.notify = feed.notify;

                ((BaseActivity) getActivity()).getSupportActionBar().setTitle(title);
                mNotifyCheck.setChecked(this.notify == 1);
            }
            // Reset loader
            getLoaderManager().destroyLoader(cursorLoader.getId());
        } else if (FEED_SETTINGS_LOADER == cursorLoader.getId()) {
            Cursor cursor = (Cursor) result;
            if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                // Conclusive results
                this.notify = cursor.getInt(0);
            } else {
                this.notify = 0;
            }
            mNotifyCheck.setChecked(this.notify == 1);

            // Reset loader
            getLoaderManager().destroyLoader(cursorLoader.getId());
        }
    }

    @Override
    public void onLoaderReset(final Loader cursorLoader) {
        if (FEEDITEMS_LOADER == cursorLoader.getId()) {
            Log.d(TAG, "onLoaderReset FeedItem");
            //mAdapter.swapCursor(null);
        }
    }

    class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int HEADERTYPE = 0;
        public static final int ITEMTYPE = 1;

        // 64dp at xhdpi is 128 pixels
        private final int defImgWidth;
        private final int defImgHeight;

        private final boolean isGrid;

        private final int unreadTextColor;
        private final int readTextColor;
        private final int linkColor;
        private final Drawable bgProtection;
        private final SortedList<FeedItemSQL> mItems;
        private HashMap<Long, FeedItemSQL> mItemMap;

        String temps;

        public FeedAdapter(final Context context) {
            super();

            setHasStableIds(true);

            mItemMap = new HashMap<>();
            mItems = new SortedList<>(FeedItemSQL.class, new SortedList.Callback<FeedItemSQL>() {
                @Override
                public int compare(FeedItemSQL a, FeedItemSQL b) {
                    final int retval;
                    // Compare pubdates
                    if (a.getPubDate() == null && b.getPubDate() == null) {
                        return 0;
                    } else if (a.getPubDate() != null && b.getPubDate() == null) {
                        retval = -1;
                    } else if (a.getPubDate() == null) {
                        retval = 1;
                    } else {
                        retval = b.getPubDate().compareTo(a.getPubDate());
                    }

                    return retval;
                }

                @Override
                public void onInserted(int position, int count) {
                    FeedAdapter.this.notifyItemRangeInserted(1 + position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    FeedAdapter.this.notifyItemRangeRemoved(1 + position, count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    FeedAdapter.this.notifyItemMoved(1 + fromPosition, 1 + toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    FeedAdapter.this.notifyItemRangeChanged(1 + position, count);
                }

                @Override
                public boolean areContentsTheSame(FeedItemSQL a, FeedItemSQL b) {
                    return a.isUnread() == b.isUnread() &&
                            a.feedtitle.compareToIgnoreCase(b.feedtitle) == 0 &&
                            a.getDomain().compareToIgnoreCase(b.getDomain()) == 0 &&
                            a.plainsnippet.compareToIgnoreCase(b.plainsnippet) == 0 &&
                            a.plaintitle.compareToIgnoreCase(b.plaintitle) == 0;
                }

                @Override
                public boolean areItemsTheSame(FeedItemSQL item1, FeedItemSQL item2) {
                    return item1.id == item2.id;
                }
            });

            isGrid = TabletUtils.isTablet(context);

            unreadTextColor = context.getResources()
                    .getColor(R.color.primary_text_default_material_dark);
            readTextColor = context.getResources()
                    .getColor(R.color.secondary_text_material_dark);
            linkColor = context.getResources().getColor(R.color
                    .accent);
            bgProtection = context.getResources().getDrawable(R.drawable.bg_protect);

            if (isGrid) {
                defImgHeight = Math.round(context.getResources().getDimension(R.dimen.grid_item_size));
                Point size = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                defImgWidth = size.x / TabletUtils.numberOfFeedColumns(context);
            } else {
                defImgWidth = Math.round(context.getResources().getDimension(R.dimen.item_img_def_width));
                defImgHeight = Math.round(context.getResources().getDimension(R.dimen.item_img_def_height));
            }
        }


        @Override
        public long getItemId(final int hposition) {
            if (hposition == 0) {
                // header
                return -2;
            } else {
                int position = hposition - 1;
                if (position >= mItems.size()) {
                    return -3;
                } else {
                    return mItems.get(position).id;
                }
            }
        }

        public void updateData(HashMap<FeedItemSQL, Integer> map) {
            HashMap<Long, FeedItemSQL> oldItemMap = mItemMap;
            mItemMap = new HashMap<>();
            mItems.beginBatchedUpdates();
            for (FeedItemSQL item : map.keySet()) {
                if (map.get(item) >= 0) {
                    // Sorted list handles inserting of existing elements
                    mItems.add(item);
                    // Add to new map as well
                    mItemMap.put(item.id, item);
                    // And remove from old
                    oldItemMap.remove(item.id);
                } else {
                    mItems.remove(item);
                    // And remove from old
                    oldItemMap.remove(item.id);
                }
            }
            // If any items remain in old set, they are not present in current result set,
            // remove them. This is pretty much what is done in the delta loader, but if
            // the loader is restarted, then it has no old data to go on.
            oldItemMap.values().forEach(mItems::remove);
            mItems.endBatchedUpdates();
        }

        @Override
        public int getItemCount() {
            // header + rest
            return 2 + mItems.size();
        }


        @Override
        public int getItemViewType(int position) {
            if (position == 0 || (position - 1) >= mItems.size()) {
                return HEADERTYPE;
            } else {
                return ITEMTYPE;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                          final int viewType) {
            if (viewType == HEADERTYPE) {
                // Header
                final View v = LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.padding_header_item, parent, false);
                return new HeaderHolder(v);
            } else {
                // normal item
                final int item_layout = R.layout.list_story_item;
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

            // Make sure view is reset if it was dismissed
            holder.resetView();

            // Compensate for header
            final int position = hposition - 1;

            // Get item
            final FeedItemSQL item = mItems.get(position);

            holder.rssItem = item;

            // Set the title first
            SpannableStringBuilder titleText = new SpannableStringBuilder
                    (item.feedtitle);
            // If no body, display domain of link to be opened
            if (holder.rssItem.description == null ||
                    holder.rssItem.description.isEmpty()) {
                titleText.append(" \u2014 ");

                if (holder.rssItem.enclosurelink != null) {
                    titleText.append(holder.rssItem.getEnclosureFilename());
                } else {
                    titleText.append(holder.rssItem.getDomain());
                }
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

            holder.fillTitle();

            if (item.imageurl == null || item.imageurl.isEmpty()) {
                holder.imageView.setVisibility(View.GONE);
                //holder.textGroup.setBackground(null);
            } else {
                // Take up width
                holder.imageView.setVisibility(View.INVISIBLE);
                // Load image when item has been measured
                holder.itemView.getViewTreeObserver().addOnPreDrawListener(holder);
            }
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener, ViewTreeObserver.OnPreDrawListener {
            public final TextView titleTextView;
            public final TextView bodyTextView;
            public final TextView dateTextView;
            public final TextView authorTextView;
            //public final View textGroup;
            public final ImageView imageView;
            public final View view;
            private final View bgFrame;
            private final View checkLeft;
            private final View checkRight;
            private final View checkBg;

            public FeedItemSQL rssItem;

            public ViewHolder(View v) {
                super(v);
                //textGroup = v.findViewById(R.id.story_text);
                titleTextView = (TextView) v.findViewById(R.id.story_snippet);
                bodyTextView = (TextView) v.findViewById(R.id.story_body);
                dateTextView = (TextView) v.findViewById(R.id.story_date);
                authorTextView = (TextView) v.findViewById(R.id.story_author);
                imageView = (ImageView) v.findViewById(R.id.story_image);

                checkBg = v.findViewById(R.id.check_bg);
                checkLeft = v.findViewById(R.id.check_left);
                checkRight = v.findViewById(R.id.check_right);
                bgFrame = v.findViewById(R.id.swiping_item);

                this.view = v;
                v.setOnClickListener(this);
                v.setOnLongClickListener(this);
                // Swipe handler
                v.setOnTouchListener(new SwipeDismissTouchListener(v, null, new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        //return rssItem != null && rssItem.isUnread();
                        return rssItem != null;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        rssItem.setUnread(!rssItem.isUnread());
                        // Update the item directly before updating database
                        if (!PrefUtils.isShowOnlyUnread(getActivity())) {
                            // Just update the view state
                            fillTitle();
                            resetView();
                        } else {
                            // Remove it from the dataset directly
                            mItems.remove(rssItem);
                        }
                        // Make database consistent with content
                        if (rssItem.isUnread()) {
                            RssDatabaseService.markItemAsUnread(getActivity(), rssItem.id);
                        } else {
                            RssDatabaseService.markItemAsRead(getActivity(), rssItem.id);
                        }
                    }

                    /**
                     * Called when a swipe is started.
                     *
                     * @param goingRight true if swiping to the right, false if left
                     */
                    @Override
                    public void onSwipeStarted(boolean goingRight) {
                        // SwipeRefreshLayout does not honor requestDisallowInterceptTouchEvent
                        mSwipeRefreshLayout.setEnabled(false);

                        TypedValue typedValue = new TypedValue();
                        if (PrefUtils.isNightMode(getActivity())) {
                            getActivity().getTheme().resolveAttribute(R.attr.nightBGColor, typedValue, true);
                        } else {
                            getActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
                        }
                        bgFrame.setBackgroundColor(typedValue.data);
                        checkBg.setVisibility(View.VISIBLE);
                        if (goingRight) {
                            checkLeft.setVisibility(View.VISIBLE);
                        } else {
                            checkRight.setVisibility(View.VISIBLE);
                        }
                    }

                    /**
                     * Called when user doesn't swipe all the way.
                     */
                    @Override
                    public void onSwipeCancelled() {
                        // SwipeRefreshLayout does not honor requestDisallowInterceptTouchEvent
                        mSwipeRefreshLayout.setEnabled(true);

                        checkBg.setVisibility(View.INVISIBLE);
                        checkLeft.setVisibility(View.INVISIBLE);
                        checkRight.setVisibility(View.INVISIBLE);

                        bgFrame.setBackground(null);
                    }

                    /**
                     * @return the subview which should move
                     */
                    @Override
                    public View getSwipingView() {
                        return bgFrame;
                    }
                }));
            }

            public void resetView() {
                checkBg.setVisibility(View.INVISIBLE);
                checkLeft.setVisibility(View.INVISIBLE);
                checkRight.setVisibility(View.INVISIBLE);
                bgFrame.clearAnimation();
                bgFrame.setAlpha(1.0f);
                bgFrame.setTranslationX(0.0f);
                bgFrame.setBackground(null);
            }

            public void fillTitle() {
                if (rssItem.plaintitle == null) {
                    titleTextView.setVisibility(View.GONE);
                } else {
                    titleTextView.setVisibility(View.VISIBLE);
                    // \u2014 is a EM-dash, basically a long version of '-'
                    temps = (rssItem.plainsnippet == null || rssItem.plainsnippet.isEmpty()) ?
                            rssItem.plaintitle :
                            rssItem.plaintitle + " \u2014 " + rssItem.plainsnippet + "\u2026";
                    Spannable textSpan = new SpannableString(temps);
                    // Body is always grey
                    textSpan.setSpan(new ForegroundColorSpan(readTextColor),
                            rssItem.plaintitle.length(), temps.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // Title depends on status
                    textSpan.setSpan(new ForegroundColorSpan(rssItem.isUnread() ?
                                    unreadTextColor :
                                    readTextColor),
                            0, rssItem.plaintitle.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    titleTextView.setText(textSpan);
                }
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
                if (mActionMode != null) {
                    mActionMode.finish();
                }

                // Open item if not empty
                if (rssItem.description != null &&
                        !rssItem.description.isEmpty()) {
                    Intent i = new Intent(getActivity(), ReaderActivity.class);
                    //i.setData(Uri.parse(link));
                    i.putExtra(BaseActivity.SHOULD_FINISH_BACK, true);
                    ReaderActivity.setRssExtras(i, rssItem);

//                    ActivityOptionsCompat options = ActivityOptionsCompat
//                            .makeScaleUpAnimation(view, 0, 0, view.getWidth(),
//                                    view.getHeight());

//                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                            new Pair<View, String>(titleTextView,
//                                    "title"));
                    //new Pair<View, String>(imageView, "image"));

                    //ActivityCompat.startActivity(getActivity(), i, null);
                    startActivity(i);
                } else {
                    // Mark as read
                    RssDatabaseService.markItemAsRead(getActivity(), rssItem.id);
                    // Open in browser since no content was posted

                    // Use enclosure or link
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(rssItem.enclosurelink != null ?
                                    rssItem.enclosurelink :
                                    rssItem.link)));
                }
            }

            // Called when the user long-clicks on someView
            @Override
            public boolean onLongClick(View view) {
                // Remember which item
                mSelectedItem = this.rssItem;
                if (mActionMode == null) {
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    //view.setSelected(true);
                    view.setActivated(true);
                }

                mActionMode.setSubtitle(mSelectedItem.title);
                mActionMode.setTitle("Selected");

                return true;
            }


            /**
             * Called when item has been measured, it is now the time to insert the image.
             *
             * @return Return true to proceed with the current drawing pass, or false to cancel.
             */
            @Override
            public boolean onPreDraw() {
                imageView.setVisibility(View.VISIBLE);
                // Width is fixed
                int w = defImgWidth;

                // Use the parent's height
                int h = itemView.getHeight();
                //Log.d("JONAS3", "iv:" + imageView.getHeight() + ", item:" + h);

                if (!isDetached() && getActivity() != null) {
                    try {
                        Glide.with(FeedFragment.this).load(rssItem.imageurl).centerCrop().into(imageView);
                    } catch (IllegalArgumentException e) {
                        // Could still happen if we have a race-condition?
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                }
                //Picasso.with(getActivity()).load(rssItem.imageurl).resize(w, h).centerCrop().noFade()
                //        .tag(FeedFragment.this)
                //        .into(imageView);

//                if (isGrid) {
//                    textGroup.setBackground(bgProtection);
//                }

                // Remove as listener
                itemView.getViewTreeObserver().removeOnPreDrawListener(this);

                return true;
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
