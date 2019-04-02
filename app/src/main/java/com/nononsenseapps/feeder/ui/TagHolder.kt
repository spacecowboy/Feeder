package com.nononsenseapps.feeder.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedUnreadCount

class TagHolder(private val onNavigationItemClickListener: OnNavigationItemClickListener,
                private val adapter: FeedsAdapter,
                v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {
    val title: TextView = v.findViewById(R.id.tag_name)
    val unreadCount: TextView = v.findViewById(R.id.tag_unreadcount)
    val expander: ImageView = v.findViewById(R.id.tag_expander)
    var wrap: FeedUnreadCount? = null

    init {
        // expander clicker
        expander.setOnClickListener {
            if (adapter.toggleExpansion(wrap!!)) {
                expander.setImageResource(R.drawable.tinted_expand_less)
            } else {
                expander.setImageResource(R.drawable.tinted_expand_more)
            }
        }
        v.setOnClickListener(this)
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        wrap?.let {
            onNavigationItemClickListener.onNavigationItemClick(
                    ID_UNSET, it.tag, null, it.tag
            )
        }
    }
}
