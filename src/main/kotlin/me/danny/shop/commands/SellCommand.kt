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

        when (args.firstOrNull()?.lowercase()) {
            "all" -> {
                Economy.sellInventory(sender, sender.inventory, null)
            }

            "hand" -> {
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

                val amount = args.last()?.toLongOrNull() ?: item.amount.toLong()
                if (args.last()?.lowercase() == "all") {
                    Economy.sellAll(sender, sender.inventory, match.iid)
                } else {
                    Economy.sell(sender, sender.inventory, match.iid, amount)
                }
            }

            else -> sender.pluginMsg("Invalid command. Use &e/$label all &7or &e/$label hand [amount]&7.")
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
            2 if args[0]!!.equals("hand", true) -> {
                val amount = args[1]?.toLongOrNull() ?: ""
                (0..9).map { "$amount$it".trim() }.toMutableList().let { it.add("all"); it }
            }

            else -> null
        }
    }
}