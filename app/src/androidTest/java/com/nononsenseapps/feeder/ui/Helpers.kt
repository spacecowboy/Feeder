package com.nononsenseapps.feeder.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

/**
 * Delays while the factory doesn't provide the correct object
 */
suspend fun <T> whileNotEq(other: Any?,
                           timeoutMillis: Long = 500,
                           sleepMillis: Long = 50,
                           body: (() -> T)): T =
        withTimeout(timeoutMillis) {
            var item = body.invoke()
            while (item != other) {
                delay(sleepMillis)
                item = body.invoke()
            }
            item
        }
