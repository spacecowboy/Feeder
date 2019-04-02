package com.nononsenseapps.feeder.ui

import android.view.View
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.FeedUnreadCount

class FeedHolder(private val onNavigationItemClickListener: OnNavigationItemClickListener, v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {
    val unreadCount: TextView = v.findViewById(R.id.feed_unreadcount)
    val title: TextView = v.findViewById(R.id.feed_name)
    var item: FeedUnreadCount? = null

    init {
        v.setOnClickListener(this)
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        item?.let {
            onNavigationItemClickListener.onNavigationItemClick(
                    it.id, it.displayTitle, it.url.toString(), it.tag
            )
        }
    }
}
