package com.nononsenseapps.feeder.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.KodeinAwareDialogFragment
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.model.FeedViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance

const val ARG_FEED_IDS = "feed_ids"
const val ARG_FEED_TITLES = "feed_titles"

class DeleteFeedsDialogFragment : KodeinAwareDialogFragment() {
    private val feedViewModel: FeedViewModel by instance()

    private lateinit var feedIds: LongArray
    private lateinit var feedTitles: Array<String>

    private val checkedItems by lazy {
        BooleanArray(feedIds.size)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        feedIds = arguments?.getLongArray(ARG_FEED_IDS) ?: longArrayOf()
        feedTitles = arguments?.getStringArray(ARG_FEED_TITLES) ?: arrayOf()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_feed)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val idsToDelete = feedIds.zip(checkedItems.asIterable())
                    .filter { (_, checked) ->
                        checked
                    }.map { (id, _) ->
                        id
                    }

                GlobalScope.launch {
                    feedViewModel.deleteFeeds(idsToDelete)
                }

                findNavController().navigate(
                    R.id.action_deleteFeedsDialogFragment_to_feedFragment,
                    bundleOf(ARG_FEED_ID to ID_ALL_FEEDS)
                )
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .setMultiChoiceItems(feedTitles, checkedItems) { _: DialogInterface, position: Int, checked: Boolean ->
                checkedItems[position] = checked
            }

        return builder.create()
    }
}
