package com.nononsenseapps.feeder.ui

import androidx.recyclerview.widget.DiffUtil
import com.nononsenseapps.feeder.model.PreviewItem

object PreviewItemDiffer : DiffUtil.ItemCallback<PreviewItem>() {
    override fun areItemsTheSame(oldItem: PreviewItem, newItem: PreviewItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(a: PreviewItem, b: PreviewItem): Boolean {
        return a.unread == b.unread &&
            a.feedDisplayTitle.compareTo(b.feedDisplayTitle, ignoreCase = true) == 0 &&
            (a.domain == null && b.domain == null || a.domain != null && a.domain!!.compareTo(b.domain!!, ignoreCase = true) == 0) &&
            a.plainSnippet.compareTo(b.plainSnippet, ignoreCase = true) == 0 &&
            a.plainTitle.compareTo(b.plainTitle, ignoreCase = true) == 0 &&
            a.feedOpenArticlesWith.compareTo(b.feedOpenArticlesWith) == 0
    }
}
