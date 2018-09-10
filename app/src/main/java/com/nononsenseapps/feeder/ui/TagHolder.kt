package com.nononsenseapps.feeder.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.nononsenseapps.feeder.R

class TagHolder(private val activity: BaseActivity, v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {
    val title: TextView = v.findViewById(R.id.tag_name)
    val unreadCount: TextView = v.findViewById(R.id.tag_unreadcount)
    val expander: ImageView = v.findViewById(R.id.tag_expander)
    var wrap: FeedWrapper? = null

    init {
        // expander clicker
        expander.setOnClickListener {
            if (activity.navAdapter!!.toggleExpansion(wrap!!)) {
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
        if (activity.drawerLayout != null) {
            activity.drawerLayout!!.closeDrawers()//GravityCompat.START);
        }

        activity.onNavigationDrawerItemSelected(-1, wrap?.tag, null, wrap?.tag)
    }
}
