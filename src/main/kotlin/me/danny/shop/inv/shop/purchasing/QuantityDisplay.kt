package me.danny.shop.inv.shop.purchasing

import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.model.*
import me.danny.shop.utils.*
import org.bukkit.inventory.*

internal class QuantityDisplay(private val item: Item) :
    Page<Int>(item.quantities.predefined, Pair(1, 1), Pair(7, 3), Pair(45 - 3, 45 - 2)) {
    override fun display(inv: Inventory) {
        if (item.cost !is Item.Cost.Value) {

            return
        }

        val costPerUnit = item.cost.buy

        val icons = items.asSequence().sorted().drop(page * size)
            .take(size)
            .map {
                val display = item.item.display()
                display.amount = it
                display.setName("&7x$it")

                val lore = LoreField()
                lore.add("&9Cost:")
                lore.add("  &3Each: &7\$%,.2f".format(costPerUnit * it))

                ItemBuilder.addLore(display, *lore.build())
            }
            .map { it.attachMarker(PurchaseMenu.PRICE_KEY) }.toList()

        var invIdx = start.second * 9 + start.second
        for (icon in icons) {
            inv.setItem(invIdx, icon)
            invIdx += 1
            if (invIdx % 9 == 0) invIdx += 2
        }
    }
}