package me.danny.shop.inv.ecolog

import me.danny.shop.DannyShop
import me.danny.shop.commands.fmt
import me.danny.shop.inv.Page
import me.danny.shop.tracking.EcoLogMgr
import me.danny.shop.tracking.SaleRecord
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.PlayerCache
import me.danny.shop.utils.getElapsed
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal class EcoLogPages(buttons: Pair<Int, Int>) :
    Page<SaleRecord>(DannyShop.instance().ecolog.getLogs(), 0 to 0, 9 to 5, buttons) {

    companion object {
        private val zdtFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        internal val availableLogs = EcoLogMgr.availableLogs().sorted()
    }

    var iconMode: LogIconMode = LogIconMode.Item
    var sortMode: SortMode = SortMode.Newest
    var selectedLog: Int = -1

    fun nextLog() {
        selectedLog = when (selectedLog) {
            -1 if availableLogs.isEmpty() -> -1
            -1 -> 0
            availableLogs.lastIndex -> -1
            else -> selectedLog + 1
        }
    }

    fun prevLog() {
        selectedLog = when (selectedLog) {
            -1 if availableLogs.isEmpty() -> -1
            -1 -> availableLogs.lastIndex
            0 -> -1
            else -> selectedLog - 1
        }
    }

    override fun display(inv: Inventory) {
        items = sort(
            sortMode,
            if (selectedLog == -1) DannyShop.instance().ecolog.getLogs()
            else EcoLogMgr.loadHistorical(availableLogs[selectedLog])?.records ?: DannyShop.instance().ecolog.getLogs()
        )

        items
            .drop(page * size)
            .take(size)
            .forEachIndexed { index, record ->
                val item = DannyShop.SHOP.itemByIid(record.item)
                val display = ItemBuilder.setName(
                    ItemBuilder.addAttribute(
                        when (iconMode) {
                            LogIconMode.Player -> PlayerCache.getSkull(record.seller)
                            LogIconMode.Item -> item?.item?.display() ?: ItemBuilder.makeItem(
                                Material.BARRIER,
                                record.item.id
                            )
                        }, *ItemFlag.entries.toTypedArray()
                    ), "&e${item?.itemName() ?: record.item.id}"
                ).apply { amount = record.amount.coerceIn(1, 64).toInt() }

                val seller = PlayerCache.getNameOrUUID(record.seller)
                val time = record.time.format(zdtFmt)
                val elapsed = record.time.getElapsed()
                val name = record.item.id

                inv.setItem(
                    index, ItemBuilder.addLore(
                        display,
                        "&9ID: &7$name",
                        "&9Seller: &7$seller",
                        "&9Date: &7$time &8($elapsed)",
                        "&9Value: &2${record.ext.fmt()} &7(${record.amount}x${record.price.fmt()})"
                    )
                )
            }

    }

    private fun sort(sortMode: SortMode, records: List<SaleRecord>): List<SaleRecord> = when (sortMode) {
        SortMode.Oldest -> records.sortedBy(SaleRecord::time)
        SortMode.Newest -> records.sortedByDescending(SaleRecord::time)
        SortMode.Most -> records.sortedByDescending(SaleRecord::amount)
        SortMode.Least -> records.sortedBy(SaleRecord::amount)
        SortMode.Value -> records.sortedByDescending(SaleRecord::ext)
        SortMode.Seller -> records.sortedBy { rec -> PlayerCache.getNameOrUUID(rec.seller) }
    }
}

internal enum class LogIconMode {
    Player,
    Item
}

internal enum class SortMode {
    Newest,
    Oldest,
    Most,
    Least,
    Value,
    Seller
}
