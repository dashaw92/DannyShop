package me.danny.shop.inv

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

interface MenuView {

    fun build(inv: Inventory)
    fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction

    sealed interface ViewAction {
        object Pass : ViewAction
        data class ChangeView(val newView: MenuView) : ViewAction
    }
}