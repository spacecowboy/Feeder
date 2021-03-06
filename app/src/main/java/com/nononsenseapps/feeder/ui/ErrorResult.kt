package com.nononsenseapps.feeder.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nononsenseapps.feeder.R

class ErrorResult(view: View) : ViewHolder(view) {
    val textTitle: TextView = view.findViewById(R.id.title)
    val textDescription: TextView = view.findViewById(R.id.description)
}
