package me.danny.shop.inv.editor.categories

import me.danny.shop.DannyShop
import me.danny.shop.data.Shop
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.view.MenuView
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

class CategoryListingView(inv: Inventory) : MenuView {
    private val page = CategoryPages(DannyShop.SHOP.categories(), Pair(inv.size - 2, inv.size - 1))

    override fun build(inv: Inventory) {
        val border = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(border)

        page.render(inv)
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        when {
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
                if (name.trim().isBlank()) return ViewAction.Pass

                val category = Shop.getCategory(name) ?: return ViewAction.Pass
                return ViewAction.ChangeView(CategoryEditorView(category))
            }
        }

        page.onClick(event) { build(inv) }
        return ViewAction.Pass
    }

}