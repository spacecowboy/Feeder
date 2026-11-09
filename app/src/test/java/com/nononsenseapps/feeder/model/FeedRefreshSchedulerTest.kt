package com.nononsenseapps.feeder.model

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedRefreshSchedulerTest {
    @Test
    fun defaultsToOneRefreshPerHost() =
        runTest {
            val feeds = (1..4).toList()
            val refreshed = mutableListOf<Int>()
            var active = 0
            refreshConcurrently(feeds, host = { "same-host" }) { feed ->
                active++
                assertEquals(1, active)
                delay(1_000)
                refreshed += feed
                active--
            }
            assertEquals(feeds, refreshed)
            assertEquals(4_000L, testScheduler.currentTime)
        }

    @Test
    fun honorsExplicitPerHostLimit() =
        runTest {
            var active = 0
            var peak = 0
            refreshConcurrently((1..4).toList(), host = { "same-host" }, perHost = 2) {
                active++
                peak = maxOf(peak, active)
                delay(1_000)
                active--
            }
            assertEquals(2, peak)
            assertEquals(2_000L, testScheduler.currentTime)
        }

    @Test
    fun comparesNetworkBoundConcurrencyLimits() =
        runTest {
            val results = mutableMapOf<Int, Long>()
            for (limit in listOf(2, 4, 8, 12)) {
                val start = testScheduler.currentTime
                val visited = mutableListOf<Int>()
                refreshConcurrently((0 until 48).toList(), host = { "host-$it" }, concurrency = limit) {
                    delay(1_000)
                    visited += it
                }
                results[limit] = testScheduler.currentTime - start
                assertEquals((0 until 48).toSet(), visited.toSet())
                assertEquals(48, visited.size)
            }
            assertEquals(mapOf(2 to 24_000L, 4 to 12_000L, 8 to 6_000L, 12 to 4_000L), results)
            println("Simulated 48-feed network workload (virtual milliseconds): $results")
        }

    @Test
    fun busyHostDoesNotBlockOtherHostsAndLimitsAreRespected() =
        runTest {
            val items = List(20) { "slow" } + List(12) { "fast-$it" }
            val active = mutableMapOf<String, Int>()
            var total = 0
            var peak = 0
            val finished = mutableListOf<String>()
            val job =
                launch {
                    refreshConcurrently(items, host = { it }) { host ->
                        total++
                        peak = maxOf(peak, total)
                        active[host] = active.getOrDefault(host, 0) + 1
                        assertEquals(1, active.getValue(host))
                        assertTrue(total <= 8)
                        delay(if (host == "slow") 10_000 else 100)
                        finished += host
                        active[host] = active.getValue(host) - 1
                        total--
                    }
                }
            runCurrent()
            assertEquals(8, peak)
            testScheduler.advanceTimeBy(1_000)
            assertEquals(12, finished.size)
            assertTrue(finished.none { it == "slow" })
            job.join()
            assertEquals(items.sorted(), finished.sorted())
        }

    @Test
    fun cancellationStopsActiveWorkAndDoesNotStartQueuedFeeds() =
        runTest {
            var started = 0
            var stopped = 0
            val ready = CompletableDeferred<Unit>()
            val job =
                launch {
                    refreshConcurrently((0 until 50).toList(), host = { "host-$it" }) {
                        started++
                        if (started == 8) ready.complete(Unit)
                        try {
                            awaitCancellation()
                        } finally {
                            stopped++
                        }
                    }
                }
            ready.await()
            job.cancelAndJoin()
            assertEquals(8, started)
            assertEquals(started, stopped)
        }

    @Test
    fun emptyRefreshDoesNotStartWork() =
        runTest {
            refreshConcurrently(emptyList<String>(), host = { it }) { error("No feeds expected") }
        }
}
