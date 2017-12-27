package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.app.LoaderManager
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FilterQueryProvider
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.COL_CUSTOM_TITLE
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.db.COL_URL
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.URI_TAGSWITHCOUNTS
import com.nononsenseapps.feeder.db.Util
import com.nononsenseapps.feeder.util.Optional
import com.nononsenseapps.feeder.util.contentValues
import com.nononsenseapps.feeder.util.feedParser
import com.nononsenseapps.feeder.util.insertFeedWith
import com.nononsenseapps.feeder.util.notifyAllUris
import com.nononsenseapps.feeder.util.requestFeedSync
import com.nononsenseapps.feeder.util.setString
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.feeder.util.updateFeedWith
import com.nononsenseapps.feeder.views.FloatLabelLayout
import com.nononsenseapps.jsonfeed.Feed
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.URL
import java.util.*

const val SHOULD_FINISH_BACK = "SHOULD_FINISH_BACK"
const val _ID = "_id"
const val CUSTOM_TITLE = "custom_title"
const val FEED_TITLE = "feed_title"
const val TEMPLATE = "template"
private const val LOADER_TAG_SUGGESTIONS = 1
private const val TAGSFILTER = "TAGSFILTER"


class EditFeedActivity : Activity() {
    private val TAG = "tag"

    private var shouldFinishBack = false
    private var id: Long = -1
    // Views and shit
    lateinit private var textTitle: EditText
    lateinit private var textUrl: EditText
    lateinit private var textTag: AutoCompleteTextView
    lateinit private var textSearch: EditText
    lateinit private var detailsFrame: View
    lateinit private var listResults: RecyclerView
    lateinit private var resultAdapter: ResultsAdapter
    lateinit private var searchFrame: View
    private var feedUrl: String? = null
    lateinit private var emptyText: TextView
    lateinit private var loadingProgress: View
    lateinit private var urlLabel: FloatLabelLayout
    lateinit private var titleLabel: FloatLabelLayout
    lateinit private var tagLabel: FloatLabelLayout

