package com.nononsenseapps.feeder.util

import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

fun Instant.minusMinutes(minutes: Int): Instant =
        minus(minutes.toLong(), ChronoUnit.MINUTES)
