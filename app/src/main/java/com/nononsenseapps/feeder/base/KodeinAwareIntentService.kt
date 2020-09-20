package com.nononsenseapps.feeder.base

import android.app.IntentService
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein

abstract class KodeinAwareIntentService(name: String) : IntentService(name), KodeinAware {
    override val kodein: Kodein by closestKodein()
}
