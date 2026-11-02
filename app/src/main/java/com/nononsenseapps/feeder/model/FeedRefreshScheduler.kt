package com.nononsenseapps.feeder.model

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Schedules ready hosts without letting a busy host occupy waiting worker slots. */
internal suspend fun <T> refreshConcurrently(
    items: List<T>,
    host: (T) -> String,
    concurrency: Int = 8,
    perHost: Int = 1,
    refresh: suspend (T) -> Unit,
): Unit =
    coroutineScope {
        require(concurrency > 0 && perHost > 0)
        val pending = items.groupBy(host).mapValues { (_, values) -> ArrayDeque(values) }.toMutableMap()
        val active = mutableMapOf<String, Int>()
        val completed = Channel<String>(concurrency)
        var running = 0

        while (pending.isNotEmpty() || running > 0) {
            while (running < concurrency) {
                val entry = pending.entries.firstOrNull { (host, _) -> active.getOrDefault(host, 0) < perHost } ?: break
                val pendingHost = entry.key
                val pendingFeed = entry.value.removeFirst()
                // Rotate hosts so one large subscription group does not monopolize new slots.
                pending.remove(pendingHost)
                if (entry.value.isNotEmpty()) pending[pendingHost] = entry.value
                active[pendingHost] = active.getOrDefault(pendingHost, 0) + 1
                running++
                launch {
                    try {
                        refresh(pendingFeed)
                    } finally {
                        completed.trySend(pendingHost)
                    }
                }
            }
            if (running > 0) {
                val completedHost = completed.receive()
                active[completedHost] = active.getValue(completedHost) - 1
                running--
            }
        }
        completed.close()
    }
