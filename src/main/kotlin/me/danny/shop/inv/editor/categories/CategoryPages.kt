package me.danny.shop.inv.editor.categories

import me.danny.shop.data.Category
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Page
import me.danny.shop.me.danny.shop.data.attachKey
import org.bukkit.inventory.Inventory

class CategoryPages(coll: Collection<Category>, buttons: Pair<Int, Int>) :
    Page<Category>(coll, Pair(1, 1), Pair(7, 3), buttons) {
    override fun display(inv: Inventory) {
        var invIdx = start.second * 9 + start.second
        val items = items
            .drop(page * size)
            .take(size)
            .map {
                ItemBuilder.makeItem(it.display, "&9${it.name}")
                    .attachKey(CategoryEditor.CATEGORY_KEY, it.name)
            }

        for (item in items) {
            inv.setItem(invIdx, item)
            invIdx += 1
        }
    }
}