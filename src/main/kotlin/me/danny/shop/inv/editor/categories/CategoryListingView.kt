package me.danny.shop.inv.editor.categories

import me.danny.shop.DannyShop
import me.danny.shop.data.Shop
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.me.danny.shop.data.hasKey
import me.danny.shop.me.danny.shop.data.keyValue
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.view.MenuView
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class CategoryListingView : MenuView {
    private lateinit var page: CategoryPages

    override fun onOpen(): ViewAction = ViewAction.Resize(5)

    override fun build(inv: Inventory) {
        if (!this::page.isInitialized) {
            page = CategoryPages(DannyShop.SHOP.categories(), Pair(inv.size - 2, inv.size - 1))
        }

        val border = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(border)
        inv.setItem(inv.size - 5, ItemBuilder.makeItem(Material.BARRIER, "&cClose"))

        page.render(inv)
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        when {
            event.currentItem!!.hasKey(CategoryEditor.CATEGORY_KEY) -> {
                val name = event.currentItem!!.keyValue(CategoryEditor.CATEGORY_KEY) ?: ""
                if (name.trim().isBlank()) return ViewAction.Pass

                val category = Shop.getCategory(name) ?: return ViewAction.Pass
                return ViewAction.ChangeView(CategoryEditorView(category))
            }

            event.currentItem!!.type == Material.BARRIER -> event.whoClicked.closeInventory()
        }

        page.onClick(event) { build(inv) }
        return ViewAction.Pass
    }

}