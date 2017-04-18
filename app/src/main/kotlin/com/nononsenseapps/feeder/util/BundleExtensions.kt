package com.nononsenseapps.feeder.util

import android.os.Bundle

inline fun bundle(init: Bundle.() -> Unit): Bundle {
    val bundle = Bundle()
    bundle.init()
    return bundle
}
