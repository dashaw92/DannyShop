package me.danny.shop.commands

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.commands.SellWandCommand.isWand
import me.danny.shop.economy.Economy
import me.danny.shop.pluginMsg
import me.danny.shop.world.ProtectionProviders
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Listens for players right clicking chests with the sell wand
 */
internal object SellWandListener : Listener {

    @EventHandler
    fun onPlayerUse(event: PlayerInteractEvent) {
        val player = event.player

        if (!event.hasItem() || !SellWandCommand.isWand(event.item!!) || !player.hasPermission(Perm.SELL_WAND)) return

        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        event.isCancelled = true

        if (event.clickedBlock?.state !is Container) return

        if (!player.hasPermission(Perm.SELL)) {
            player.pluginMsg("&cYou aren't allowed to sell items.")
            return
        }

        if (!ProtectionProviders.canUseChestAt(player, event.clickedBlock!!.location)) {
            player.pluginMsg("No permission to sell from this.")
            return
        }

        val state = event.clickedBlock!!.state as Container
        if (state.inventory.isEmpty) {
            player.pluginMsg("Nothing to sell from this container.")
            return
        }

        player.pluginMsg("Selling items from container.")
        if (state.inventory.holder is DoubleChest) {
            val chest = state.inventory.holder as DoubleChest
            val left = (chest.leftSide as Chest).block
            val right = (chest.rightSide as Chest).block

            val canSellLeft = ProtectionProviders.canUseChestAt(player, left.location)
            val canSellRight = ProtectionProviders.canUseChestAt(player, right.location)

            if (canSellLeft && canSellRight) {
                sellBlockState(player, left, left.state as Container)
                sellBlockState(player, right, right.state as Container)
            } else {
                player.pluginMsg("No permission to sell from this.")
                return
            }
        } else {
            sellBlockState(player, event.clickedBlock!!, state)
        }
    }

    private fun sellBlockState(player: Player, block: Block, state: Container) {
        Economy.sellInventory(player, state.snapshotInventory, null)

        if (!state.update()) {
            DannyShop.instance().logger.warning("Failed to update container after sell wand interaction.")
            DannyShop.instance().logger.warning("To mitigate potential item duplication, the container will be deleted completely.")
            DannyShop.instance().logger.warning("Container at (${block.location.world?.name}, ${block.location.blockX}, ${block.location.blockY}, ${block.location.blockZ}, ) is now Air.")
            block.type = Material.AIR
        }
    }

    @EventHandler
    fun onWandDrop(event: PlayerDropItemEvent) {
        if (SellWandCommand.isWand(event.itemDrop.itemStack)) {
            event.player.pluginMsg("Removed sell wand!")
            event.itemDrop.remove()
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDeath(event: PlayerDeathEvent) {
        var i = 0
        while (i < event.drops.size) {
            if (isWand(event.drops[i])) {
                event.drops.removeAt(i)
            } else {
                i++
            }
        }
    }

//    @EventHandler
//    fun onInvClick(event: InventoryClickEvent) {
//        val isWand = SellWandCommand::isWand
//
//        if (event.click == ClickType.NUMBER_KEY) {
//            val item = event.whoClicked.inventory.getItem(event.hotbarButton) ?: return
//            if (item.type.isAir) return
//            event.isCancelled = isWand(item)
//        } else {
//            val item = event.view.getItem(event.rawSlot) ?: return
//            if (event.clickedInventory !is PlayerInventory && event.cursor != null && !event.cursor!!.type.isAir) {
//                event.isCancelled = isWand(event.cursor!!)
//                return
//            } else {
//                if (item.type.isAir) return
//                event.isCancelled = isWand(item)
//                        && event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY
//            }
//        }
//    }
}