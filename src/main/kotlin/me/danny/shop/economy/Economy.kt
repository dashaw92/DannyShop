package me.danny.shop.economy

import me.danny.shop.DannyShop
import me.danny.shop.model.ID
import me.danny.shop.model.Item
import me.danny.shop.pluginMsg
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import net.milkbowl.vault.economy.Economy as VaultEco

internal object Economy {
    private val econ by lazy(::getEconomy)

    fun hasEconomy(): Boolean = Bukkit.getPluginManager().getPlugin("Vault") != null
    private fun getEconomy(): VaultEco {
        if (!hasEconomy()) throw Exception("Attempted to get Vault instance with no Vault plugin active...")
        val rsp = Bukkit.getServicesManager().getRegistration(VaultEco::class.java)
            ?: throw Exception("Failed to load Economy from Vault...")
        return rsp.provider
    }

    private fun decreaseSlot(inv: Inventory, slot: Int, amount: Int = 1): Int {
        val item = inv.getItem(slot) ?: return 0
        val stackSize = item.amount
        if (amount >= stackSize) {
            inv.setItem(slot, null)
            return stackSize
        } else {
            item.amount = stackSize - amount
            return amount
        }
    }

    private fun getLimitForItem(viewer: Player, iid: ID, amount: Int): Int {
        val limit = LimitTracking.remaining(viewer, iid)
        if (limit != null) {
            if (limit == 0) {
                return 0
            }

            if (amount > limit) {
                return limit
            }
        }

        return amount
    }

    internal fun sell(viewer: Player, inventory: Inventory, iid: ID, amount: Int) {
        val item = DannyShop.SHOP.itemByIid(iid) ?: return
        if (item.cost !is Item.Cost.Value) return

        val value = item.cost.buy

        if (amount == 0) {
            viewer.pluginMsg("&cYou don't have any &o${itemName(item)}&c to sell!")
            return
        }

        var remaining = getLimitForItem(viewer, iid, amount)
        if (remaining == 0) {
            viewer.pluginMsg("&cYou've hit the limit for &o${itemName(item)}&c.")
            return
        }
        var sold = 0

        for (i in inventory.contents.indices) {
            val it = inventory.getItem(i)
            if (it == null || !item.matchesItemStack(it)) continue

            val soldThisSlot = decreaseSlot(inventory, i, remaining)
            sold += soldThisSlot
            remaining -= soldThisSlot
            if (remaining == 0) break
        }

        LimitTracking.add(viewer, iid, sold)
        messageProfit(viewer, item, sold, sold * value)
    }

    fun sellAll(viewer: Player, inventory: Inventory, iid: ID) {
        val item = DannyShop.SHOP.itemByIid(iid) ?: return
        var count = 0

        for (i in inventory.contents.indices) {
            val it = inventory.getItem(i)
            if (it == null || !item.matchesItemStack(it)) continue
            count += it.amount
        }

        sell(viewer, inventory, iid, count)
    }

    fun sellInventory(viewer: Player, inventory: Inventory, sellCategory: ID?) {
        val itemPool = DannyShop.SHOP.items
            .filterKeys { it.isVisible(viewer) }
            .filterKeys { sellCategory == null || it.cid == sellCategory }
            .values.flatten()
            .filter { it.cost is Item.Cost.Value }

        for (i in inventory.contents.indices) {
            val stack = inventory.getItem(i) ?: continue
            val match = itemPool.filter { it.matchesItemStack(stack) }
                .maxByOrNull { (it.cost as Item.Cost.Value).buy } ?: continue

            sellAll(viewer, inventory, match.iid)
        }
    }

    private fun messageProfit(viewer: Player, item: Item, sold: Int, profit: Double) {
        val name = itemName(item)
        if (sold == 0) {
            viewer.pluginMsg("&cYou don't have any &o$name&c to sell!")
        } else {
            viewer.pluginMsg("&aSold ${sold}x&o$name&a for $%,.2f.".format(profit))
            econ.depositPlayer(viewer, profit)
        }
    }

    private fun itemName(item: Item): String = humanize(
        when (item.item) {
            is Item.ItemType.Mat -> item.item.material.name
            is Item.ItemType.Item -> item.item.item.type.name
        }
    )

    private fun humanize(name: String): String =
        name.split('_').joinToString(" ") { word -> word.first().uppercaseChar() + word.substring(1).lowercase() }
}