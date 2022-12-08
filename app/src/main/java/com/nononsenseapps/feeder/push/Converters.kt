package com.nononsenseapps.feeder.push

import org.threeten.bp.Instant

fun Timestamp.toInstant(): Instant =
    Instant.ofEpochSecond(seconds, nanos.toLong())

fun Instant.toProto(): Timestamp =
    Timestamp(epochSecond, nano)
