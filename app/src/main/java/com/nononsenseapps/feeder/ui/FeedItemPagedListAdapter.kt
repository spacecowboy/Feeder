package com.nononsenseapps.feeder.ui

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.PreviewItem
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class FeedItemPagedListAdapter(context: Context, private val dismissedListener: DismissedListener) :
        PagedListAdapter<PreviewItem, RecyclerView.ViewHolder>(Differ) {

    private val shortDateTimeFormat: DateTimeFormatter =
            DateTimeFormat.mediumDate().withLocale(Locale.getDefault())
    private val linkColor: Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.resources.getColor(R.color.accent, null)
            } else {
                @Suppress("DEPRECATION")
                context.resources.getColor(R.color.accent)
            }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: ID_UNSET
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            FeedItemHolder(
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.list_story_item, parent, false), dismissedListener)

    override fun onBindViewHolder(vHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = vHolder as FeedItemHolder

        // Make sure view is reset if it was dismissed
        holder.resetView()

        // Get item
        val item = getItem(position)

        holder.rssItem = item

        if (item == null) {
            // Placeholder
            return
        }

        // Set the title first
        val titleText = SpannableStringBuilder(item.feedTitle)
        // If no body, display domain of link to be opened
        if (holder.rssItem!!.plainSnippet.isEmpty()) {
            if (holder.rssItem!!.enclosureLink != null && holder.rssItem!!.enclosureFilename != null) {
                titleText.append(" \u2014 ${holder.rssItem!!.enclosureFilename}")
            } else if (holder.rssItem?.domain != null) {
                titleText.append(" \u2014 ${holder.rssItem!!.domain}")
            }

            if (titleText.length > item.feedTitle.length) {
                titleText.setSpan(ForegroundColorSpan(linkColor),
                        item.feedTitle.length + 3, titleText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        holder.authorTextView.text = titleText

        item.pubDate.let {
            if (it == null) {
                holder.dateTextView.visibility = View.GONE
            } else {
                holder.dateTextView.visibility = View.VISIBLE
                holder.dateTextView.text = it.withZone(DateTimeZone.getDefault())
                        .toString(shortDateTimeFormat)
            }
        }

        holder.fillTitle()

        if (item.imageUrl?.isNotEmpty() == true) {
            // Take up width
            holder.imageView.visibility = View.VISIBLE
            // Load image when item has been measured
            holder.itemView.viewTreeObserver.addOnPreDrawListener(holder)
        } else {
            holder.imageView.visibility = View.GONE
        }
    }

}
