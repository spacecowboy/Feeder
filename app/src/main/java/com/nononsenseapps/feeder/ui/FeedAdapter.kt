package com.nononsenseapps.feeder.ui

import android.content.Context
import android.graphics.Point
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.util.TabletUtils
import org.joda.time.DateTimeZone

const val HEADER_COUNT = 1
const val HEADERTYPE = 0
const val ITEMTYPE = 1

const val PAGE_COUNT = 4
const val PAGE_SIZE = 25

class FeedAdapter(context: Context,
                  internal val feedFragment: FeedFragment) :
        androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    // 64dp at xhdpi is 128 pixels
    private val defImgWidth: Int
    private val defImgHeight: Int

    private val isGrid: Boolean = TabletUtils.isTablet(context)

    @Suppress("DEPRECATION")
    private val linkColor: Int = context.resources.getColor(R.color.accent)
    private var itemMap: androidx.collection.ArrayMap<Long, FeedItemSQL> = androidx.collection.ArrayMap()

    var temps: String = ""

    var currentPage = 0

    private val sortedCallBack: SortedList.Callback<FeedItemSQL> = object : SortedList.Callback<FeedItemSQL>() {
        override fun compare(a: FeedItemSQL, b: FeedItemSQL): Int {
            return if (a.pubDate != null && b.pubDate != null) {
                b.pubDate.compareTo(a.pubDate)
            } else if (a.pubDate != null && b.pubDate == null) {
                -1
            } else if (a.pubDate == null && b.pubDate != null) {
                1
            } else {
                return 0
            }
        }

        override fun onInserted(position: Int, count: Int) {
            // Since there is a footer some special logic is done when the list is initially populated
            if (count == items.size()) {
                this@FeedAdapter.notifyDataSetChanged()
            } else {
                this@FeedAdapter.notifyItemRangeInserted(position, count)
            }
        }

        override fun onRemoved(position: Int, count: Int) {
            // Since there is a footer some special logic is done when the list becomes empty
            if (items.size() == 0) {
                this@FeedAdapter.notifyDataSetChanged()
            } else {
                this@FeedAdapter.notifyItemRangeRemoved(position, count)
            }
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            this@FeedAdapter.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            this@FeedAdapter.notifyItemRangeChanged(position, count)
        }

        override fun areContentsTheSame(a: FeedItemSQL, b: FeedItemSQL): Boolean {
            return a.unread == b.unread &&
                    a.feedtitle.compareTo(b.feedtitle, ignoreCase = true) == 0 &&
                    (a.domain == null && b.domain == null || a.domain != null && a.domain!!.compareTo(b.domain!!, ignoreCase = true) == 0) &&
                    a.plainsnippet.compareTo(b.plainsnippet, ignoreCase = true) == 0 &&
                    a.plaintitle.compareTo(b.plaintitle, ignoreCase = true) == 0
        }

        override fun areItemsTheSame(item1: FeedItemSQL, item2: FeedItemSQL): Boolean = item1.id == item2.id
    }

    val items: SortedList<FeedItemSQL> = SortedList(FeedItemSQL::class.java, sortedCallBack)

    init {
        setHasStableIds(true)

        if (isGrid) {
            defImgHeight = Math.round(context.resources.getDimension(R.dimen.grid_item_size))
            val size = Point()
            feedFragment.activity!!.windowManager.defaultDisplay.getSize(size)
            defImgWidth = size.x / TabletUtils.numberOfFeedColumns(context)
        } else {
            defImgWidth = Math.round(context.resources.getDimension(R.dimen.item_img_def_width))
            defImgHeight = Math.round(context.resources.getDimension(R.dimen.item_img_def_height))
        }
    }


    override fun getItemId(position: Int): Long = when {
        position >= items.size() -> -3
        else -> items.get(position).id
    }

    fun setAllAsRead() {
        for (i in 0 until items.size()) {
            val item = items.get(i)
            if (item.unread) {
                item.unread = false
            }
        }
    }

    fun updateData(map: Map<FeedItemSQL, Int>) {
        val oldItemMap = itemMap
        itemMap = androidx.collection.ArrayMap()
        items.beginBatchedUpdates()
        for (item in map.keys) {
            if ((map[item] ?: -1) >= 0) {
                // Sorted list handles inserting of existing elements
                items.add(item)
                // Add to new map as well
                itemMap[item.id] = item
                // And remove from old
                oldItemMap.remove(item.id)
            } else {
                items.remove(item)
                // And remove from old
                oldItemMap.remove(item.id)
            }
        }
        // If any items remain in old set, they are not present in current result set,
        // remove them. This is pretty much what is done in the delta loader, but if
        // the loader is restarted, then it has no old data to go on.
        for (item in oldItemMap.values) {
            items.remove(item)
        }
        items.endBatchedUpdates()
    }

    override fun getItemCount(): Int = when {
        items.size() > 0 -> HEADER_COUNT + items.size()
        else -> 0
    }


    override fun getItemViewType(position: Int): Int = when {
        position >= items.size() -> HEADERTYPE
        else -> ITEMTYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder = when (viewType) {
        HEADERTYPE -> {
            // Header
            val v = LayoutInflater.from(parent.context)
                    .inflate(
                            R.layout.padding_header_item, parent, false)
            feedFragment.HeaderHolder(v)
        }
        else -> {
            // normal item
            FeedItemHolder(
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.list_story_item, parent, false),
                    this@FeedAdapter)
        }
    }

    override fun onBindViewHolder(vHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                                  position: Int) {
        val newPage = if (position == PAGE_SIZE * (PAGE_COUNT - 1)) {
            currentPage + 1
        } else if (position == PAGE_SIZE) {
            if (currentPage > 0) currentPage - 1 else currentPage
        } else currentPage

        if (newPage != currentPage) {
            currentPage = newPage
            feedFragment.updateFirstVisiblePage()
        }

        if (getItemViewType(position) == HEADERTYPE) {
            // Nothing to bind for padding
            return
        }

        val holder = vHolder as FeedItemHolder

        // Make sure view is reset if it was dismissed
        holder.resetView()

        // Get item
        val item = items.get(position)

        holder.rssItem = item

        // Set the title first
        val titleText = SpannableStringBuilder(item.feedtitle)
        // If no body, display domain of link to be opened
        if (holder.rssItem!!.plainsnippet.isEmpty()) {
            if (holder.rssItem!!.enclosurelink != null && holder.rssItem!!.enclosureFilename != null) {
                titleText.append(" \u2014 ${holder.rssItem!!.enclosureFilename}")
            } else if (holder.rssItem?.domain != null) {
                titleText.append(" \u2014 ${holder.rssItem!!.domain}")
            }

            if (titleText.length > item.feedtitle.length) {
                titleText.setSpan(ForegroundColorSpan(linkColor),
                        item.feedtitle.length + 3, titleText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        holder.authorTextView.text = titleText

        if (item.pubDate == null) {
            holder.dateTextView.visibility = View.GONE
        } else {
            holder.dateTextView.visibility = View.VISIBLE
            holder.dateTextView.text = item.pubDate.withZone(DateTimeZone.getDefault())
                    .toString(FeedFragment.shortDateTimeFormat)
        }

        holder.fillTitle()

        if (item.imageurl?.isNotEmpty() == true) {
            // Take up width
            holder.imageView.visibility = View.VISIBLE
            // Load image when item has been measured
            holder.itemView.viewTreeObserver.addOnPreDrawListener(holder)
        } else {
            holder.imageView.visibility = View.GONE
        }
    }

    fun skipCount(): Int = currentPage * PAGE_SIZE
}
