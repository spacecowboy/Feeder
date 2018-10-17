package com.nononsenseapps.feeder

import android.app.Application
import org.conscrypt.Conscrypt
import java.security.Security

@Suppress("unused")
class FeederApplication: Application() {
    init {
        // Install Conscrypt to handle missing SSL cyphers on older platforms
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }
}
