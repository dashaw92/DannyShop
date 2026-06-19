package me.danny.shop.inv.ecolog

import me.danny.shop.DannyShop
import me.danny.shop.commands.fmt
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import java.time.format.DateTimeFormatter

internal class EcoLogDisplayView : MenuView {

    companion object {

    }

    override fun onOpen(): ViewAction = ViewAction.Resize(6)

    override fun build(inv: Inventory) {
        val logs = DannyShop.instance().ecolog.getLogs()
        for (i in listOf(9, 18, 27, 36, 5, 14, 23, 32, 41).withIndex()) {
            val record = logs.getOrNull(i.index) ?: break
            val item = DannyShop.SHOP.itemByIid(record.item)
            val display = item?.item?.display() ?: ItemBuilder.makeItem(Material.DIRT, "")
            val slot = i.value
            inv.setItem(
                slot + 0, ItemBuilder.makeItem(
                    Material.CLOCK, "Record ${i.index}", record.time.format(
                        DateTimeFormatter.BASIC_ISO_DATE
                    )
                )
            )
            inv.setItem(
                slot + 1,
                ItemBuilder.makeItem(
                    Material.PLAYER_HEAD,
                    Bukkit.getOfflinePlayer(record.seller).name ?: record.seller.toString()
                )
            )
            inv.setItem(
                slot + 2,
                ItemBuilder.setName(display, item?.itemName() ?: record.item.id)
            )
            inv.setItem(slot + 3, ItemBuilder.makeItem(Material.EMERALD, record.ext.fmt()))
        }
    }

    override fun onClick(
        inv: Inventory,
        event: InventoryClickEvent
    ): ViewAction {
        return ViewAction.Pass
    }
}