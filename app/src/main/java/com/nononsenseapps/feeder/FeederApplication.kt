package com.nononsenseapps.feeder

import androidx.multidex.MultiDexApplication
import org.conscrypt.Conscrypt
import java.security.Security

@Suppress("unused")
class FeederApplication: MultiDexApplication() {
    init {
        // Install Conscrypt to handle missing SSL cyphers on older platforms
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }
}
