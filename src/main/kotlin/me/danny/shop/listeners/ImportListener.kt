package me.danny.shop.listeners

import com.earth2me.essentials.*
import me.danny.shop.*
import me.danny.shop.commands.*
import me.danny.shop.data.*
import me.danny.shop.data.Item
import me.danny.shop.me.danny.shop.inv.*
import org.bukkit.*
import org.bukkit.block.*
import org.bukkit.entity.*
import org.bukkit.event.*
import org.bukkit.event.block.*
import org.bukkit.event.player.*
import org.bukkit.inventory.*

/**
 * Listens for players punching chests with the import wand
 * When this occurs, attempt to import the items in the chest
 * into the shop
 */
object ImportListener : Listener {

    private val chests = object : Tag<Material> {
        val materials = mutableSetOf(Material.CHEST, Material.TRAPPED_CHEST)

        @Suppress("DEPRECATION")
        override fun getKey(): NamespacedKey = NamespacedKey("dannyshop", "chest_tag")
        override fun getValues(): MutableSet<Material> = materials
        override fun isTagged(item: Material): Boolean = materials.contains(item)
    }

    @EventHandler
    fun onPlayerPunch(event: PlayerInteractEvent) {
        val player = event.player

        if (!event.hasItem() || !ImportCommand.isWand(event.item!!) || !player.hasPermission("dannyshop.import")) return
        if (event.action == Action.RIGHT_CLICK_BLOCK && chests.isTagged(event.clickedBlock!!.type)) return
        event.isCancelled = true
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        if (!chests.isTagged(event.clickedBlock!!.type)) return

        val state = event.clickedBlock!!.state as Chest
        if (state.customName == null) return
        val category = Category(state.customName!!, Material.BOOK)
        if (Shop.getCategory(category.name) == null) {
            Shop.addCategory(category)
            player.sendMessage("&6[DannyShop] Created category &e${category.name}".color())
        }

        player.sendMessage("&6[DannyShop] Importing items from chest into category &e${category.name}".color())
        importItems(player, category, state.inventory)
    }

    private fun importItems(player: Player, category: Category, inv: Inventory) {
        for (item in inv.filterNotNull()) {
            if (item.type.isAir) continue

            val iid = Item.IID.generate()
            val type = if (item.hasItemMeta()) {
                Item.ItemType.Item(item)
            } else {
                Item.ItemType.Mat(item.type)
            }

            val cost = when (type) {
                is Item.ItemType.Mat, is Item.ItemType.Item -> getWorth(item)
                else -> Item.Cost.NotSet
            }

            val quantities = when (type) {
                is Item.ItemType.Mat -> Item.Quantities(listOf(1, 32, 64), Item.Quantities.Allowed.Any)
                else -> Item.Quantities(listOf(1), Item.Quantities.Allowed.Predefined)
            }

            val cooldown = Item.Cooldown.None

            val shopItem = Item(iid, null, type, cost, cooldown, quantities, category)
            player.sendMessage("&6[DannyShop] Imported &7\"${iid.id}\"".color())
            DannyShop.SHOP.addItem(shopItem)
        }

        inv.clear()
    }

    @EventHandler
    fun onWandDrop(event: PlayerDropItemEvent) {
        if (ImportCommand.isWand(event.itemDrop.itemStack)) {
            event.player.sendMessage("&6[DannyShop] Removed import wand!".color())
            event.itemDrop.remove()
        }
    }

    private fun getWorth(item: ItemStack): Item.Cost {
        val plug = Bukkit.getPluginManager().getPlugin("Essentials") ?: return Item.Cost.NotSet
        val ess = plug as Essentials

        val sell = ess.worth.getPrice(ess, item)?.toDouble() ?: return Item.Cost.NotSet
        val buy = 1.25 * sell
        return Item.Cost.Value(buy, sell)
    }
}