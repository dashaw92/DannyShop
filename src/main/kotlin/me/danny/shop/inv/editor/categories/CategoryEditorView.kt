package me.danny.shop.inv.editor.categories

import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.view.*
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.inv.view.*
import org.bukkit.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.persistence.*

class CategoryEditorView(val category: Category) : MenuView {

    companion object {
        private val DELETE_BUTTON_KEY = Key("delete_category", PersistentDataType.BYTE)
    }

    override fun onOpen(): ViewAction = ViewAction.Resize(3)

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        inv.setItem(
            13, ItemBuilder.makeItem(
                category.display, "&9${category.name}",
                "&eClick an item to change the icon"
            )
        )

        inv.setItem(
            inv.size - 9, ItemBuilder.makeItem(
                Material.BARRIER, "&4Delete Category",
                "&7This is irreversible!!!"
            ).attachMarker(DELETE_BUTTON_KEY)
        )

        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(CategoryListingView())
        if (event.clickedInventory == inv) {
            if (event.currentItem!!.hasMarker(DELETE_BUTTON_KEY)) {
                DannyShop.SHOP.deleteCategory(category.cid)
                event.whoClicked.sendMessage("&6[DannyShop] &eCategory &6${category.name}&e deleted.".color())
                return ViewAction.ChangeView(CategoryListingView())
            }

            return ViewAction.Pass
        }

        category.changeDisplay(event.currentItem!!.type)
        build(inv)
        return ViewAction.Pass
    }

}