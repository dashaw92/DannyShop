package me.danny.shop.tracking

import java.io.Serializable
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

class ItemVolumeHistory(val bins: LongArray = LongArray(TOTAL_BINS.toInt())) : Serializable {

    fun startNextBin() {
        bins.shift(1)
    }

    fun add(amount: Long) {
        bins[0] += amount
    }

    fun getFirstNDays(days: Int): List<List<Long>> {
        if (days < 1) return emptyList()

        val numBinsToday = binsToday()
        val clampedDays = days.coerceIn(1, TOTAL_DAYS)
        val numBins = (clampedDays * BINS_PER_DAY).toInt()

        val today = bins.slice(0 until numBinsToday).reversed()
        if (clampedDays == 1) return listOf(today)

        val days: MutableList<List<Long>> = bins.slice(numBinsToday until numBins)
            .reversed()
            .chunked(BINS_PER_DAY.toInt())
            .take(clampedDays - 1)
            .toMutableList()
        days += today
        return days
    }

    private fun binsToday(): Int {
        val midnight = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)

        val now = LocalDateTime.now()
        val minsSinceMidnight = ChronoUnit.MINUTES.between(midnight, now)
        return ceil(minsSinceMidnight / RESOLUTION.toDouble()).toInt()
    }
}

// [1, 2, 3, ..., n].shift(1) -> [0, 1, 2, 3, ... n - 1]
private fun LongArray.shift(amount: Int = 1) {
    val n = amount.coerceIn(0, size - 1)
    if (n == 0) return

    val retained = sliceArray(0 until size - n)

    for (i in n until size) {
        this[i] = retained[i - n]
    }

    for (i in 0 until n) {
        this[i] = 0
    }
}