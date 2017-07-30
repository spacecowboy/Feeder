package com.nononsenseapps.feeder.model

import android.support.v4.util.ArrayMap
import android.support.v4.util.ArraySet
import android.support.v7.util.SortedList
import com.nononsenseapps.feeder.util.getWithDefault


class NestedSortedList<T>(klass: Class<T>, val callback: NestedCallback<T>, initialCapacity: Int = 10) : SortedList<T>(klass, callback, initialCapacity) {

    private val children: ArrayMap<T, MutableSet<T>> = ArrayMap()
    private val expandedParents: MutableSet<T> = ArraySet()

    override fun add(item: T): Int {
        addSubItem(item)
        // Only add to super if item is at top, or in an expanded subtree
        if (isShowing(item)) {
            return super.add(item)
        } else {
            return INVALID_POSITION
        }
    }

    override fun remove(item: T): Boolean {
        super.beginBatchedUpdates()
        // Delete parent(s) of item if no more siblings remain
        val res = nestedRemove(item)
        super.endBatchedUpdates()

        return res
    }

    // Moves upward in hierarchy to find parent to remove
    private fun nestedRemove(item: T): Boolean {
        val parent = callback.getParentOf(item)
        if (parent != null) {
            val siblings = children.getWithDefault(parent, mutableSetOf())
            if (siblings.size == 1) {
                return nestedRemove(parent)
            }
        }
        return removeSubItem(item)
    }

    override fun removeItemAt(index: Int): T? {
        val item = get(index) ?: return null
        remove(item)
        return item
    }

    /**
     * @param parent
     * @return true if parent is expanded after this call, false otherwise
     */
    fun toggleExpansion(parent: T): Boolean {
        if (!children.containsKey(parent)) {
            throw IllegalArgumentException("No such parent: $parent")
        }

        if (expandedParents.contains(parent)) {
            contract(parent)
        } else {
            expand(parent)
        }
        return expandedParents.contains(parent)
    }

    fun isExpanded(parent: T): Boolean {
        return expandedParents.contains(parent)
    }

    fun expand(parent: T) {
        if (expandedParents.contains(parent)) {
            return
        }

        super.beginBatchedUpdates()
        for (child in children.getWithDefault(parent, mutableSetOf())) {
            super.add(child)
        }
        super.endBatchedUpdates()

        expandedParents.add(parent)
    }

    fun contract(parent: T) {
        if (expandedParents.contains(parent)) {
            super.beginBatchedUpdates()
            nestedContract(parent)
            super.endBatchedUpdates()
        }
    }

    fun getParentUnreadCount(parent: T): Int {
        return callback.getParentUnreadCount(parent, children.getWithDefault(parent, mutableSetOf()))
    }

    /**
     * Same as contract except that it is does not handle batching of updates
     */
    private fun nestedContract(parent: T) {
        if (expandedParents.contains(parent)) {
            for (item in children.getWithDefault(parent, mutableSetOf())) {
                super.remove(item)
                nestedContract(item)
            }
            expandedParents.remove(parent)
        }
    }

    private fun addSubItem(item: T) {
        val parent = callback.getParentOf(item)

        if (parent != null) {
            val notify: Boolean
            if (!children.containsKey(parent)) {
                children.put(parent, ArraySet())
                add(parent)
                notify = false
            } else {
                notify = true
            }

            children[parent]?.add(item)

            if (notify) {
                callback.onChanged(super.indexOf(parent), 1)
            }
        } else {
            // Root item should be expanded by default
            expandedParents.add(item)
        }
    }

    private fun removeSubItem(item: T): Boolean {
        if (isShowing(item)) {
            super.remove(item)
        }

        for (child in children.getWithDefault(item, mutableSetOf())) {
            removeSubItem(child)
        }

        expandedParents.remove(item)
        children.remove(item)

        val parent = callback.getParentOf(item)

        if (parent != null) {
            val siblings = children.getWithDefault(parent, mutableSetOf())
            siblings.remove(item)
            callback.onChanged(super.indexOf(parent), 1)
        }
        return false
    }

    private fun isShowing(item: T): Boolean {
        val parent = callback.getParentOf(item)
        return when {
            parent != null -> expandedParents.contains(parent)
            else -> true
        }
    }
}

abstract class NestedCallback<T2> : SortedList.Callback<T2>() {
    abstract fun getItemLevel(item: T2): Int
    abstract fun getParentOf(item: T2): T2?
    abstract fun getParentUnreadCount(parent: T2, ts: Set<T2>): Int
}
