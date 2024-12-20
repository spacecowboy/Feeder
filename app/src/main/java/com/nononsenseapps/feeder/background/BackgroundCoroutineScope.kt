package com.nononsenseapps.feeder.background

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BackgroundCoroutineScope : CoroutineScope {
    override val coroutineContext = Dispatchers.Default + SupervisorJob()
}
