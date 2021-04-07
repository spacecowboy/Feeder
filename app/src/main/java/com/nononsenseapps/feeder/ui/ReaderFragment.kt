package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.BidiFormatter
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.KodeinAwareFragment
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import com.nononsenseapps.feeder.model.TextOptions
import com.nononsenseapps.feeder.model.cancelNotification
import com.nononsenseapps.feeder.model.maxImageSize
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_WEBVIEW
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.bundle
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale

const val ARG_TITLE = "title"
const val ARG_CUSTOMTITLE = "customtitle"
const val ARG_LINK = "link"
const val ARG_ENCLOSURE = "enclosure"
const val ARG_IMAGEURL = "imageUrl"
const val ARG_ID = "dbid"
const val ARG_AUTHOR = "author"
const val ARG_DATE = "date"

@FlowPreview
class ReaderFragment : KodeinAwareFragment() {
    private val dateTimeFormat =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
            .withLocale(Locale.getDefault())

    private var _id: Long = ID_UNSET

    private lateinit var titleTextView: TextView
    private lateinit var bodyTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var feedTitleTextView: TextView

    private val viewModel: FeedItemViewModel by instance(arg = this)

    private val prefs: Prefs by instance()
    private val warmer: CustomTabsWarmer by instance()

    private var liveText: LiveData<Spanned>? = null

    // For menu things
    private var rssItemMenu: FeedItemWithFeed? = null

    init {
        lifecycleScope.launchWhenStarted {
            try {
                if (prefs.shouldPreloadCustomTab) {
                    val rssItem = viewModel.getItem(_id)
                    warmer.preLoad {
                        rssItem.link?.let { Uri.parse(it) }
                    }
                }
            } catch (e: Exception) {
                // Don't let this crash
                Log.e("FeederReaderFragment", "Couldn't preload $_id", e)
            }
        }
    }

    private fun getTextOptions(): TextOptions =
        TextOptions(
            itemId = _id,
            maxImageSize = requireActivity().maxImageSize(),
            nightMode = prefs.isNightMode
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fragment's views have their own coroutine scope
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            // It may have been manually overridden by fetching full text
            if (liveText == null) {
                liveText = viewModel.getLiveTextMaybeFull(
                    getTextOptions(),
                    urlClickListener()
                )
            }

            // Fragment's views have their own lifecycle
            liveText?.observe(viewLifecycleOwner) {
                bodyTextView.text = it
            }
        }
        viewModel.getLiveItem(_id).observe(viewLifecycleOwner) { rssItem ->
            if (rssItem == null) {
                return@observe
            }
            rssItemMenu = rssItem

            titleTextView.text = rssItem.plainTitle

            rssItem.feedId?.let { feedId ->
                feedTitleTextView.setOnClickListener {
                    findNavController()
                        .navigate(
                            R.id.action_readerFragment_to_feedFragment,
                            bundle {
                                putLong(ARG_FEED_ID, feedId)
                            }
                        )
                }
            }

            feedTitleTextView.text = rssItem.feedDisplayTitle

            rssItem.pubDate.let { pubDate ->
                rssItem.author.let { author ->
                    when {
                        author == null && pubDate != null ->
                            authorTextView.text = getString(
                                R.string.on_date,
                                pubDate.format(dateTimeFormat)
                            )
                        author != null && pubDate != null ->
                            authorTextView.text = getString(
                                R.string.by_author_on_date,
                                // Must wrap author in unicode marks to ensure it formats
                                // correctly in RTL
                                unicodeWrap(author),
                                pubDate.format(dateTimeFormat)
                            )
                        else -> authorTextView.visibility = View.GONE
                    }
                }
            }

            // Update state of notification toggle
            activity?.invalidateOptionsMenu()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arguments ->
            _id = arguments.getLong(ARG_ID, ID_UNSET)
        }

        if (_id > ID_UNSET) {
            val itemId = _id
            val appContext = context?.applicationContext
            appContext?.let {
                lifecycleScope.launchWhenResumed {
                    viewModel.markAsReadAndNotified(_id)
                    cancelNotification(it, itemId)
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val theLayout = if (TabletUtils.isTablet(activity)) {
            R.layout.fragment_reader_tablet
        } else {
            R.layout.fragment_reader
        }
        val rootView = inflater.inflate(theLayout, container, false)

        titleTextView = rootView.findViewById(R.id.story_title)
        bodyTextView = rootView.findViewById(R.id.story_body)
        authorTextView = rootView.findViewById(R.id.story_author)
        feedTitleTextView = rootView.findViewById(R.id.story_feedtitle)

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reader, menu)

        // Set intent
        rssItemMenu?.let { rssItem ->
            // Show/Hide buttons
            menu.findItem(R.id.action_open_enclosure).isVisible = rssItem.enclosureLink != null
            menu.findItem(R.id.action_open_in_webview).isVisible = rssItem.link != null
            menu.findItem(R.id.action_open_in_browser).isVisible = rssItem.link != null
            menu.findItem(R.id.action_fetch_article).isVisible = !rssItem.fullTextByDefault
            // Add filename to tooltip
            if (rssItem.enclosureLink != null) {
                val filename = rssItem.enclosureFilename
                if (filename != null) {
                    menu.findItem(R.id.action_open_enclosure).title = filename
                }
            }
        }

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater)
    }

