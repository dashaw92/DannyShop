package me.danny.shop.economy

import java.time.Instant

class ResetTask(val period: Long) : Runnable {
    companion object {
        internal var nextReset: Long = Instant.now().toEpochMilli()
    }

    init {
        nextReset = Instant.now().toEpochMilli()
    }

    override fun run() {
        if (Instant.now().toEpochMilli() > nextReset) {
            nextReset = Instant.now().toEpochMilli() + (period * 50)
            VolumeTracker.applyAll { 0L }
        }
    }
}