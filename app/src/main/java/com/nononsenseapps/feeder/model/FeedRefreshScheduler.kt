package com.nononsenseapps.feeder.model

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Schedules ready hosts without letting a busy host occupy waiting worker slots. */
internal suspend fun <T> refreshConcurrently(
    items: List<T>,
    host: (T) -> String,
    concurrency: Int = 8,
    perHost: Int = 2,
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
                val entry = pending.entries.firstOrNull { (key, _) -> active.getOrDefault(key, 0) < perHost } ?: break
                val key = entry.key
                val item = entry.value.removeFirst()
                // Rotate hosts so one large subscription group does not monopolize new slots.
                pending.remove(key)
                if (entry.value.isNotEmpty()) pending[key] = entry.value
                active[key] = active.getOrDefault(key, 0) + 1
                running++
                launch {
                    try {
                        refresh(item)
                    } finally {
                        completed.trySend(key)
                    }
                }
            }
            if (running > 0) {
                val key = completed.receive()
                active[key] = active.getValue(key) - 1
                running--
            }
        }
        completed.close()
    }
