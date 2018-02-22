package com.nononsenseapps.feeder.util

fun <K, V> Map<K, V>.getWithDefault(key: K, defaultValue: V): V {
    return get(key) ?: defaultValue
}
