package com.nononsenseapps.feeder.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BottomListSpace(val bottomOffset: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        if (parent.getChildAdapterPosition(view) + 1 == state.itemCount) {
            outRect.set(outRect.left, outRect.top, outRect.right, bottomOffset)
        }
    }
}