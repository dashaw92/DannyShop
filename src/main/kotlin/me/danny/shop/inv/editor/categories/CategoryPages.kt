package me.danny.shop.inv.editor.categories

import me.danny.shop.DannyShop
import me.danny.shop.data.attachKey
import me.danny.shop.inv.Page
import me.danny.shop.model.Category
import me.danny.shop.utils.ItemBuilder
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag

internal class CategoryPages(buttons: Pair<Int, Int>, var selected: Category? = null) :
    Page<Category>(DannyShop.SHOP.categories(), Pair(1, 1), Pair(7, 3), buttons) {

    override fun display(inv: Inventory) {
        items = DannyShop.SHOP.categories()

        var invIdx = start.second * 9 + start.second
        val items = items
            .drop(page * size)
            .take(size)
            .map {
                val item = ItemBuilder.makeItem(it.display, "&e${it.name}",
                    "&9CID: &7${it.cid.id}",
                    "&9Permission: &e&o${it.permission ?: "&2None"}",
                    "&9Items: &7${DannyShop.SHOP.items(it.cid).size}",
                )
                    .let { i -> ItemBuilder.addAttribute(i, *ItemFlag.entries.toTypedArray()) }
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