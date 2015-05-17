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

package com.nononsenseapps.feeder.model;

import android.support.v7.util.SortedList;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A version of a sortedlist where some items can be expanded/contracted.
 */
public class ExpandableSortedList<T> extends SortedList<T> {

    public static final int TOP_LEVEL = 0;

    private static final int CONTRACTED = 0;
    private static final int EXPANDED = 1;


    private final HashMap<T, HashSet<T>> mGroups;
    private final HashMap<T, Integer> mGroupState;

    /**
     * Callback which handles tree structure.
     */
    private ExpandableCallback<T> mCallback;

    /**
     * Creates a new ExpandableSortedList of type T.
     *
     * @param klass    The class of the contents of the SortedList.
     * @param callback The callback that controls the behavior of SortedList.
     */
    public ExpandableSortedList(Class<T> klass, ExpandableCallback<T> callback) {
        super(klass, callback);
        mCallback = callback;
        mGroups = new HashMap<>();
        mGroupState = new HashMap<>();
    }

    /**
     * Creates a new ExpandableSortedList of type T.
     *
     * @param klass           The class of the contents of the SortedList.
     * @param callback        The callback that controls the behavior of SortedList.
     * @param initialCapacity The initial capacity to hold items.
     */
    public ExpandableSortedList(Class<T> klass, ExpandableCallback<T> callback, int initialCapacity) {
        super(klass, callback, initialCapacity);
        mCallback = callback;
        mGroups = new HashMap<>();
        mGroupState = new HashMap<>();
    }

    /**
     * @param parent
     * @return true if parent is expanded after this call, false otherwise
     */
    public boolean toggleExpansion(T parent) {
        if (EXPANDED == mGroupState.get(parent)) {
            contract(parent);
        } else {
            expand(parent);
        }

        return isExpanded(parent);
    }

    /**
     * @param parent
     * @return true if expanded, false otherwise
     */
    public boolean isExpanded(T parent) {
        return EXPANDED == mGroupState.get(parent);
    }

    public void expand(T parent) {
        if (EXPANDED == mGroupState.get(parent)) {
            return;
        }

        super.beginBatchedUpdates();
        for (T item : mGroups.get(parent)) {
            super.add(item);
        }
        super.endBatchedUpdates();

        mGroupState.put(parent, EXPANDED);
    }

    public void contract(T parent) {
        if (CONTRACTED == mGroupState.get(parent)) {
            return;
        }

        super.beginBatchedUpdates();
        for (T item : mGroups.get(parent)) {
            super.remove(item);
        }
        super.endBatchedUpdates();

        mGroupState.put(parent, CONTRACTED);
    }

    @Override
    public int add(T item) {
        addSubItem(item);
        // Only add to super if item is at top, or in an expanded subtree
        if (isShowing(item)) {
            return super.add(item);
        } else {
            return INVALID_POSITION;
        }
    }

    /**
     * @param item
     * @return true if item is in an expanded subtree, or a root
     */
    private boolean isShowing(T item) {
        if (TOP_LEVEL == mCallback.getItemLevel(item)) {
            return true;
        } else {
            T parent = mCallback.getParentOf(item);
            return EXPANDED == mGroupState.get(parent);
        }
    }

    @Override
    public boolean remove(T item) {
        boolean res = false;
        if (isShowing(item)) {
            res = super.remove(item);
        }
        res |= removeSubItem(item);
        return res;
    }

    @Override
    public T removeItemAt(int index) {
        T item = get(index);
        if (item == null) {
            return null;
        }
        super.removeItemAt(index);
        removeSubItem(item);
        return item;
    }

    private void addSubItem(T item) {
        if (TOP_LEVEL == mCallback.getItemLevel(item)) {
            // Nothing to do
            return;
        }

        T parent = mCallback.getParentOf(item);

        boolean notify = true;
        if (!mGroups.containsKey(parent)) {
            mGroups.put(parent, new HashSet<T>());
            mGroupState.put(parent, CONTRACTED);
            super.add(parent);
            notify = false;
        }

        mGroups.get(parent).add(item);

        if (notify) {
            // Just notify that it should be updated
            mCallback.onChanged(super.indexOf(parent), 1);
        }
    }

    private boolean removeSubItem(T item) {
        if (TOP_LEVEL == mCallback.getItemLevel(item)) {
            // Nothing to do
            return false;
        }

        T parent = mCallback.getParentOf(item);
        if (mGroups.containsKey(parent)) {
            HashSet<T> set = mGroups.get(parent);
            boolean res = set.remove(item);
            if (set.isEmpty()) {
                mGroups.remove(parent);
                mGroupState.remove(parent);
                super.remove(parent);
            } else {
                // Just notify that it should be updated
                mCallback.onChanged(super.indexOf(parent), 1);
            }
            return res;
        } else {
            return false;
        }
    }

    public int getTagUnreadCount(T parent) {
        return mCallback.getParentUnreadCount(parent, mGroups.get(parent));
    }

    public static abstract class ExpandableCallback<T2> extends Callback<T2> {
        abstract public int getItemLevel(T2 item);

        abstract public T2 getParentOf(T2 item);

        abstract public int getParentUnreadCount(T2 parent, HashSet<T2> ts);
    }
}
