package me.danny.shop.inv.editor.categories

import me.danny.shop.data.hasKey
import me.danny.shop.data.keyValue
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.model.ID
import me.danny.shop.model.Shop
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.fill
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

internal class CategoryListingView : MenuView {
    private lateinit var page: CategoryPages

    constructor()

    private constructor(page: CategoryPages) {
        this.page = page
    }

    override fun onOpen(): ViewAction = ViewAction.Resize(5)

    override fun build(inv: Inventory) {
        if (!this::page.isInitialized) {
            page = CategoryPages(Pair(inv.size - 2, inv.size - 1))
        }

        val border = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(border)
        inv.setItem(inv.size - 5, ItemBuilder.makeItem(Material.BARRIER, "&eClose"))

        page.render(inv)
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        when {
            event.currentItem!!.hasKey(CategoryEditor.CATEGORY_KEY) -> {
                val cid = ID(event.currentItem!!.keyValue(CategoryEditor.CATEGORY_KEY) ?: return ViewAction.Pass)
                val category = Shop.getCategory(cid) ?: return ViewAction.Pass
                return ViewAction.ChangeView(CategoryEditorView(category) { CategoryListingView(page) })
            }

            event.currentItem!!.type == Material.BARRIER -> event.whoClicked.closeInventory()
        }

        page.onClick(event) { build(inv) }
        return ViewAction.Pass
    }

}