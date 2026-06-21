package me.danny.shop.inv.ecolog

import me.danny.shop.data.Key
import me.danny.shop.data.attachMarker
import me.danny.shop.data.hasMarker
import me.danny.shop.inv.LoreList
import me.danny.shop.inv.LoreList.toEntry
import me.danny.shop.inv.next
import me.danny.shop.inv.prev
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.fill
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

internal class EcoLogDisplayView : MenuView {

    companion object {
        internal val SORT_BUTTON = Key("sort_button", PersistentDataType.BYTE)
        internal val MODE_BUTTON = Key("mode_button", PersistentDataType.BYTE)
        internal val LOG_SELECT_BUTTON = Key("log_button", PersistentDataType.BYTE)
    }

    override fun onOpen(): ViewAction = ViewAction.Resize(6)

    private lateinit var pages: EcoLogPages
    private var selectedLog = "Current"

    override fun build(inv: Inventory) {
        if (!this::pages.isInitialized) {
            pages = EcoLogPages(Pair(inv.size - 2, inv.size - 1))
        }

        inv.fill(pages.fillerItem())
        pages.render(inv)

        val sortButton = ItemBuilder.makeItem(
            Material.HOPPER, "&eSort Mode", *LoreList.makeList(
                listOf(
                    SortMode.Newest toEntry listOf("Newest records first"),
                    SortMode.Oldest toEntry listOf("Oldest records first"),
                    SortMode.Most toEntry listOf("Records with most units first"),
                    SortMode.Least toEntry listOf("Records with least units first"),
                    SortMode.Value toEntry listOf("Highest value records first"),
                    SortMode.Seller toEntry listOf("Alphabetical sort of sellers"),
                ), selected = pages.sortMode
            )
        ).attachMarker(SORT_BUTTON)

        val modeButton = ItemBuilder.makeItem(
            Material.COMPARATOR, "&eIcon Mode", *LoreList.makeList(
                listOf(
                    LogIconMode.Item toEntry listOf("Display records by sold item icons"),
                    LogIconMode.Player toEntry listOf("Display records by seller's skull"),
                ), selected = pages.iconMode
            )
        ).attachMarker(MODE_BUTTON)

        val selectLogButton = ItemBuilder.makeItem(
            Material.WRITABLE_BOOK, "&eSelected Log", *LoreList.makeList(
                createAvailableLogs(EcoLogPages.availableLogs), selectedLog
            )
        ).attachMarker(LOG_SELECT_BUTTON)

        inv.setItem(inv.size - 7, selectLogButton)
        inv.setItem(inv.size - 8, sortButton)
        inv.setItem(inv.size - 9, modeButton)
    }

    override fun onClick(
        inv: Inventory, event: InventoryClickEvent
    ): ViewAction {
        pages.onClick(event) { build(inv) }

        if (event.currentItem!!.hasMarker(MODE_BUTTON)) {
            val stepFn = if (event.isLeftClick) pages.iconMode::next
            else pages.iconMode::prev
            pages.iconMode = stepFn()
            return ViewAction.Pass
        }

        if (event.currentItem!!.hasMarker(SORT_BUTTON)) {
            val stepFn = if (event.isLeftClick) pages.sortMode::next
            else pages.sortMode::prev
            pages.sortMode = stepFn()
            return ViewAction.Pass
        }

        if (event.currentItem!!.hasMarker(LOG_SELECT_BUTTON)) {
            val stepFn = if (event.isLeftClick) pages::nextLog
            else pages::prevLog
            stepFn()
            selectedLog = logName(EcoLogPages.availableLogs.getOrNull(pages.selectedLog) ?: "Current")
            return ViewAction.Pass
        }

        return ViewAction.Pass
    }

    private fun logName(fullName: String): String = when (fullName) {
        "Current" -> "Current"
        else -> fullName.removePrefix("sales-").removeSuffix(".csv.gz").replace('-', ' ')
    }

    private fun createAvailableLogs(opts: List<String>): List<LoreList.ListEntry<String>> =
        listOf("Current" toEntry listOf("Current logs")) + opts.map { fullName ->
            val name = logName(fullName)
            name toEntry listOf("Logs from $fullName")
        }
}