    private var feedTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_feed)

        // Setup views
        textTitle = findViewById(R.id.feed_title)
        titleLabel = textTitle.parent as FloatLabelLayout
        textUrl = findViewById(R.id.feed_url)
        urlLabel = textUrl.parent as FloatLabelLayout
        textTag = findViewById(R.id.feed_tag)
        tagLabel = textTag.parent as FloatLabelLayout
        detailsFrame = findViewById(R.id.feed_details_frame)
        searchFrame = findViewById(R.id.feed_search_frame)
        textSearch = findViewById(R.id.search_view)
        listResults = findViewById(R.id.results_listview)
        emptyText = findViewById(android.R.id.empty)
        loadingProgress = findViewById(R.id.loading_progress)
        resultAdapter = ResultsAdapter()
        //listResults.emptyView = emptyText
        listResults.setHasFixedSize(true)
        listResults.layoutManager = LinearLayoutManager(this)
        listResults.adapter = resultAdapter

        textSearch.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                // Hide keyboard
                val f = currentFocus
                if (f != null) {
                    val imm = getSystemService(
                            Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(f.windowToken,
                            0)
                }

                // Issue search
                val url: URL = sloppyLinkToStrictURL(textSearch.text.toString().trim())

                listResults.visibility = View.GONE
                emptyText.visibility = View.GONE
                loadingProgress.visibility = View.VISIBLE

                Observable.just(url)
                        .subscribeOn(Schedulers.io())
                        .flatMap {
                            try {
                                val alts = feedParser.getAlternateFeedLinksAtUrl(it)
                                if (alts.isNotEmpty()) {
                                    Observable.fromIterable(alts.map { sloppyLinkToStrictURL(it.first) to it.second })
                                } else {
                                    throw IllegalArgumentException("No alternate feeds found")
                                }
                            } catch (t: Throwable) {
                                Log.d("Finding alternates: ", t.localizedMessage)
                                Observable.just(it to "RSS")
                            }
                        }
                        .flatMap {
                            Observable.just(it).subscribeOn(Schedulers.io())
                                    .map {
                                        try {
                                            Log.d("JONAS", "Fetching feed on ${Thread.currentThread().name}")
                                            // TODO FIX
                                            Optional.of(feedParser.parseFeedUrl(it.first) to it.second)
                                        } catch (t: Throwable) {
                                            Log.d("Parsing feed:", t.localizedMessage)
                                            Optional.empty<Pair<Feed, String>>()
                                        }
                                    }
                        }
                        .filter {
                            it.isPresent
                        }
                        .map {
                            it.get()
                        }
                        .reduce(ArrayList<Pair<Feed, String>>(), { acc, next ->
                            acc.add(next)
                            acc
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { result ->
                            Log.d("JONAS", "UPdating UI...")
                            loadingProgress.visibility = View.GONE
                            if (result.isNotEmpty()) {
                                resultAdapter.data = result.map { it.first }
                                detailsFrame.visibility = View.GONE
                                searchFrame.visibility = View.VISIBLE
                                listResults.visibility = View.VISIBLE
                            } else {
                                emptyText.text = getString(R.string.no_such_feed)
                                emptyText.visibility = View.VISIBLE
                            }
                        }

                return@OnEditorActionListener true
            }
            false
        })

        val addButton = findViewById<Button>(R.id.add_button) as Button
        addButton
                .setOnClickListener {
                    // TODO error checking and stuff like that
                    val title = textTitle.text.toString().trim()
                    val customTitle = if (title == feedTitle) {
                        ""
                    } else {
                        title
                    }
                    val values = contentValues {
                        setString(COL_TITLE to feedTitle)
                        setString(COL_CUSTOM_TITLE to customTitle)
                        setString(COL_TAG to textTag.text.toString().trim())
                        setString(COL_URL to textUrl.text.toString().trim())
                    }

                    if (id < 1) {
                        id = contentResolver.insertFeedWith(values)
                    } else {
                        contentResolver.updateFeedWith(id, values)
                    }
                    contentResolver.notifyAllUris()
                    contentResolver.requestFeedSync(id)

                    val intent = Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$id"))
                    intent.putExtra(ARG_FEED_TITLE, title)
                            .putExtra(ARG_FEED_URL, values.getAsString(COL_URL))
                            .putExtra(ARG_FEED_TAG, values.getAsString(COL_TAG))

                    setResult(RESULT_OK, intent)
                    finish()
                    if (shouldFinishBack) {
                        // Only care about exit transition
                        overridePendingTransition(R.anim.to_bottom_right,
                                R.anim.to_bottom_right)
                    }
                }

        // Consider start intent
        val i = intent
        if (i != null) {
            shouldFinishBack = i.getBooleanExtra(SHOULD_FINISH_BACK, false)
            // Existing id
            id = i.getLongExtra(_ID, -1)
            // Edit like existing, but it's really new
            val template = i.getBooleanExtra(TEMPLATE, false)

            // Existing item
            if (id > 0 || template) {
                searchFrame.visibility = View.GONE
                detailsFrame.visibility = View.VISIBLE
                if (id > 0) {
                    // Don't allow editing url, but allow copying the text
                    //textUrl.setInputType(InputType.TYPE_NULL);
                    //textUrl.setTextIsSelectable(true);
                    // Focus on tag
                    textTag.requestFocus()
                    addButton.text = getString(R.string.save)
                } else {
                    textUrl.requestFocus()
                }
            } else {
                searchFrame.visibility = View.VISIBLE
                detailsFrame.visibility = View.GONE
                // Focus on search
                searchFrame.requestFocus()
            }

            // Link
            if (i.dataString != null) {
                feedUrl = i.dataString.trim()
                textSearch.setText(feedUrl)
            } else if (i.hasExtra(Intent.EXTRA_TEXT)) {
                feedUrl = i.getStringExtra(Intent.EXTRA_TEXT).trim()
                textSearch.setText(feedUrl)
            }
            // URL
            textUrl.setText(feedUrl)
            // Title
            if (i.hasExtra(FEED_TITLE)) {
                feedTitle = i.getStringExtra(FEED_TITLE)
            }
            if (i.hasExtra(CUSTOM_TITLE) && !i.getStringExtra(CUSTOM_TITLE).isEmpty()) {
                textTitle.setText(i.getStringExtra(CUSTOM_TITLE))
            } else {
                textTitle.setText(feedTitle)
            }

            // Tag
            if (i.hasExtra(TAG)) {
                // Use append instead of setText to make sure cursor is at end
                textTag.append(i.getStringExtra(TAG))
            }
        }

        // Create an adapter
        val tagsAdapter = SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null, Util.ToStringArray(COL_TAG),
                Util.ToIntArray(android.R.id.text1), 0)

        // Create a loader manager
        val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(id: Int,
                                        args: Bundle?): Loader<Cursor> {
                var filter: String? = null
                if (args != null && args.containsKey(TAGSFILTER)) {
                    filter = COL_TAG + " LIKE '" + args
                            .getCharSequence(TAGSFILTER, "") + "%'"
                }
                return CursorLoader(this@EditFeedActivity,
                        URI_TAGSWITHCOUNTS,
                        Util.ToStringArray(COL_ID,
                                COL_TAG), filter, null,
                        Util.SortAlphabeticNoCase(COL_TAG))
            }

            override fun onLoadFinished(loader: Loader<Cursor>,
                                        data: Cursor) {
                tagsAdapter.swapCursor(data)
            }

            override fun onLoaderReset(loader: Loader<Cursor>) {
                tagsAdapter.swapCursor(null)
            }
        }

        // Tell adapter how to return result
        tagsAdapter.cursorToStringConverter = SimpleCursorAdapter.CursorToStringConverter { cursor ->
            if (cursor == null) {
                return@CursorToStringConverter null
            }

            cursor.getString(1)
        }

        // Tell adapter how to filter
        tagsAdapter.filterQueryProvider = FilterQueryProvider { constraint ->
            // Restart loader with filter
            val filter = Bundle()
            filter.putCharSequence(TAGSFILTER, constraint)
            loaderManager.restartLoader(LOADER_TAG_SUGGESTIONS,
                    filter, loaderCallbacks)
            // Return null since existing cursor is going to be closed
            null
        }

        // Set the adapter
        textTag.setAdapter(tagsAdapter)

        // Start suggestions loader
        val args = Bundle()
        args.putCharSequence(TAGSFILTER, textTag.text)
        loaderManager.restartLoader(LOADER_TAG_SUGGESTIONS,
                args, loaderCallbacks)
    }

    private fun shouldBeFloatingWindow(): Boolean {
        val theme = theme
        val floatingWindowFlag = TypedValue()
        if (theme == null || !theme.resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag,
                true)) {
            // isFloatingWindow flag is not defined in theme
            return false
        }
        return floatingWindowFlag.data != 0
    }

    private fun setupFloatingWindow() {
        // configure this Activity as a floating window, dimming the background
        val params = window.attributes
        params.width = resources
                .getDimensionPixelSize(R.dimen.session_details_floating_width)
        params.height = resources
                .getDimensionPixelSize(R.dimen.session_details_floating_height)
        params.alpha = 1f
        params.dimAmount = 0.7f
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = params
    }

    private fun useEntry(title: String, url: String) {
        feedTitle = android.text.Html.fromHtml(title).toString()
        feedUrl = url.trim()
        textUrl.setText(feedUrl)
        textTitle.setText(feedTitle)
        detailsFrame.visibility = View.VISIBLE
        searchFrame.visibility = View.GONE
        // Focus on tag
        textTag.requestFocus()
        val imm = getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(textTag, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (shouldFinishBack) {
            // Only care about exit transition
            overridePendingTransition(0, R.anim.to_bottom_right)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean =
            true

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == android.R.id.home && shouldFinishBack) {
            // Was launched from inside app, should just go back
            // Action bar handles other cases.
            finish()
            // Only care about exit transition
            overridePendingTransition(R.anim.to_bottom_right,
                    R.anim.to_bottom_right)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class FeedResult(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        var textTitle: TextView = view.findViewById(R.id.feed_title)
        var textUrl: TextView = view.findViewById(R.id.feed_url)
        var textDescription: TextView = view.findViewById(R.id.feed_description)
        var item: Feed? = null

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (item != null) {
                useEntry(item!!.title ?: "untitled", item!!.feed_url ?: feedUrl ?: "")
            }
        }
    }

    private inner class ResultsAdapter : RecyclerView.Adapter<FeedResult>() {

        private var items: List<Feed> = emptyList()
        var data: List<Feed>
            get() = items
            set(value) {
                items = value
                notifyDataSetChanged()
            }

        override fun onBindViewHolder(holder: FeedResult, position: Int) {
            val item = items[position]

            holder.item = item
            holder.textTitle.text = item.title ?: ""
            holder.textDescription.text = item.description ?: ""
            holder.textUrl.text = item.feed_url ?: ""
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedResult =
                FeedResult(LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_feed_result, parent, false))

        override fun getItemCount(): Int = items.size

    }

}
