package com.nononsenseapps.feeder.model;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;

/**
 * An expandable list adapter which displays feeds sorted under tags. The
 * adapter is designed to be used together with CursorLoaders. See
 * {@link #getGroupCursorLoader()} and {@link #getChildCursorLoader(String)}.
 *
 * To use, do something as follows in your activity/fragment:
 * public Loader<Cursor> onCreateLoader(final int id, final Bundle bundle) {
 *     // GROUP_LOADER must be a negative number due to the children
 *     if (id == GROUP_LOADER) {
 *         return adapter.getGroupCursorLoader();
 *     } else {
 *         // Using id as group position
 *         return adapter.getChildCursorLoader(bundle.getString("tag"));
 *     }
 * }
 *
 * public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
 *     if (cursorLoader.getId() == GROUP_LOADER) {
 *         adapter.setGroupCursor(cursor);
 *         // Load child cursors
 *         for (int i = 0; i < cursor.getCount(); i++) {
 *             Cursor group = adapter.getGroup(i);
 *             Bundle b = new Bundle();
 *             // Make sure position is correct
 *             b.putString("tag", group.getString(1));
 *             getLoaderManager().restartLoader(i, b, this);
 *         }
 *     } else {
 *         // Child loader
 *         mNavAdapter.setChildrenCursor(cursorLoader.getId(), cursor);
 *     }
 * }
 *
 * public void onLoaderReset(final Loader<Cursor> cursorLoader) {
 *    if (cursorLoader.getId() == GROUP_LOADER) {
 *        adapter.setGroupCursor(null);
 *    } else {
 *        adapter.setChildrenCursor(cursorLoader.getId(), null);
 *    }
 * }
 */
public class TaggedFeedsAdapter extends BaseExpandableListAdapter {

    private final Context mContext;
    private final LongSparseArray<Cursor> childCursors =
            new LongSparseArray<Cursor>();
    private Cursor groupCursor = null;

    /**
     * Constructor.
     *
     * @param context The context
     */
    public TaggedFeedsAdapter(final Context context) {
        mContext = context;
    }

    /**
     * @return a CursorLoader which loads the groups this adapter will display
     */
    public CursorLoader getGroupCursorLoader() {
        return new CursorLoader(mContext, FeedSQL.URI_TAGSWITHCOUNTS,
                FeedSQL.FIELDS_TAGSWITHCOUNT, null, null,
                Util.SortAlphabeticNoCase(FeedSQL.COL_TAG));
    }

