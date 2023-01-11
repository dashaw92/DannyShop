package me.danny.shop.me.danny.shop.inv.view

import me.danny.shop.inv.view.ViewAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * Represents an inventory-agnostic GUI that can be plugged into
 * any menu. Can request the parent menu do specific actions view the
 * ViewAction class hierarchy
 */
interface MenuView {

    /**
     *
     */
    fun build(inv: Inventory)

    /**
     *
     */
    fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction

}