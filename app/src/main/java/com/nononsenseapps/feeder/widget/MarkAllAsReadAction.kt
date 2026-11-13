package com.nononsenseapps.feeder.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.archmodel.Repository
import org.kodein.di.instance

/**
 * Marks everything as read in the feed or tag which the widget is showing.
 * This matches the "mark all as read" action in the app.
 */
class MarkAllAsReadAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val app = context.applicationContext as FeederApplication
        val repository: Repository by app.di.instance()
        val (feedId, tag) = repository.currentWidgetFeedAndTag.value
        repository.markAllAsReadInFeedOrTag(feedId, tag)
    }
}
