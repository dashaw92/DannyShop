package me.danny.shop.config

internal data class Config(
    val ecoAnalyticsEnabled: Boolean = true,
    val loggingEnabled: Boolean = true,
    val loggingPersistLogs: Boolean = true,
    val sellLimitRefreshTicks: Long = 72000L,
)
