package me.danny.shop.listeners

import com.earth2me.essentials.Essentials
import me.danny.shop.DannyShop
import me.danny.shop.commands.ImportCommand
import me.danny.shop.data.Category
import me.danny.shop.data.Item
import me.danny.shop.data.Shop
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object ImportListener : Listener {

    val chests = object : Tag<Material> {
        val materials = mutableSetOf(Material.CHEST, Material.TRAPPED_CHEST)

        @Suppress("DEPRECATION")
        override fun getKey(): NamespacedKey = NamespacedKey("dannyshop", "chest_tag")
        override fun getValues(): MutableSet<Material> = materials
        override fun isTagged(item: Material): Boolean = materials.contains(item)
    }

    @EventHandler
    fun onPlayerPunch(event: PlayerInteractEvent) {
        val player = event.player

        if (event.action != Action.LEFT_CLICK_BLOCK) return
        if (!event.hasItem() || !ImportCommand.isWand(event.item!!) || !player.hasPermission("dannyshop.import")) return
        if (!chests.isTagged(event.clickedBlock!!.type)) return
        event.isCancelled = true

        val state = event.clickedBlock!!.state as Chest
        if (state.customName == null) return
        val category = Category(state.customName!!, Material.BOOK)
        if (Shop.getCategory(category.name) == null) {
            Shop.addCategory(category)
        }

        player.sendMessage("${ChatColor.GOLD}[DannyShop] Importing items from chest into category ${ChatColor.YELLOW}${category.name}")
        importItems(player, category, state.inventory)
    }

    private fun importItems(player: Player, category: Category, inv: Inventory) {
        for (item in inv.filterNotNull()) {
            val iid = Item.IID.generate()
            val type = if (item.hasItemMeta()) {
                Item.ItemType.Item(item)
            } else {
                Item.ItemType.Mat(item.type)
            }

            val cost = getWorth(item)
            val quantities = Item.Quantities(listOf(1, 32, 64), Item.Quantities.Allowed.Any)
            val cooldown = Item.Cooldown.None

            val shopItem = Item(iid, type, cost, cooldown, quantities, category)
            player.sendMessage("Adding \"${iid.id}\" to category \"${category.name}\"")
            DannyShop.SHOP.addItem(shopItem)
        }
        println(DannyShop.SHOP)
    }

    @EventHandler
    fun onWandDrop(event: PlayerDropItemEvent) {
        if (ImportCommand.isWand(event.itemDrop.itemStack)) {
            event.player.sendMessage("${ChatColor.GOLD}[DannyShop] Removed import wand!")
            event.itemDrop.remove()
        }
    }

    private fun getWorth(item: ItemStack): Item.Cost {
        val plug = Bukkit.getPluginManager().getPlugin("Essentials") ?: return Item.Cost.NotSet
        val ess = plug as Essentials

        val buy = ess.worth.getPrice(ess, item)?.toDouble() ?: return Item.Cost.NotSet
        val sell = buy * 0.75
        return Item.Cost.Value(buy, sell)
    }
}