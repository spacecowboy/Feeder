package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.base.CoroutineScopedKodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_UNSET
import org.kodein.di.Kodein

/**
 * Should only be created with the activity as its lifecycle
 */
class EphemeralState(kodein: Kodein) : CoroutineScopedKodeinAwareViewModel(kodein) {
    var lastOpenFeedId: Long = ID_UNSET
        set(value) {
            if (value != lastOpenFeedId) {
                firstVisibleListItem = null
            }
            field = value
        }
    var lastOpenFeedTag: String = ""
        set(value) {
            if (value != lastOpenFeedTag) {
                firstVisibleListItem = null
            }
            field = value
        }
    var firstVisibleListItem: Int? = null
}
