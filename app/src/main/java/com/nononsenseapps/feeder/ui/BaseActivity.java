/*
 * Copyright (c) 2015 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.model.RssNotificationsKt;
import com.nononsenseapps.feeder.util.ContextExtensionsKt;
import com.nononsenseapps.feeder.util.FeedAsyncTaskLoader;
import com.nononsenseapps.feeder.util.LPreviewUtils;
import com.nononsenseapps.feeder.util.LPreviewUtilsBase;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.views.ObservableScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Base activity which handles navigation drawer and other bloat common
 * between activities.
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks {

    public static final String SHOULD_FINISH_BACK = "SHOULD_FINISH_BACK";
    // Durations for certain animations we use:
    public static final int HEADER_HIDE_ANIM_DURATION = 300;
    // Special Navdrawer items
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    // delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    private static final TypeEvaluator ARGB_EVALUATOR = new ArgbEvaluator();
    // Positive numbers reserved for children
    private static final int NAV_TAGS_LOADER = -2;
    protected boolean mActionBarShown = true;
    // If pressing home should finish or start new activity
    protected boolean mShouldFinishBack = false;
    protected Toolbar mActionBarToolbar;
    //protected MultiScrollListener mMultiScrollListener;
    private ObjectAnimator mStatusBarColorAnimator;
    // When set, these components will be shown/hidden in sync with the action bar
    // to implement the "quick recall" effect (the Action Bar and the header views disappear
    // when you scroll down a list, and reappear quickly when you scroll up).
    private ArrayList<View> mHideableHeaderViews = new ArrayList<View>();
    private ArrayList<View> mHideableFooterViews = new ArrayList<View>();
    // variables that control the Action Bar auto hide behavior (aka "quick recall")
    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private int mThemedStatusBarColor;
    private LPreviewUtilsBase mLPreviewUtils;
    private DrawerLayout mDrawerLayout;
    private LPreviewUtilsBase.ActionBarDrawerToggleWrapper mDrawerToggle;
    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;
    private FeedsAdapter mNavAdapter;

    /**
     * Converts an intent into a {@link Bundle} suitable for use as fragment
     * arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        final Uri data = arguments.getParcelable("_uri");
        if (data != null) {
            intent.setData(data);
        }

        intent.putExtras(arguments);
        intent.removeExtra("_uri");
        return intent;
    }


    @Nullable
    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Nullable
    public FeedsAdapter getNavAdapter() {
        return mNavAdapter;
    }


    /**
     * Set the background depending on user preferences
     */
    protected void setNightBackground() {
        // Change background
        TypedValue typedValue = new TypedValue();
        if (PrefUtils.INSTANCE.isNightMode(this)) {
            // Get black
            getTheme().resolveAttribute(R.attr.nightBGColor, typedValue, true);
        } else {
            getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(typedValue.data));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Write default setting if method has never been called before
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        Intent i = getIntent();
        if (i != null) {
            mShouldFinishBack = i.getBooleanExtra(SHOULD_FINISH_BACK, false);
        }

        mLPreviewUtils = LPreviewUtils.getInstance(this);
        mThemedStatusBarColor = getResources().getColor(R.color.primary_dark);

        setNightBackground();
        // Add account and enable sync - if not done before
        ContextExtensionsKt.setupSync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Send notifications for configured feeds
        RssNotificationsKt.notify(this);
    }

    protected Toolbar initializeActionBar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();

