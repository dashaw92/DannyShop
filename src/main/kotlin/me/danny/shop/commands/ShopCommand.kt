package me.danny.shop.commands

import me.danny.shop.*
import me.danny.shop.inv.*
import me.danny.shop.inv.editor.categories.*
import me.danny.shop.inv.editor.cooldowns.*
import me.danny.shop.me.inv.shop.*
import org.bukkit.command.*
import org.bukkit.entity.*

object ShopCommand : CommandExecutor, TabCompleter {
    init {
        DannyShop.instance().getCommand("dannyshop")!!.tabCompleter = this
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("&6[DannyShop]&7 Sorry, but only players may use the shop.".color())
            return true
        }

        if (args.isEmpty()) {
            ShopMenu(sender)
            return true
        }

        when (args.first().lowercase()) {
            "import" -> ImportCommand.onCommand(sender, args.sliceArray(1 until args.size))
            "catedit" -> {
                if (!sender.hasPermission("dannyshop.admin")) {
                    sender.sendMessage("&cYou lack permission.".color())
                    return true
                }

                CategoryEditor(sender)
            }

            "cooldowns" -> {
                if (!sender.hasPermission("dannyshop.admin")) {
                    sender.sendMessage("&cYou lack permission.".color())
                    return true
                }

                CooldownEditor(sender)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.size < 2) return mutableListOf("import", "catedit", "cooldowns")
        return null
    }
}