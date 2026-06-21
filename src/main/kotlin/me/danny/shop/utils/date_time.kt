package me.danny.shop.utils

import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun ZonedDateTime.getElapsed(now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)): String {
    val duration = Duration.between(this, now)
    val days = duration.toDays()
    val hours = duration.toHours()
    val mins = duration.toMinutes()
    val secs = duration.toSeconds()

    val elapsed =
        if (days >= 1) "${days}d"
        else if (hours >= 1) "${hours}h"
        else if (mins >= 1) "${mins}m"
        else if (secs >= 1) "${secs}s"
        else "Now"

    return elapsed
}