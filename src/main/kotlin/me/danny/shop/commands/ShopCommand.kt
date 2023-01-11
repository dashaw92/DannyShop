package me.danny.shop.commands

import me.danny.shop.DannyShop
import me.danny.shop.inv.editor.categories.CategoryEditor
import me.danny.shop.me.danny.shop.inv.color
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ShopCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("&cNope!".color())
            return true
        }

        if (args.isEmpty()) {
            ShopMenu(DannyShop.SHOP, sender)
            return true
        }

        when (args.first().lowercase()) {
            "import" -> ImportCommand.onCommand(sender, args.sliceArray(2 until args.size))
            "catedit" -> {
                if (!sender.hasPermission("dannyshop.admin")) {
                    sender.sendMessage("&cYou lack permission.".color())
                    return true
                }

                CategoryEditor(sender)
            }
        }
        return true
    }

}