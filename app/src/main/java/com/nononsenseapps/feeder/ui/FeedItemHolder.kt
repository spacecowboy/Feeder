package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_BROWSER
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_READER
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_WEBVIEW
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.bundle
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
@FlowPreview
@ExperimentalCoroutinesApi
class FeedItemHolder(
    val view: View,
    private val feedItemsViewModel: FeedItemsViewModel,
    private val settingsViewModel: SettingsViewModel,
    private val actionCallback: ActionCallback
) : ViewHolder(view),
    View.OnClickListener,
    ViewTreeObserver.OnPreDrawListener,
    KodeinAware,
    View.OnCreateContextMenuListener {
    private val TAG = "FeedItemHolder"
    val titleTextView: TextView = view.findViewById(R.id.story_snippet)
    val dateTextView: TextView = view.findViewById(R.id.story_date)
    val authorTextView: TextView = view.findViewById(R.id.story_author)
    val imageView: ImageView = view.findViewById(R.id.story_image)
    private val bgFrame: View = view.findViewById(R.id.swiping_item)
    private val checkLeft: View = view.findViewById(R.id.check_left)
    private val checkRight: View = view.findViewById(R.id.check_right)
    private val checkBg: View = view.findViewById(R.id.check_bg)

    var rssItem: PreviewItem? = null

    override val kodein: Kodein by closestKodein(view.context)
    val prefs: Prefs by instance()
    private val imageLoader: ImageLoader by instance()
    private var imageFetchJob: Job? = null

    init {
        view.setOnClickListener(this)
        view.setOnCreateContextMenuListener(this)
        // Swipe handler
        view.setOnTouchListener(
            SwipeDismissTouchListener(
                view, null,
                object : SwipeDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Any?): Boolean = rssItem != null

                    override fun onDismiss(view: View, token: Any?) {
                        actionCallback.onDismiss(rssItem)
                    }

                    /**
                     * Called when a swipe is started.
                     *
                     * @param goingRight true if swiping to the right, false if left
                     */
                    override fun onSwipeStarted(goingRight: Boolean) {
                        actionCallback.onSwipeStarted()

                        bgFrame.setBackgroundColor(settingsViewModel.backgroundColor)

                        checkBg.visibility = View.VISIBLE
                        if (goingRight) {
                            checkLeft.visibility = View.VISIBLE
                        } else {
                            checkRight.visibility = View.VISIBLE
                        }
                    }

                    /**
                     * Called when user doesn't swipe all the way.
                     */
                    override fun onSwipeCancelled() {
                        actionCallback.onSwipeCancelled()

                        checkBg.visibility = View.INVISIBLE
                        checkLeft.visibility = View.INVISIBLE
                        checkRight.visibility = View.INVISIBLE

                        bgFrame.background = null
                    }

                    /**
                     * @return the subview which should move
                     */
                    override fun getSwipingView(): View {
                        return bgFrame
                    }
                }
            )
        )
    }

    fun resetView() {
        imageFetchJob?.cancel("View reset")
        imageFetchJob = null
        checkBg.visibility = View.INVISIBLE
        checkLeft.visibility = View.INVISIBLE
        checkRight.visibility = View.INVISIBLE
        bgFrame.clearAnimation()
        bgFrame.alpha = 1.0f
        bgFrame.translationX = 0.0f
        bgFrame.background = null
    }

    fun fillTitle(forceRead: Boolean = false) {
        titleTextView.visibility = View.VISIBLE
        rssItem?.let { rssItem ->
            // \u2014 is a EM-dash, basically a long version of '-'
            val temps = if (rssItem.plainSnippet.isEmpty())
                rssItem.plainTitle
            else
                rssItem.plainTitle + " \u2014 " + rssItem.plainSnippet + "\u2026"
            val textSpan = SpannableString(temps)

            textSpan.setSpan(
                TextAppearanceSpan(view.context, R.style.TextAppearance_ListItem_Body),
                rssItem.plainTitle.length, temps.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            if (rssItem.unread && !forceRead) {
                textSpan.setSpan(
                    TextAppearanceSpan(view.context, R.style.TextAppearance_ListItem_Title),
                    0, rssItem.plainTitle.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                textSpan.setSpan(
                    TextAppearanceSpan(view.context, R.style.TextAppearance_ListItem_Title_Read),
                    0, rssItem.plainTitle.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            titleTextView.text = textSpan
        }
    }

    /**
     * OnItemClickListener replacement.
     *
     *
     * If a feeditem does not have any content,
     * then it opens the link in the browser directly.
     *
     * @param view
     */
    override fun onClick(view: View) {
        val context = view.context ?: return

        try {
            val openItemWith = when (val defaultOpenItemWith = prefs.openItemsWith) {
                PREF_VAL_OPEN_WITH_READER -> {
                    if (rssItem?.plainSnippet?.isNotEmpty() == true) {
                        defaultOpenItemWith
                    } else {
                        prefs.openLinksWith
                    }
                }
                else -> defaultOpenItemWith
            }

            when (openItemWith) {
                PREF_VAL_OPEN_WITH_BROWSER, PREF_VAL_OPEN_WITH_WEBVIEW, PREF_VAL_OPEN_WITH_CUSTOM_TAB -> {
                    // Mark as read
                    rssItem?.id?.let {
                        actionCallback.coroutineScope().launch(Dispatchers.Default) {
                            feedItemsViewModel.markAsRead(it)
                        }
                    }

                    when (openItemWith) {
                        PREF_VAL_OPEN_WITH_BROWSER -> {
                            // Open in browser since no content was posted
                            rssItem?.link?.let { link ->
                                openLinkInBrowser(context, link)
                            }
                        }
                        PREF_VAL_OPEN_WITH_CUSTOM_TAB -> {
                            rssItem?.link?.let { link ->
                                openLinkInCustomTab(context, link, rssItem?.id)
                            }
                        }
                        else -> {
                            rssItem?.let {
                                view.findNavController().navigate(
                                    R.id.action_feedFragment_to_readerWebViewFragment,
                                    bundle {
                                        putString(ARG_URL, it.link)
                                        putString(ARG_ENCLOSURE, it.enclosureLink)
                                        putLong(ARG_ID, it.id)
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {
                    rssItem?.let {
                        view.findNavController().navigate(
                            R.id.action_feedFragment_to_readerFragment,
                            bundle {
                                putLong(ARG_ID, it.id)
                            }
                        )
                    }
                }
            }
        } catch (e: java.lang.IllegalArgumentException) {
            // Can happen if you press two feed items at once - then navcontroller will consider
            // one of the clicks an unknown destination - ignore it
            Log.e(TAG, "Did user click two at once?", e)
        }
    }

    /**
     * Called when item has been measured, it is now the time to insert the image.
     *
     * @return Return true to proceed with the current drawing pass, or false to cancel.
     */
    override fun onPreDraw(): Boolean {
        val context = view.context
        if (context != null) {
            rssItem?.let { rssItem ->
                imageFetchJob?.cancel("New image about to be loaded")
                imageFetchJob = feedItemsViewModel.viewModelScope.launch {
                    try {
                        val placeHolderImage = when (prefs.isNightMode) {
                            true -> R.drawable.placeholder_image_list_night_64dp
                            false -> R.drawable.placeholder_image_list_day_64dp
                        }

                        imageLoader.enqueue(
                            ImageRequest.Builder(context)
                                .placeholder(placeHolderImage)
                                .fallback(placeHolderImage)
                                .error(placeHolderImage)
                                .crossfade(true)
                                .scale(Scale.FILL)
                                .data(rssItem.imageUrl ?: "")
                                .target(imageView)
                                .build()
                        )
                    } catch (t: Throwable) {
                        Log.d(TAG, "Error when trying to fetch image", t)
                    }
                }
            }
        }

        // Remove as listener
        itemView.viewTreeObserver.removeOnPreDrawListener(this)
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (menu != null) {
            val menuInflater: MenuInflater by instance()
            menuInflater.inflate(R.menu.feeditem, menu)
            menu.findItem(R.id.open_feed).setOnMenuItemClickListener {
                rssItem?.feedId?.let {
                    findNavController(view).navigate(
                        R.id.action_feedFragment_self,
                        bundle {
                            putLong(ARG_FEED_ID, it)
                        }
                    )
                }
                true
            }
            menu.findItem(R.id.edit_feed).setOnMenuItemClickListener {
                rssItem?.let {
                    val i = Intent(view.context, EditFeedActivity::class.java)
                    i.putExtra(ARG_ID, it.feedId)
                    i.putExtra(ARG_CUSTOMTITLE, it.feedCustomTitle)
                    i.putExtra(ARG_TITLE, it.feedTitle)
                    i.putExtra(ARG_FEED_TAG, it.tag)
                    i.data = Uri.parse(it.feedUrl.toString())
                    view.context.startActivity(i)
                }
                true
            }
            menu.findItem(R.id.mark_items_above_as_read).setOnMenuItemClickListener {
                actionCallback.markAsRead(adapterPosition, false)
                true
            }
            menu.findItem(R.id.mark_items_below_as_read).setOnMenuItemClickListener {
                actionCallback.markAsRead(adapterPosition, true)
                true
            }
            menu.findItem(R.id.action_share).setOnMenuItemClickListener {
                rssItem?.link?.let { link ->
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, link)
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(view.context, shareIntent, null)
                }
                true
            }
        }
    }
}

interface ActionCallback {
    fun onDismiss(item: PreviewItem?)
    fun onSwipeStarted()
    fun onSwipeCancelled()
    fun markAsRead(position: Int, markBelow: Boolean)
    fun coroutineScope(): CoroutineScope
}
