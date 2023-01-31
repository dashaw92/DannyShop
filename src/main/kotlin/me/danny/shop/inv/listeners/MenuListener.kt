package me.danny.shop.inv.listeners

import me.danny.shop.inv.*
import org.bukkit.*
import org.bukkit.event.*
import org.bukkit.event.inventory.*

object MenuListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val holder = event.view.topInventory.holder
        if (holder !is Menu) return
        event.isCancelled = true

        if (event.clickedInventory == event.view.bottomInventory && holder !is FullInvListener) return

        if (event.click != ClickType.LEFT && event.click != ClickType.RIGHT
            && event.click != ClickType.SHIFT_LEFT && event.click != ClickType.SHIFT_RIGHT
            && event.click != ClickType.DROP && event.click != ClickType.NUMBER_KEY
            || event.currentItem == null || event.currentItem?.type == Material.AIR
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