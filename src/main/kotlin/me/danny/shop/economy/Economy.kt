package me.danny.shop.economy

import me.danny.shop.DannyShop
import me.danny.shop.Perm
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
        if (!viewer.hasPermission(Perm.SELL)) {
            viewer.pluginMsg("&cYou aren't allowed to sell items.")
            return
        }

        val item = DannyShop.SHOP.itemByIid(iid) ?: return
        if (item.cost !is Item.Cost.Value) return

        val value = item.cost.buy

        if (amount == 0) {
            viewer.pluginMsg("&cYou don't have any &o${item.itemName()}&c to sell!")
            return
        }

        var remaining = getLimitForItem(viewer, iid, amount)
        if (remaining == 0) {
            viewer.pluginMsg("&cYou've hit the limit for &o${item.itemName()}&c.")
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

        if (sold == 0) return
        LimitTracking.add(viewer, iid, sold)
        DannyShop.instance().analytics.log(item.iid, sold.toLong())
        DannyShop.instance().ecolog.log(viewer, item, sold.toLong(), sold * value)
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
        val itemPool = DannyShop.SHOP.sellableItems(viewer, sellCategory)

        //first collect all applicable items that will be sold in this transaction
        //skipping this step and selling directly will potentially diminish profits
        //if they're selling an item that has more than one applicable shop item match
        val foundItems = mutableSetOf<Item>()
        for (i in inventory.contents.indices) {
            val stack = inventory.getItem(i) ?: continue
            val match = itemPool.filter { it.matchesItemStack(stack) }
                .maxByOrNull { (it.cost as Item.Cost.Value).buy } ?: continue

            foundItems += match
        }

        //now sort the applicable items by cost descending so the player earns as much as possible
        foundItems
            .sortedByDescending { (it.cost as Item.Cost.Value).buy }
            .forEach { sellAll(viewer, inventory, it.iid) }
    }

    private fun messageProfit(viewer: Player, item: Item, sold: Int, profit: Double) {
        val name = item.itemName()
        if (sold == 0) {
            viewer.pluginMsg("&cYou don't have any &o$name&c to sell!")
        } else {
            viewer.pluginMsg("&aSold ${sold}x&o$name&a for $%,.2f.".format(profit))
            econ.depositPlayer(viewer, profit)
        }
    }
}