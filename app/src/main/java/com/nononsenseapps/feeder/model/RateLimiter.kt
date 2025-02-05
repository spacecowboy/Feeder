package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Limits arbitrary operations to a certain rate.
 */
object RateLimiter {
    private const val LOG_TAG = "FEEDER_RATELIMIT"
    private const val MAX_PER_SECOND = 3
    private const val DELAY_MS = 1000L / MAX_PER_SECOND
    private val lastTime = ConcurrentHashMap<String, Long>()

    /**
     * Will sleep if the rate limit is exceeded.
     */
    fun <T> blockingRateLimited(
        key: String,
        block: () -> T,
    ): T {
        while (true) {
            val last = lastTime[key] ?: 0
            val now = System.currentTimeMillis()
            val diff = now - last
            if (diff < DELAY_MS) {
                val remainingTime = DELAY_MS - diff
                val sleepTime = Random.nextLong(remainingTime, remainingTime * 2)
                logDebug(LOG_TAG, "Rate limit hit [$key]. Sleeping for $sleepTime ms")
                Thread.sleep(sleepTime)
            } else {
                return block().also {
                    lastTime[key] = System.currentTimeMillis()
                    cleanOldEntries()
                }
            }
        }
    }

    /**
     * Will suspend if the rate limit is exceeded.
     */
    suspend fun <T> suspendingRateLimited(
        key: String,
        block: suspend () -> T,
    ): T {
        while (true) {
            val last = lastTime[key] ?: 0
            val now = System.currentTimeMillis()
            val diff = now - last
            if (diff < DELAY_MS) {
                val remainingTime = DELAY_MS - diff
                val sleepTime = Random.nextLong(remainingTime, remainingTime * 2)
                logDebug(LOG_TAG, "Delaying for $sleepTime ms")
                delay(sleepTime)
            } else {
                return block().also {
                    lastTime[key] = System.currentTimeMillis()
                    cleanOldEntries()
                }
            }
        }
    }

    fun cleanOldEntries() {
        val now = System.currentTimeMillis()
        lastTime.entries.removeIf {
            it.value < now - 60
        }
    }
}
