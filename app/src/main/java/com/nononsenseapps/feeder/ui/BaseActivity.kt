package com.nononsenseapps.feeder.ui

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.FeedAsyncTaskLoader
import com.nononsenseapps.feeder.util.LPreviewUtils
import com.nononsenseapps.feeder.util.LPreviewUtilsBase
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.setupSync
import com.nononsenseapps.feeder.views.ObservableScrollView
import java.util.*

const val SHOULD_FINISH_BACK = "SHOULD_FINISH_BACK"

/**
 * Base activity which handles navigation drawer and other bloat common
 * between activities.
 */
@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<List<FeedWrapper>> {
    protected var mActionBarShown = true
    // If pressing home should finish or start new activity
    protected var mShouldFinishBack = false
    protected var mActionBarToolbar: Toolbar? = null
    //protected MultiScrollListener mMultiScrollListener;
    private var mStatusBarColorAnimator: ObjectAnimator? = null
    // When set, these components will be shown/hidden in sync with the action bar
    // to implement the "quick recall" effect (the Action Bar and the header views disappear
    // when you scroll down a list, and reappear quickly when you scroll up).
    private val mHideableHeaderViews = ArrayList<View>()
    private val mHideableFooterViews = ArrayList<View>()
    // variables that control the Action Bar auto hide behavior (aka "quick recall")
    private var mActionBarAutoHideEnabled = false
    private var mActionBarAutoHideSensivity = 0
    private var mActionBarAutoHideMinY = 0
    private var mActionBarAutoHideSignal = 0
    private var mThemedStatusBarColor: Int = 0
    private lateinit var mLPreviewUtils: LPreviewUtilsBase
    var drawerLayout: DrawerLayout? = null
        private set
    private var mDrawerToggle: LPreviewUtilsBase.ActionBarDrawerToggleWrapper? = null
    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private var mDeferredOnDrawerClosedRunnable: Runnable? = null
    internal var navAdapter: FeedsAdapter? = null
        private set

    protected val isNavDrawerOpen: Boolean
        get() = drawerLayout != null && drawerLayout!!.isDrawerOpen(GravityCompat.START)


    /**
     * Set the background depending on user preferences
     */
    protected fun setNightBackground() {
        // Change background
        val typedValue = TypedValue()
        if (PrefUtils.isNightMode(this)) {
            // Get black
            theme.resolveAttribute(R.attr.nightBGColor, typedValue, true)
        } else {
            theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        }
        window.setBackgroundDrawable(ColorDrawable(typedValue.data))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Write default setting if method has never been called before
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)

        val i = intent
        if (i != null) {
            mShouldFinishBack = i.getBooleanExtra(SHOULD_FINISH_BACK, false)
        }

        mLPreviewUtils = LPreviewUtils.getInstance(this)
        mThemedStatusBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.primary_dark, null)
        } else {
            @Suppress("DEPRECATION")
            resources.getColor(R.color.primary_dark)
        }

        setNightBackground()
        // Add account and enable sync - if not done before
        this.setupSync()
    }

    public override fun onResume() {
        super.onResume()
    }

    protected fun initializeActionBar(): Toolbar? {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = findViewById(R.id.toolbar_actionbar)
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar)
            }
        }
        return mActionBarToolbar
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupNavDrawer()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (mDrawerToggle != null && mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }

        when (id) {
            android.R.id.home -> {
                if (mShouldFinishBack) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition()
                    } else {
                        finish()
                    }
                } else {
                    val intent = Intent(this, FeedActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupNavDrawer() {
        // Show icon
        val ab = supportActionBar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true)
            ab.setHomeButtonEnabled(true)
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout == null) {
            return
        }

        mDrawerToggle = mLPreviewUtils.setupDrawerToggle(drawerLayout,
                object : DrawerLayout.DrawerListener {
                    override fun onDrawerClosed(drawerView: View) {
                        // run deferred action, if we have one
                        if (mDeferredOnDrawerClosedRunnable != null) {
                            mDeferredOnDrawerClosedRunnable!!.run()
                            mDeferredOnDrawerClosedRunnable = null
                        }
                        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
                        updateStatusBarForNavDrawerSlide(0f)
                        onNavDrawerStateChanged(false)
                    }

                    override fun onDrawerOpened(drawerView: View) {
                        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
                        updateStatusBarForNavDrawerSlide(1f)
                        onNavDrawerStateChanged(true)
                    }

                    override fun onDrawerStateChanged(newState: Int) {
                        invalidateOptionsMenu()
                        onNavDrawerStateChanged(isNavDrawerOpen
                        )
                    }

                    override fun onDrawerSlide(drawerView: View,
                                               slideOffset: Float) {
                        updateStatusBarForNavDrawerSlide(slideOffset)
                        onNavDrawerSlide(slideOffset)
                    }
                })

        mDrawerToggle!!.syncState()

        // Recycler view stuff
        val mRecyclerView = drawerLayout!!.findViewById<RecyclerView>(R.id.navdrawer_list)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        navAdapter = FeedsAdapter(this)
        mRecyclerView.adapter = navAdapter

        populateNavDrawer()

        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!PrefUtils.isWelcomeDone(this)) {
            // first run of the app starts with the nav drawer open
            PrefUtils.markWelcomeDone(this)
            drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    /**
     * Open the nav drawer
     */
    fun openNavDrawer() {
        drawerLayout!!.openDrawer(GravityCompat.START)
    }

    private fun updateStatusBarForNavDrawerSlide(slideOffset: Float) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator!!.cancel()
        }

        if (!mActionBarShown) {
            mLPreviewUtils.statusBarColor = Color.BLACK
            return
        }


        mLPreviewUtils.statusBarColor = ARGB_EVALUATOR
                .evaluate(slideOffset, mThemedStatusBarColor, Color.BLACK) as Int
    }

    // Subclasses can override this for custom behavior
    protected fun onNavDrawerStateChanged(isOpen: Boolean) {
        if (mActionBarAutoHideEnabled && isOpen) {
            autoShowOrHideActionBar(true)
        }
    }

    protected open fun onNavDrawerSlide(offset: Float) {}

    // Subclasses can override to decide what happens on nav item selection
    open fun onNavigationDrawerItemSelected(id: Long, title: String?,
                                                      url: String?, tag: String?) {
        // TODO add default start activity with arguments
    }

    private fun populateNavDrawer() {
        supportLoaderManager.restartLoader(NAV_TAGS_LOADER, Bundle(), this)
    }

    fun showActionBar() {
        autoShowOrHideActionBar(true)
    }

    protected fun autoShowOrHideActionBar(show: Boolean) {
        if (show == mActionBarShown) {
            return
        }

        mActionBarShown = show
        onActionBarAutoShowOrHide(show)
    }

    protected open fun onActionBarAutoShowOrHide(shown: Boolean) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator!!.cancel()
        }
        mStatusBarColorAnimator = ObjectAnimator
                .ofInt(mLPreviewUtils, "statusBarColor",
                        if (shown) mThemedStatusBarColor else Color.BLACK)
                .setDuration(250)
        mStatusBarColorAnimator!!.setEvaluator(ARGB_EVALUATOR)
        mStatusBarColorAnimator!!.start()

        for (view in mHideableHeaderViews) {
            if (shown) {
                view.animate().translationY(0f).alpha(1f)
                        .setDuration(HEADER_HIDE_ANIM_DURATION.toLong()).interpolator = DecelerateInterpolator()
            } else {
                view.animate().translationY((-view.bottom).toFloat()).alpha(0f)
                        .setDuration(HEADER_HIDE_ANIM_DURATION.toLong()).interpolator = DecelerateInterpolator()
            }
        }
        for (view in mHideableFooterViews) {
            if (shown) {
                view.animate().translationY(0f).alpha(1f)
                        .setDuration(HEADER_HIDE_ANIM_DURATION.toLong()).interpolator = DecelerateInterpolator()
            } else {
                view.animate().translationY(view.height.toFloat()).alpha(0f)
                        .setDuration(HEADER_HIDE_ANIM_DURATION.toLong()).interpolator = DecelerateInterpolator()
            }
        }
    }

    fun enableActionBarAutoHide(listView: RecyclerView) {
        initActionBarAutoHide()
        val layoutManager = listView.layoutManager as LinearLayoutManager
        mActionBarAutoHideSignal = 0
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            internal val ITEMS_THRESHOLD = 0
            internal var lastFvi = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                var force = false
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                if (recyclerView!!.adapter != null) {
                    val lastPos = recyclerView.adapter.itemCount - 1
                    if (layoutManager.findLastVisibleItemPosition() == lastPos) {
                        // Show when last item is visible
                        force = true
                    }
                }
                onMainContentScrolled(if (firstVisibleItem <= ITEMS_THRESHOLD)
                    0
                else
                    Integer.MAX_VALUE,
                        if (lastFvi - firstVisibleItem > 0)
                            Integer.MIN_VALUE
                        else if (lastFvi == firstVisibleItem) 0 else Integer.MAX_VALUE,
                        force)
                lastFvi = firstVisibleItem
            }
        })
    }

    /**
     * Initializes the Action Bar auto-hide (aka Quick Recall) effect.
     */
    private fun initActionBarAutoHide() {
        mActionBarAutoHideEnabled = true
        mActionBarAutoHideMinY = resources
                .getDimensionPixelSize(R.dimen.action_bar_auto_hide_min_y)
        mActionBarAutoHideSensivity = resources
                .getDimensionPixelSize(R.dimen.action_bar_auto_hide_sensivity)
    }

    /**
     * Indicates that the main content has scrolled (for the purposes of
     * showing/hiding
     * the action bar for the "action bar auto hide" effect). currentY and
     * deltaY may be exact
     * (if the underlying view supports it) or may be approximate indications:
     * deltaY may be INT_MAX to mean "scrolled forward indeterminately" and
     * INT_MIN to mean
     * "scrolled backward indeterminately".  currentY may be 0 to mean
     * "somewhere close to the
     * start of the list" and INT_MAX to mean "we don't know, but not at the
     * start of the list"
     */
    private fun onMainContentScrolled(currentY: Int, delta: Int, force: Boolean) {
        var deltaY = delta
        if (deltaY > mActionBarAutoHideSensivity) {
            deltaY = mActionBarAutoHideSensivity
        } else if (deltaY < -mActionBarAutoHideSensivity) {
            deltaY = -mActionBarAutoHideSensivity
        }

        if (Math.signum(deltaY.toFloat()) * Math.signum(mActionBarAutoHideSignal.toFloat()) < 0) {
            // deltaY is a motion opposite to the accumulated signal, so reset signal
            mActionBarAutoHideSignal = deltaY
        } else {
            // add to accumulated signal
            mActionBarAutoHideSignal += deltaY
        }

        val shouldShow = currentY < mActionBarAutoHideMinY || mActionBarAutoHideSignal <= -mActionBarAutoHideSensivity
        autoShowOrHideActionBar(shouldShow or force)
    }

    fun enableActionBarAutoHide(
            scrollView: ObservableScrollView) {
        initActionBarAutoHide()
        mActionBarAutoHideSignal = 0
        scrollView.addOnScrollChangedListener { _, deltaY -> onMainContentScrolled(scrollView.scrollY, deltaY, false) }
    }

    protected fun registerHideableHeaderView(hideableHeaderView: View) {
        if (!mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.add(hideableHeaderView)
        }
    }

    protected fun deregisterHideableHeaderView(hideableHeaderView: View) {
        if (mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.remove(hideableHeaderView)
        }
    }

    protected fun registerHideableFooterView(hideableFooterView: View) {
        if (!mHideableFooterViews.contains(hideableFooterView)) {
            mHideableFooterViews.add(hideableFooterView)
        }
    }

    protected fun deregisterHideableFooterView(hideableFooterView: View) {
        if (mHideableFooterViews.contains(hideableFooterView)) {
            mHideableFooterViews.remove(hideableFooterView)
        }
    }

    override fun onCreateLoader(id: Int, bundle: Bundle): Loader<List<FeedWrapper>> {
        val loader = FeedAsyncTaskLoader(this)
        loader.setUpdateThrottle(200)
        return loader
    }

    override fun onLoadFinished(Loader: Loader<List<FeedWrapper>>,
                                items: List<FeedWrapper>?) {
        navAdapter?.updateData(items ?: emptyList())
    }

    override fun onLoaderReset(loader: Loader<List<FeedWrapper>>) {
        // ..
    }

    companion object {


        // Durations for certain animations we use:
        val HEADER_HIDE_ANIM_DURATION = 300
        // Special Navdrawer items
        protected val NAVDRAWER_ITEM_INVALID = -1
        private val ARGB_EVALUATOR = ArgbEvaluator()
        // Positive numbers reserved for children
        private val NAV_TAGS_LOADER = -2

        /**
         * Converts an intent into a [Bundle] suitable for use as fragment
         * arguments.
         */
        fun intentToFragmentArguments(intent: Intent?): Bundle {
            val arguments = Bundle()
            if (intent == null) {
                return arguments
            }

            val data = intent.data
            if (data != null) {
                arguments.putParcelable("_uri", data)
            }

            val extras = intent.extras
            if (extras != null) {
                arguments.putAll(intent.extras)
            }

            return arguments
        }
    }

}
