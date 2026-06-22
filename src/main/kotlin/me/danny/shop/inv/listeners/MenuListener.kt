package me.danny.shop.inv.listeners

import me.danny.shop.inv.FullInvListener
import me.danny.shop.inv.Menu
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

internal object MenuListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val holder = event.view.topInventory.holder
        if (holder !is Menu) return
        event.isCancelled = true

        if (event.clickedInventory == event.view.bottomInventory && holder !is FullInvListener) return

        if (event.click != ClickType.LEFT && event.click != ClickType.RIGHT
            && event.click != ClickType.SHIFT_LEFT && event.click != ClickType.SHIFT_RIGHT
            && event.click != ClickType.DROP && event.click != ClickType.NUMBER_KEY
            && event.click != ClickType.MIDDLE || event.currentItem == null || event.currentItem?.type == Material.AIR
        ) {
            return
        }

        holder.onClick(event)
    }

    @EventHandler
    fun onInvClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder
        if (holder is Menu) holder.close()
    }

    @EventHandler
    fun onInvOpen(event: InventoryOpenEvent) {
        val holder = event.inventory.holder
        if (holder is Menu) Menu.openInv(event.player.uniqueId, holder)
    }
}