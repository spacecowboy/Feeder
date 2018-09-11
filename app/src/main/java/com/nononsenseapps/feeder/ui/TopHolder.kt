package com.nononsenseapps.feeder.ui

import android.view.View
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS

class TopHolder(private val activity: BaseActivity, v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {
    val title: TextView = v.findViewById(R.id.feed_name)
    val unreadCount: TextView = v.findViewById(R.id.feed_unreadcount)

    init {
        title.setText(R.string.all_feeds)
        v.setOnClickListener(this)
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        if (activity.drawerLayout != null) {
            activity.drawerLayout!!.closeDrawers()//GravityCompat.START);
        }

        activity.onNavigationDrawerItemSelected(ID_ALL_FEEDS, null, null, null)
    }
}
