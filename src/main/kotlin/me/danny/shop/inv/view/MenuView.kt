package me.danny.shop.me.danny.shop.inv.view

import me.danny.shop.inv.view.ViewAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

interface MenuView {

    fun build(inv: Inventory)
    fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction

}