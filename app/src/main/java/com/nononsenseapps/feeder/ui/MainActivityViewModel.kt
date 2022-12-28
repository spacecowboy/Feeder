package com.nononsenseapps.feeder.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.util.currentlyCharging
import com.nononsenseapps.feeder.util.currentlyConnected
import com.nononsenseapps.feeder.util.currentlyUnmetered
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.threeten.bp.Instant

class MainActivityViewModel(di: DI) : DIAwareViewModel(di) {
    private val repository: Repository by instance()
    private val context: Context by instance()

    fun setResumeTime() {
        repository.setResumeTime(Instant.now())
    }

    val shouldSyncOnResume: Boolean =
        repository.syncOnResume.value

    val currentTheme: StateFlow<ThemeOptions> =
        repository.currentTheme

    val darkThemePreference: StateFlow<DarkThemePreferences> =
        repository.preferredDarkTheme

    fun ensurePeriodicSyncConfigured() = viewModelScope.launch {
        repository.ensurePeriodicSyncConfigured()
    }

    fun schedulePeriodicProofOfLife() {
        repository.schedulePeriodicProofOfLife()
    }

    val dynamicColors: StateFlow<Boolean> =
        repository.useDynamicTheme

    val textScale: StateFlow<Float> =
        repository.textScale

    fun isOkToSyncAutomatically(): Boolean =
        currentlyConnected(context) &&
            (!repository.syncOnlyWhenCharging.value || currentlyCharging(context)) &&
            (!repository.syncOnlyOnWifi.value || currentlyUnmetered(context))
}
