package me.danny.shop.tracking

import java.io.Serializable

internal class ItemVolumeHistory(val bins: LongArray = LongArray(TOTAL_BINS.toInt())) : Serializable {

    fun startNextBin() {
        bins.shift(1)
    }

    fun add(amount: Long) {
        bins[0] += amount
    }

    fun getFirstNDays(days: Int): List<List<Long>> {
        val clampedDays = days.coerceIn(1, TOTAL_DAYS)
        val numBins = (clampedDays * BINS_PER_DAY).toInt()
        return bins.slice(0 until numBins)
            .reversed()
            .chunked(BINS_PER_DAY.toInt())
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