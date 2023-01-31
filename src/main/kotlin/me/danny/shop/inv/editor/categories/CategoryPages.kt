package me.danny.shop.inv.editor.categories

import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.inv.*
import org.bukkit.inventory.*

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
                    .attachKey(CategoryEditor.CATEGORY_KEY, it.cid.id)
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