package com.nononsenseapps.feeder.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import android.widget.Toast
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.model.FeedParser
import com.nononsenseapps.feeder.ui.ARG_FEED_TITLE
import com.nononsenseapps.feeder.ui.FeedActivity

fun Context.makeToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

val Context.notificationManager: NotificationManagerCompat
    get() = NotificationManagerCompat.from(this)

/**
 * If feed already has a shortcut then it is updated and bumped to the top of the list.
 * Ensures that a maximum number of shortcuts is available at any time with the last used being bumped out of the list
 * first.
 */
fun Context.addDynamicShortcutToFeed(label: String, id: Any, icon: Icon? = null) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            val intent = Intent(this, FeedActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.withAppendedPath(URI_FEEDS, "$id")
            intent.putExtra(ARG_FEED_TITLE, label)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

            val current = shortcutManager.dynamicShortcuts.toMutableList()

            // Update shortcuts
            val shortcut: ShortcutInfo = ShortcutInfo.Builder(this, "$id")
                    .setShortLabel(label)
                    .setLongLabel(label)
                    .setIcon(icon ?: Icon.createWithBitmap(getLetterIcon(label, id, radius = shortcutManager.iconMaxHeight)))
                    .setIntent(intent)
                    .setDisabledMessage("Feed deleted")
                    .setRank(0)
                    .build()

            if (current.map { it.id }.contains(shortcut.id)) {
                // Just update existing one
                shortcutManager.updateShortcuts(mutableListOf(shortcut))
            } else {
                // Ensure we do not exceed max limits
                if (current.size >= Math.min(3, shortcutManager.maxShortcutCountPerActivity)) {
                    current.sortBy { it.rank }
                    current.last().let { removeDynamicShortcutToFeed(it.id) }
                }

                // It's new!
                shortcutManager.addDynamicShortcuts(mutableListOf(shortcut))
            }
        }
    } catch (error: Throwable) {
        Log.d("addDynamicShortCut", "Error during add of shortcut: ${error.message}")
    }
}

/**
 * Typically, launcher apps use this information to build a prediction model so that they can promote the shortcuts
 * that are likely to be used at the moment.
 */
fun Context.reportShortcutToFeedUsed(id: Any) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            shortcutManager.reportShortcutUsed("$id")
        }
    } catch (error: Throwable) {
        Log.d("reportShortcutUsed", "Error during report use of shortcut: ${error.message}")
    }
}

/**
 * Remove a shortcut to feed. Should be called when feed is deleted.
 */
fun Context.removeDynamicShortcutToFeed(id: Any) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            shortcutManager.removeDynamicShortcuts(mutableListOf("$id"))
        }
    } catch (error: Throwable) {
        Log.d("removeDynamicShortcut", "Error during removal of shortcut: ${error.message}")
    }
}

@Volatile private var _feedParserInitialized = false

/**
 * Returns the FeedParser singleton with a cache directory set.
 * Only sets the cache directory once.
 */
val Context.feedParser: FeedParser
    get() {
        synchronized(this) {
            if (!_feedParserInitialized) {
                // Yes, cacheDir can indeed be null
                FeedParser.setup(cacheDir = externalCacheDir ?: filesDir)
                _feedParserInitialized = true
            }
        }
        return FeedParser
    }