//        View mainContent = findViewById(R.id.main_content);
//        if (mainContent != null) {
//            mainContent.setAlpha(0);
//            mainContent.animate().alpha(1)
//                    .setDuration(MAIN_CONTENT_FADEIN_DURATION);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mDrawerToggle != null &&
                mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (id) {
            case android.R.id.home:
                if (mShouldFinishBack) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition();
                    } else {
                        finish();
                    }
                } else {
                    Intent intent = new Intent(this, FeedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupNavDrawer() {
        // Show icon
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
        // What nav drawer item should be selected?
        int selfItem = getSelfNavDrawerItem();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }

        if (selfItem == NAVDRAWER_ITEM_INVALID) {
            // do not show a nav drawer
            View navDrawer = mDrawerLayout.findViewById(R.id.navdrawer);
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }

        mDrawerToggle = mLPreviewUtils.setupDrawerToggle(mDrawerLayout,
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // run deferred action, if we have one
                        if (mDeferredOnDrawerClosedRunnable != null) {
                            mDeferredOnDrawerClosedRunnable.run();
                            mDeferredOnDrawerClosedRunnable = null;
                        }
                        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                        updateStatusBarForNavDrawerSlide(0f);
                        onNavDrawerStateChanged(false, false);
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                        updateStatusBarForNavDrawerSlide(1f);
                        onNavDrawerStateChanged(true, false);
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        invalidateOptionsMenu();
                        onNavDrawerStateChanged(isNavDrawerOpen(),
                                newState != DrawerLayout.STATE_IDLE);
                    }

                    @Override
                    public void onDrawerSlide(View drawerView,
                                              float slideOffset) {
                        updateStatusBarForNavDrawerSlide(slideOffset);
                        onNavDrawerSlide(slideOffset);
                    }
                });

        mDrawerToggle.syncState();

        // Recycler view stuff
        RecyclerView mRecyclerView = (RecyclerView) mDrawerLayout.findViewById(R.id.navdrawer_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mNavAdapter = new FeedsAdapter(this);
        mRecyclerView.setAdapter(mNavAdapter);

        populateNavDrawer();

        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!PrefUtils.INSTANCE.isWelcomeDone(this)) {
            // first run of the app starts with the nav drawer open
            PrefUtils.INSTANCE.markWelcomeDone(this);
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * Open the nav drawer
     */
    public void openNavDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity.
     * Subclasses
     * of BaseActivity override this to indicate what nav drawer item
     * corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have
     * a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    private void updateStatusBarForNavDrawerSlide(float slideOffset) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator.cancel();
        }

        if (!mActionBarShown) {
            mLPreviewUtils.setStatusBarColor(Color.BLACK);
            return;
        }

        mLPreviewUtils.setStatusBarColor((Integer) ARGB_EVALUATOR
                .evaluate(slideOffset, mThemedStatusBarColor, Color.BLACK));
    }

    // Subclasses can override this for custom behavior
    protected void onNavDrawerStateChanged(boolean isOpen,
                                           boolean isAnimating) {
        if (mActionBarAutoHideEnabled && isOpen) {
            autoShowOrHideActionBar(true);
        }
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null &&
                mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void onNavDrawerSlide(float offset) {
    }

    // Subclasses can override to decide what happens on nav item selection
    protected void onNavigationDrawerItemSelected(long id, String title,
                                                  String url, String tag) {
        // TODO add default start activity with arguments
    }

    private void populateNavDrawer() {
        getSupportLoaderManager().restartLoader(NAV_TAGS_LOADER, new Bundle(), this);
    }

    public void showActionBar() {
        autoShowOrHideActionBar(true);
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown) {
            return;
        }

        mActionBarShown = show;
        onActionBarAutoShowOrHide(show);
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator.cancel();
        }
        mStatusBarColorAnimator = ObjectAnimator
                .ofInt(mLPreviewUtils, "statusBarColor",
                        shown ? mThemedStatusBarColor : Color.BLACK)
                .setDuration(250);
        mStatusBarColorAnimator.setEvaluator(ARGB_EVALUATOR);
        mStatusBarColorAnimator.start();

        for (View view : mHideableHeaderViews) {
            if (shown) {
                view.animate().translationY(0).alpha(1)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            } else {
                view.animate().translationY(-view.getBottom()).alpha(0)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            }
        }
        for (View view : mHideableFooterViews) {
            if (shown) {
                view.animate().translationY(0).alpha(1)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            } else {
                view.animate().translationY(view.getHeight()).alpha(0)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            }
        }
    }

    protected void enableActionBarAutoHide(final RecyclerView listView) {
        initActionBarAutoHide();
        final LinearLayoutManager layoutManager =
                (LinearLayoutManager) listView.getLayoutManager();
        mActionBarAutoHideSignal = 0;
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final static int ITEMS_THRESHOLD = 0;
            int lastFvi = 0;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean force = false;
                int firstVisibleItem =
                        layoutManager.findFirstVisibleItemPosition();
                if (recyclerView.getAdapter() != null) {
                    int lastPos = recyclerView.getAdapter().getItemCount() - 1;
                    if (layoutManager.findLastVisibleItemPosition() == lastPos) {
                        // Show when last item is visible
                        force = true;
                    }
                }
                onMainContentScrolled(firstVisibleItem <= ITEMS_THRESHOLD ?
                                0 :
                                Integer.MAX_VALUE,
                        lastFvi - firstVisibleItem > 0 ?
                                Integer.MIN_VALUE :
                                lastFvi == firstVisibleItem ? 0 : Integer.MAX_VALUE,
                        force);
                lastFvi = firstVisibleItem;
            }
        });
    }

    /**
     * Initializes the Action Bar auto-hide (aka Quick Recall) effect.
     */
    private void initActionBarAutoHide() {
        mActionBarAutoHideEnabled = true;
        mActionBarAutoHideMinY = getResources()
                .getDimensionPixelSize(R.dimen.action_bar_auto_hide_min_y);
        mActionBarAutoHideSensivity = getResources()
                .getDimensionPixelSize(R.dimen.action_bar_auto_hide_sensivity);
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
    private void onMainContentScrolled(int currentY, int deltaY, boolean force) {
        if (deltaY > mActionBarAutoHideSensivity) {
            deltaY = mActionBarAutoHideSensivity;
        } else if (deltaY < -mActionBarAutoHideSensivity) {
            deltaY = -mActionBarAutoHideSensivity;
        }

        if (Math.signum(deltaY) * Math.signum(mActionBarAutoHideSignal) < 0) {
            // deltaY is a motion opposite to the accumulated signal, so reset signal
            mActionBarAutoHideSignal = deltaY;
        } else {
            // add to accumulated signal
            mActionBarAutoHideSignal += deltaY;
        }

        boolean shouldShow = currentY < mActionBarAutoHideMinY ||
                (mActionBarAutoHideSignal <=
                        -mActionBarAutoHideSensivity);
        autoShowOrHideActionBar(shouldShow | force);
    }

    protected void enableActionBarAutoHide(
            final ObservableScrollView scrollView) {
        initActionBarAutoHide();
        mActionBarAutoHideSignal = 0;
        scrollView.addOnScrollChangedListener(
                new ObservableScrollView.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged(final int deltaX,
                                                final int deltaY) {
                        onMainContentScrolled(scrollView.getScrollY(), deltaY, false);
                    }
                });
    }

    protected void registerHideableHeaderView(View hideableHeaderView) {
        if (!mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.add(hideableHeaderView);
        }
    }

    protected void deregisterHideableHeaderView(View hideableHeaderView) {
        if (mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.remove(hideableHeaderView);
        }
    }

    protected void registerHideableFooterView(View hideableFooterView) {
        if (!mHideableFooterViews.contains(hideableFooterView)) {
            mHideableFooterViews.add(hideableFooterView);
        }
    }

    protected void deregisterHideableFooterView(View hideableFooterView) {
        if (mHideableFooterViews.contains(hideableFooterView)) {
            mHideableFooterViews.remove(hideableFooterView);
        }
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle bundle) {
        FeedAsyncTaskLoader loader = new FeedAsyncTaskLoader(this);
        loader.setUpdateThrottle(2000);
        return loader;
    }

    @Override
    public void onLoadFinished(final Loader Loader,
                               final Object obj) {
        mNavAdapter.updateData((List<FeedWrapper>) obj);
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        // ..
    }

}
