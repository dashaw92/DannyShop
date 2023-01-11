package me.danny.shop.inv.editor.categories

import me.danny.shop.data.Category
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.view.MenuView
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class CategoryEditorView(val category: Category) : MenuView {

    override fun onOpen(): ViewAction = ViewAction.Resize(3)

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        inv.setItem(
            13, ItemBuilder.makeItem(
                category.display, "&9${category.name}",
                "&eClick an item to change the icon"
            )
        )
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(CategoryListingView())
        if (event.clickedInventory == inv) return ViewAction.Pass

        category.changeDisplay(event.currentItem!!.type)
        build(inv)
        return ViewAction.Pass
    }

}