    enum class ReadingState {
        NOT_STARTED, PAUSED, READING
    }
    private var readingState: ReadingState = ReadingState.NOT_STARTED
    private val textToSpeechViewModel: TextToSpeechViewModel by instance()

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_fetch_article -> {
                lifecycleScope.launch {
                    liveText?.removeObservers(this@ReaderFragment)
                    liveText = viewModel.getLiveFullText(
                        getTextOptions(),
                        urlClickListener()
                    )
                    liveText?.observe(this@ReaderFragment) {
                        bodyTextView.text = it
                    }
                }
                true
            }
            R.id.action_open_in_webview -> {
                // Open in web view or custom tab
                lifecycleScope.launch {
                    context?.let { context ->
                        val rssItem = viewModel.getItem(_id)
                        rssItem.link?.let { link ->
                            when (prefs.openLinksWith) {
                                PREF_VAL_OPEN_WITH_CUSTOM_TAB -> {
                                    openLinkInCustomTab(context, link, _id)
                                }
                                else -> {
                                    findNavController().navigate(
                                        R.id.action_readerFragment_to_readerWebViewFragment,
                                        bundle {
                                            putString(ARG_URL, link)
                                            putString(ARG_ENCLOSURE, rssItem.enclosureLink)
                                            putLong(ARG_ID, _id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                true
            }
            R.id.action_open_in_browser -> {
                lifecycleScope.launch {
                    val link = viewModel.getItem(_id).link
                    if (link != null) {
                        context?.let { context ->
                            openLinkInBrowser(context, link)
                        }
                    }
                }
                true
            }
            R.id.action_open_enclosure -> {
                lifecycleScope.launch {
                    val link = viewModel.getItem(_id).enclosureLink
                    if (link != null) {
                        context?.let { context ->
                            openLinkInBrowser(context, link)
                        }
                    }
                }

                true
            }
            R.id.action_mark_as_unread -> {
                lifecycleScope.launch {
                    viewModel.markAsRead(_id, unread = true)
                }
                true
            }
            R.id.action_read_article_aloud -> {
                when (readingState) {
                    ReadingState.NOT_STARTED -> {
                        val fullText = liveText?.value.toString()
                        textToSpeechViewModel.textToSpeechClear()
                        textToSpeechViewModel.textToSpeechAddText(fullText)
                        textToSpeechViewModel.textToSpeechStart(lifecycleScope)
                        menuItem.title = getString(R.string.pause_reading)
                        readingState = ReadingState.READING
                    }
                    ReadingState.READING -> {
                        textToSpeechViewModel.textToSpeechPause()
                        menuItem.title = getString(R.string.resume_reading)
                        readingState = ReadingState.PAUSED
                    }
                    ReadingState.PAUSED -> {
                        textToSpeechViewModel.textToSpeechStart(lifecycleScope)
                        menuItem.title = getString(R.string.pause_reading)
                        readingState = ReadingState.READING
                    }
                }
                true
            }
            R.id.action_share -> {
                lifecycleScope.launch {
                    viewModel.getItem(_id).link?.let { link ->
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, link)

                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }
}

fun Fragment.unicodeWrap(text: String): String =
    BidiFormatter.getInstance(getLocale()).unicodeWrap(text)

fun Fragment.getLocale(): Locale? =
    context?.getLocale()

fun Context.unicodeWrap(text: String): String =
    BidiFormatter.getInstance(getLocale()).unicodeWrap(text)

fun Context.getLocale(): Locale =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }

fun Fragment.urlClickListener(): (link: String) -> Unit = { link ->
    context?.let { context ->
        val kodein: Kodein by closestKodein()
        val prefs: Prefs by kodein.instance()

        when (prefs.openLinksWith) {
            PREF_VAL_OPEN_WITH_CUSTOM_TAB -> {
                openLinkInCustomTab(context, link, null)
            }
            PREF_VAL_OPEN_WITH_WEBVIEW -> {
                findNavController().navigate(
                    R.id.action_readerFragment_to_readerWebViewFragment,
                    bundle {
                        putString(ARG_URL, link)
                    }
                )
            }
            else -> {
                openLinkInBrowser(context, link)
            }
        }
    }
}
