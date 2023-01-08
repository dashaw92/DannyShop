package me.danny.shop.inv.editor.categories

import me.danny.shop.data.Category
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Page
import org.bukkit.ChatColor
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

class CategoryPages(coll: Collection<Category>, buttons: Pair<Int, Int>) :
    Page<Category>(coll, Pair(1, 1), Pair(7, 3), buttons) {
    override fun display(inv: Inventory) {
        var invIdx = start.second * 9 + start.second
        for (item in items
            .drop(page * size)
            .take(size)
            .map {
                ItemBuilder.attachKey(
                    ItemBuilder.makeItem(it.display, "${ChatColor.BLUE}${it.name}"),
                    CategoryEditor.CATEGORY_KEY,
                    PersistentDataType.STRING,
                    it.name
                )
            }) {
            inv.setItem(invIdx, item)
            invIdx += 1
        }
    }
}