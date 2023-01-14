package me.danny.shop.inv.shop.purchasing

import me.danny.shop.data.Item
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Page
import me.danny.shop.me.danny.shop.data.attachKey
import me.danny.shop.me.danny.shop.inv.setName
import org.bukkit.inventory.Inventory

class QuantityDisplay(private val item: Item) :
    Page<Int>(item.quantities.predefined, Pair(1, 1), Pair(7, 3), Pair(45 - 3, 45 - 2)) {
    override fun display(inv: Inventory) {
        if (item.cost !is Item.Cost.Value) {

            return
        }

        val costPerUnit = item.cost.buy

        val icons = items.sorted().drop(page * size)
            .take(size)
            .map {
                val display = item.item.display()
                display.amount = it
                display.setName("&2x$it")

                val header =
                    "&6┌┤&4 DannyShop &6├──"
                val footer =
                    "&6└───────────"
                val fields: MutableList<String> = mutableListOf()

                fields.add("&dCost:")
                fields.add("  &eBuy: &7\$%,.2f".format(costPerUnit * it))

                val lore = fields.map { field -> "&6│ $field" }.toMutableList()
                lore.add(0, header)
                lore.add(footer)

                ItemBuilder.addLore(display, *lore.toTypedArray())
            }
            .map { it.attachKey(PurchaseMenu.PRICE_KEY, costPerUnit * it.amount) }

        var invIdx = start.second * 9 + start.second
        for (icon in icons) {
            inv.setItem(invIdx, icon)
            invIdx += 1
            if (invIdx % 9 == 0) invIdx += 2
        }
    }
}