    /**
     * Set the groups' cursor
     *
     * @param c cursor for groups
     */
    public void setGroupCursor(final Cursor c) {
        groupCursor = c;
        if (c == null) {
            childCursors.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * @param tag of requested children
     * @return a CursorLoader which loads feeds with the specified tag
     */
    public CursorLoader getChildCursorLoader(final String tag) {
        return new CursorLoader(mContext, FeedSQL.URI_FEEDSWITHCOUNTS,
                FeedSQL.FIELDS_VIEWCOUNT,
                Util.SelectionCouldBeNull(FeedSQL.COL_TAG, tag),
                Util.SelectionValCouldBeNull(tag),
                Util.SortAlphabeticNoCase(FeedSQL.COL_TITLE));
    }

    /**
     * @param groupPosition of the group in which the children exist
     * @param c             the cursor for the children
     */
    public void setChildrenCursor(final int groupPosition, final Cursor c) {
        if (getGroupCount() <= groupPosition) {
            return;
        }

        final long groupId = getGroupId(groupPosition);
        childCursors.put(groupId, c);
        notifyDataSetChanged();
    }

    /**
     * Gets the number of groups.
     *
     * @return the number of groups
     */
    @Override
    public int getGroupCount() {
        if (groupCursor == null) {
            return 0;
        } else {
            return groupCursor.getCount();
        }
    }

    /**
     * Gets the number of children in a specified group.
     *
     * @param groupPosition the position of the group for which the children
     *                      count should be returned
     * @return the children count in the specified group
     */
    @Override
    public int getChildrenCount(final int groupPosition) {
        Cursor c = childCursors.get(getGroupId(groupPosition));
        if (c == null) {
            return 0;
        } else {
            return c.getCount();
        }
    }

    /**
     * Gets the data associated with the given group.
     *
     * @param groupPosition the position of the group
     * @return a cursor set to the position of the specified group, or null
     */
    @Override
    public Cursor getGroup(final int groupPosition) {
        if (groupCursor != null) {
            groupCursor.moveToPosition(groupPosition);
        }

        return groupCursor;
    }

    /**
     * Gets the data associated with the given child within the given group.
     *
     * @param groupPosition the position of the group that the child resides in
     * @param childPosition the position of the child with respect to other
     *                      children in the group
     * @return a cursor set to the position of the child, or null
     */
    @Override
    public Cursor getChild(final int groupPosition, final int childPosition) {
        if (groupCursor == null) {
            return null;
        }

        Cursor childCursor = childCursors.get(getGroupId(groupPosition));
        if (childCursor != null) {
            childCursor.moveToPosition(childPosition);
        }

        return childCursor;
    }

    /**
     * Gets the ID for the group at the given position. This group ID must be
     * unique across groups. The combined ID (see
     * {@link #getCombinedGroupId(long)}) must be unique across ALL items
     * (groups and all children).
     *
     * @param groupPosition the position of the group for which the ID is
     *                      wanted
     * @return the ID associated with the group
     */
    @Override
    public long getGroupId(final int groupPosition) {
        return getGroup(groupPosition).getLong(0);
    }

    /**
     * Gets the ID for the given child within the given group. This ID must be
     * unique across all children within the group. The combined ID (see
     * {@link #getCombinedChildId(long, long)}) must be unique across ALL items
     * (groups and all children).
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group for
     *                      which
     *                      the ID is wanted
     * @return the ID associated with the child
     */
    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return getChild(groupPosition, childPosition).getLong(0);
    }

    /**
     * Indicates whether the child and group IDs are stable across changes to
     * the underlying data.
     *
     * @return whether or not the same ID always refers to the same object
     */
    @Override
    public boolean hasStableIds() {
        // Uniqueness is enforced by the database, so true
        return true;
    }

    /**
     * Gets a View that displays the given group. This View is only for the
     * group--the Views for the group's children will be fetched using
     * {@link #getChildView(int, int, boolean, android.view.View,
     * android.view.ViewGroup)}.
     *
     * @param groupPosition the position of the group for which the View is
     *                      returned
     * @param isExpanded    whether the group is expanded or collapsed
     * @param convertView   the old view to reuse, if possible. You should
     *                      check that this view is non-null and of an
     *                      appropriate type before using. If it is not
     *                      possible to convert this view to display the
     *                      correct data, this method can create a new view.
     * @param parent        the parent that this view will eventually be
     *                      attached to
     * @return the View corresponding to the group at the specified position
     */
    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded,
            View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.view_feed_tag, parent, false);
            convertView.setTag(new GroupViewHolder(convertView));
        }

        final Cursor c = getGroup(groupPosition);
        final GroupViewHolder holder = (GroupViewHolder) convertView.getTag();

        if (c.getString(1).isEmpty()) {
            // TODO use string resource
            holder.titleTextView.setText("No tag");
        } else {
            holder.titleTextView.setText(c.getString(1));
        }
        if (c.isNull(2) || c.getString(2).isEmpty()) {
            holder.unreadCountTextView.setVisibility(View.GONE);
        } else {
            holder.unreadCountTextView.setVisibility(View.VISIBLE);
            holder.unreadCountTextView.setText(c.getString(2));
        }

        return convertView;
    }

    /**
     * Gets a View that displays the data for the given child within the given
     * group.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child (for which the View is
     *                      returned) within the group
     * @param isLastChild   Whether the child is the last child within the
     *                      group
     * @param convertView   the old view to reuse, if possible. You should
     *                      check that this view is non-null and of an
     *                      appropriate type before using. If it is not
     *                      possible to convert this view to display the
     *                      correct data, this method can create a new view.
     * @param parent        the parent that this view will eventually be
     *                      attached to
     * @return the View corresponding to the child at the specified position
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
            final boolean isLastChild, View convertView,
            final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.view_feed, parent, false);
            convertView.setTag(new FeedViewHolder(convertView));
        }

        final FeedViewHolder holder = (FeedViewHolder) convertView.getTag();
        final Cursor c = getChild(groupPosition, childPosition);
        // See FEED_COLS for positions
        holder.titleTextView.setText(c.getString(1));
        holder.id = c.getLong(0);
        holder.link = c.getString(2);

        if (c.isNull(4) || c.getString(4).isEmpty()) {
            holder.unreadCountTextView.setVisibility(View.GONE);
        } else {
            holder.unreadCountTextView.setVisibility(View.VISIBLE);
            holder.unreadCountTextView.setText(c.getString(4));
        }

        return convertView;
    }

    /**
     * Whether the child at the specified position is selectable.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group
     * @return whether the child is selectable.
     */
    @Override
    public boolean isChildSelectable(final int groupPosition,
            final int childPosition) {
        // All children are selectable
        return true;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        // Nothing to do here
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        // nothing to do here
    }

    /**
     * A ViewHolder for group views
     */
    private class GroupViewHolder {
        public final View parent;
        public final TextView titleTextView;
        public final TextView unreadCountTextView;

        public GroupViewHolder(final View v) {
            parent = v;
            titleTextView = (TextView) v.findViewById(R.id.tag_name);
            unreadCountTextView =
                    (TextView) v.findViewById(R.id.tag_unreadcount);
        }
    }

    /**
     * A ViewHolder for child views
     */
    private class FeedViewHolder {
        public final View parent;
        public final TextView titleTextView;
        public final TextView unreadCountTextView;
        public long id = -1;
        public String link = null;

        public FeedViewHolder(final View v) {
            parent = v;
            titleTextView = (TextView) v.findViewById(R.id.feed_name);
            unreadCountTextView =
                    (TextView) v.findViewById(R.id.feed_unreadcount);
        }
    }
}
