package me.danny.shop.inv.view

import org.bukkit.event.inventory.*
import org.bukkit.inventory.*

/**
 * Represents an inventory-agnostic GUI that can be plugged into
 * any menu. Can request the parent menu do specific actions view the
 * ViewAction class hierarchy
 */
interface MenuView {

    /**
     * Use this method to customize the inventory
     * when your view is first used
     */
    fun onOpen(): ViewAction = ViewAction.Pass

    /**
     * Used for views of [me.danny.shop.inv.RefreshPlease]
     */
    fun refresh(inv: Inventory) {}

    /**
     * Render the view here
     */
    fun build(inv: Inventory)

    /**
     * Handle button clicks here
     */
    fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction

}