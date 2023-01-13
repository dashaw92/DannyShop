package me.danny.shop.inv.editor.categories

import me.danny.shop.DannyShop
import me.danny.shop.data.Category
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Page
import me.danny.shop.me.danny.shop.data.attachKey
import org.bukkit.inventory.Inventory

class CategoryPages(buttons: Pair<Int, Int>, var selected: Category? = null) :
    Page<Category>(DannyShop.SHOP.categories(), Pair(1, 1), Pair(7, 3), buttons) {

    override fun display(inv: Inventory) {
        items = DannyShop.SHOP.categories()

        var invIdx = start.second * 9 + start.second
        val items = items
            .drop(page * size)
            .take(size)
            .map {
                val item = ItemBuilder.makeItem(it.display, "&9${it.name}")
                    .attachKey(CategoryEditor.CATEGORY_KEY, it.name)
                if (it.name == selected?.name) {
                    ItemBuilder.addEnchantGlow(item)
                } else {
                    item
                }
            }

        for (item in items) {
            inv.setItem(invIdx, item)
            invIdx += 1
        }
    }
}