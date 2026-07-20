package me.danny.shop.tracking

import me.danny.shop.config.Config
import me.danny.shop.model.Item
import org.bukkit.entity.Player
import java.math.BigDecimal

internal sealed interface Logging {
    fun load() {}
    fun save() {}
    fun log(player: Player, item: Item, amount: Long, profit: Double) {}
    fun getLogs(): List<SaleRecord> = listOf()
}

internal fun getLogging(config: Config): Logging {
    return if (config.loggingEnabled) {
        EcoLogging(config)
    } else {
        DummyLogging
    }
}

private object DummyLogging : Logging

internal class EcoLogging(private val config: Config) : Logging {

    internal lateinit var currentLog: EcoLogMgr.EcoLog

    override fun load() {
        if (!config.loggingPersistLogs) return
        currentLog = EcoLogMgr.load()
    }

    override fun save() {
        if (!config.loggingPersistLogs || !::currentLog.isInitialized) return
        EcoLogMgr.save(currentLog)
    }

    override fun log(player: Player, item: Item, amount: Long, profit: Double) {
        currentLog.records.add(SaleRecord(player.uniqueId, item, amount, BigDecimal(profit)))
    }

    //Note: I'm aware that this is unsafe because the caller
    //could wipe the list. It's an internal function- either
    //someone is using reflection or I'm really messing up.
    override fun getLogs(): List<SaleRecord> = currentLog.records
}