package me.danny.shop.commands

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.economy.Economy
import me.danny.shop.pluginMsg
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.BlockInventoryHolder

/**
 * Listens for players punching chests with the import wand
 * When this occurs, attempt to import the items in the chest
 * into the shop
 */
internal object SellWandListener : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST) //let protection plugins get first say and heed their cancellation result
    fun onPlayerPunch(event: PlayerInteractEvent) {
        val player = event.player

        if (!event.hasItem() || !SellWandCommand.isWand(event.item!!) || !player.hasPermission(Perm.SELL_WAND)) return

        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            if (!player.isSneaking && event.clickedBlock?.state is BlockInventoryHolder) return
            event.isCancelled = true
            return
        }
        if (event.action != Action.LEFT_CLICK_BLOCK || event.clickedBlock?.state !is Container) return
        event.isCancelled = true

        val state = event.clickedBlock!!.state as Container
        if (state.inventory.isEmpty) return

        player.pluginMsg("Selling items from container.")
        Economy.sellInventory(player, state.snapshotInventory, null)
        if(!state.update()) {
            DannyShop.instance().logger.warning("Failed to update container after sell wand interaction.")
            DannyShop.instance().logger.warning("To mitigate potential item duplication, the container will be deleted completely.")
            DannyShop.instance().logger.warning("Container at (${event.clickedBlock!!.location}) is now Air.")
            event.clickedBlock!!.type = Material.AIR
        }
    }

    @EventHandler
    fun onWandDrop(event: PlayerDropItemEvent) {
        if (ImportCommand.isWand(event.itemDrop.itemStack)) {
            event.player.pluginMsg("Removed import wand!")
            event.itemDrop.remove()
        }
    }
}