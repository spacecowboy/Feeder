package com.nononsenseapps.feeder.ui

import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.FeedSQL

class FeedHolder(private val activity: BaseActivity, v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {
    val unreadCount: TextView = v.findViewById(R.id.feed_unreadcount)
    val title: TextView = v.findViewById(R.id.feed_name)
    var item: FeedSQL? = null

    init {
        v.setOnClickListener(this)
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        if (activity.drawerLayout != null) {
            activity.drawerLayout!!.closeDrawer(GravityCompat.START)
        }

        item?.let {
            activity.onNavigationDrawerItemSelected(it.id, it.displayTitle, it.url.toString(), it.tag)
        }
    }
}
