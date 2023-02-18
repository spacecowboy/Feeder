package com.nononsenseapps.feeder.base

import android.app.IntentService
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

abstract class DIAwareIntentService(name: String) : IntentService(name), DIAware {
    override val di: DI by closestDI()
}
