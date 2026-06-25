package me.danny.shop.tracking

import me.danny.shop.DannyShop
import me.danny.shop.config.Config
import me.danny.shop.model.ID
import org.bukkit.Bukkit
import java.io.*
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.abs


internal val UNITS = TimeUnit.MINUTES
internal const val RESOLUTION = 15L
internal const val BINS_PER_DAY = 24 * (60 / RESOLUTION)

//How many days of history to keep
internal const val TOTAL_DAYS = 90
internal const val TOTAL_BINS = TOTAL_DAYS * BINS_PER_DAY

internal sealed interface Analytics {
    fun load() {}
    fun save() {}
    fun log(item: ID, amount: Long) {}
    fun tick() {}
    fun allKeys(): Set<ID> = setOf()
    fun getHistory(query: ID): ItemVolumeHistory? = null
}

internal fun getAnalytics(config: Config): Analytics {
    return if (config.ecoAnalyticsEnabled) {
        EcoAnalytics()
    } else {
        DummyAnalytics
    }
}

private object DummyAnalytics : Analytics

class EcoAnalytics : Analytics {

    private val file = DannyShop.instance().dataFolder.resolve("item-history.bin")

    class Data : Serializable {
        companion object {
            @JvmStatic
            val serialVersionUID: Long = 1L
        }

        var lastTick: Long = Instant.now().toEpochMilli()
        val history: MutableMap<ID, ItemVolumeHistory> = mutableMapOf()
    }

    var data: Data = Data()

    override fun load() {
        DannyShop.instance().logger.info("Volume analytics: Loading item history from ${file.absolutePath}")
        if (!file.exists()) file.createNewFile()
        try {
            val read = ObjectInputStream(GZIPInputStream(FileInputStream(file))).use(ObjectInputStream::readObject)
            if (read is Data) {
                data = read
            }
        } catch (ex: Exception) {
            DannyShop.instance().logger.severe("Volume analytics: Error while loading item history from ${file.absolutePath}:")
            ex.printStackTrace()
        }

        val delta = deltaPerTick()
        val nextTick = calcNextTick()
        while (data.lastTick < nextTick) {
            data.history.values.forEach(ItemVolumeHistory::startNextBin)
            data.lastTick += delta
        }

        for (item in DannyShop.SHOP.items.values.flatten()) {
            data.history.putIfAbsent(item.iid, ItemVolumeHistory())
        }
        DannyShop.instance().logger.info("Volume analytics: Item history loaded.")
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            DannyShop.instance(),
            ::tick, 0L, deltaPerTick() / 50
        )
    }

    override fun save() {
        if (!file.exists()) file.createNewFile()
        ObjectOutputStream(GZIPOutputStream(FileOutputStream(file))).use { obj -> obj.writeObject(data) }
    }

    override fun tick() {
        val now = Instant.now().toEpochMilli()
        if (abs(data.lastTick - now) < deltaPerTick()) return
        data.lastTick = calcNextTick()
        data.history.values.forEach(ItemVolumeHistory::startNextBin)
    }

    override fun allKeys(): Set<ID> = data.history.keys

    override fun getHistory(query: ID): ItemVolumeHistory =
        data.history.computeIfAbsent(query) { ItemVolumeHistory() }

    override fun log(item: ID, amount: Long) {
        getHistory(item).add(amount)
    }

    private fun deltaPerTick(): Long = TimeUnit.MILLISECONDS.convert(RESOLUTION, UNITS)
    private fun calcNextTick(): Long = Instant.now().toEpochMilli() + deltaPerTick()
}