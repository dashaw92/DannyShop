package me.danny.shop.commands

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.economy.Economy
import me.danny.shop.model.Item
import me.danny.shop.pluginMsg
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object SellCommand : TabExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): Boolean {
        if (sender !is Player) {
            sender.pluginMsg("&cSorry, but only players may use this command.")
            return true
        }

        if (!sender.hasPermission(Perm.SELL)) {
            sender.pluginMsg("&cYou aren't allowed to sell items.")
            return true
        }

        var amountIndex = 1
        when (args.firstOrNull()?.lowercase()) {
            "all" -> {
                Economy.sellInventory(sender, sender.inventory, null)
            }

            else -> {
                if (args.size == 1 && args[0]!!.lowercase() != "hand") amountIndex = 0

                var item = sender.inventory.itemInMainHand
                if (item.type.isAir) item = sender.inventory.itemInOffHand
                if (item.type.isAir) {
                    sender.pluginMsg("&cNot holding anything to sell!")
                    return true
                }

                val itemPool = DannyShop.SHOP.sellableItems(sender, null)
                val match = itemPool.filter { it.matchesItemStack(item) }
                    .maxByOrNull { (it.cost as Item.Cost.Value).buy }
                if (match == null) {
                    sender.pluginMsg("&cItem cannot be sold to server.")
                    return true
                }

                var amount = item.amount
                if (args.size > amountIndex && args[amountIndex]?.lowercase() == "all") {
                    Economy.sellAll(sender, sender.inventory, match.iid)
                } else {
                    if (args.size > amountIndex) amount = args[amountIndex]?.toIntOrNull() ?: amount
                    Economy.sell(sender, sender.inventory, match.iid, amount)
                    }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): List<String?>? {
        if (!sender.hasPermission(Perm.SELL)) return null
        val allIds = listOf("hand", "all")
        if (args.isEmpty()) return allIds
        return when (args.size) {
            1 -> allIds.filter { id -> id.lowercase().startsWith(args[0]!!.lowercase()) }
            2 -> {
                val amount = args[1]?.toIntOrNull() ?: ""
                (0..9).map { "$amount$it".trim() }.toMutableList().let { it.add("all"); it }
            }

            else -> null
        }
    }
}