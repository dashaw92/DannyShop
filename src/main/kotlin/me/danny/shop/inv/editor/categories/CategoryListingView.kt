package me.danny.shop.inv.editor.categories

import me.danny.shop.DannyShop
import me.danny.shop.data.Category
import me.danny.shop.data.Shop
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.MenuView
import me.danny.shop.inv.Page
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

class CategoryListingView(inv: Inventory) : MenuView {
    private val page = CategoryPages(DannyShop.SHOP.categories(), Pair(inv.size - 2, inv.size - 1))

    override fun build(inv: Inventory) {
        val border = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        (0 until inv.size).forEach { inv.setItem(it, border) }
        page.render(inv)
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): MenuView.ViewAction {
        when {
            event.slot == inv.size - 2 -> {
                page.prevPage(); build(inv)
            }

            event.slot == inv.size - 1 -> {
                page.nextPage(); build(inv)
            }

            event.currentItem != null && ItemBuilder.hasKey(
                event.currentItem!!,
                CategoryEditor.CATEGORY_KEY,
                PersistentDataType.STRING
            ) -> {
                val name = ItemBuilder.getValue(
                    event.currentItem!!,
                    CategoryEditor.CATEGORY_KEY,
                    PersistentDataType.STRING,
                    ""
                )
                if (name.trim().isBlank()) return MenuView.ViewAction.Pass

                val category = Shop.getCategory(name) ?: return MenuView.ViewAction.Pass
                return MenuView.ViewAction.ChangeView(CategoryEditorView(category))
            }
        }

        return MenuView.ViewAction.Pass
    }

    private class CategoryPages(coll: Collection<Category>, buttons: Pair<Int, Int>) :
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

}