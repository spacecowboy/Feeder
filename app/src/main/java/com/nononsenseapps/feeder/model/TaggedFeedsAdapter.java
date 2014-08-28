package com.nononsenseapps.feeder.model;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;

import java.util.ArrayList;

public class TaggedFeedsAdapter extends SimpleCursorTreeAdapter {

    private final Context mContext;

    /**
     * Constructor.
     *
     * @param context     The context where the {@link android.widget.ExpandableListView}
     *                    associated with this {@link android.widget.SimpleCursorTreeAdapter}
     *                    is
     *                    running
     * @param cursor      The database cursor
     */
    public TaggedFeedsAdapter(final Context context, final Cursor cursor) {
        super(context, cursor, -1, new String[]{}, new int[]{},
                -1, new String[]{}, new int[]{});
        mContext = context;
    }

    /**
     *
     * @return a CursorLoader which loads the groups this adapter will display
     */
    public CursorLoader getGroupCursorLoader() {
        return new CursorLoader(mContext, FeedSQL.URI_TAGSWITHCOUNTS,
                FeedSQL.FIELDS_TAGSWITHCOUNT, null, null,
                Util.SortAlphabeticNoCase(FeedSQL.COL_TAG));
    }

    /**
     *
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
     * Gets the Cursor for the children at the given group. Subclasses must
     * implement this method to return the children data for a particular
     * group.
     * <p/>
     * If you want to asynchronously query a provider to prevent blocking the
     * UI, it is possible to return null and at a later time call
     * {@link #setChildrenCursor(int, android.database.Cursor)}.
     * <p/>
     * It is your responsibility to manage this Cursor through the Activity
     * lifecycle. It is a good idea to use {@link android.app.Activity#managedQuery} which
     * will handle this for you. In some situations, the adapter will
     * deactivate
     * the Cursor on its own, but this will not always be the case, so please
     * ensure the Cursor is properly managed.
     *
     * @param groupCursor The cursor pointing to the group whose children
     *                    cursor
     *                    should be returned
     * @return The cursor for the children of a particular group, or null.
     */
    @Override
    protected Cursor getChildrenCursor(final Cursor groupCursor) {
        // See JavaDoc, setting cursor in loader
        return null;
    }

    /**
     * Overriding this method to prevent super class from deactivating the child
     * cursor.
     */
    @Override
    public void onGroupCollapsed(int groupPosition) {
        // Called by super unless overridden here
        // deactivateChildrenCursorHelper(groupPosition);
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
     *                      check
     *                      that this view is non-null and of an appropriate
     *                      type before
     *                      using. If it is not possible to convert this view
     *                      to
     *                      display
     *                      the correct data, this method can create a new
     *                      view.
     *                      It is not
     *                      guaranteed that the convertView will have been
     *                      previously
     *                      created by
     *                      {@link #getGroupView(int, boolean, android.view.View,
     *                      android.view.ViewGroup)}.
     * @param parent        the parent that this view will eventually be
     *                      attached to
     * @return the View corresponding to the group at the specified position
     */
    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded,
            View convertView, final ViewGroup parent) {
        Log.d("JONAS", "Children: " + getChildrenCount(groupPosition));
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout
                    .view_feed_tag, parent, false);
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
     *                      check
     *                      that this view is non-null and of an appropriate
     *                      type before
     *                      using. If it is not possible to convert this view
     *                      to
     *                      display
     *                      the correct data, this method can create a new
     *                      view.
     *                      It is not
     *                      guaranteed that the convertView will have been
     *                      previously
     *                      created by
     *                      {@link #getChildView(int, int, boolean,
     *                      android.view.View,
     *                      android.view.ViewGroup)}.
     * @param parent        the parent that this view will eventually be
     *                      attached to
     * @return the View corresponding to the child at the specified position
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
            final boolean isLastChild, View convertView,
            final ViewGroup parent) {
        Log.d("JONAS", "Getting childView " + groupPosition + ", " + childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout
                    .view_feed, parent, false);
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

    private class FeedViewHolder {
        public final View parent;
        public final TextView titleTextView;
        public final TextView unreadCountTextView;
        public long id = -1;
        public String link = null;
        public FeedViewHolder(final View v) {
            parent = v;
            titleTextView = (TextView) v.findViewById(R.id.feed_name);
            unreadCountTextView = (TextView) v.findViewById(R.id.feed_unreadcount);
        }

        public void onClick(final View v) {

        }
    }
}
