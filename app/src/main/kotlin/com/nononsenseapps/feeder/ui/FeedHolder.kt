package com.nononsenseapps.feeder.ui

import android.support.v4.view.GravityCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.FeedSQL

class FeedHolder(private val activity: BaseActivity, v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
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

        activity.onNavigationDrawerItemSelected(item!!.id, item!!.displayTitle, item!!.url, item!!.tag)
    }
}
