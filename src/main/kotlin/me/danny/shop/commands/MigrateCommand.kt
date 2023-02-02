package me.danny.shop.commands

import me.danny.shop.*
import me.danny.shop.backend.*
import me.danny.shop.inv.*
import org.bukkit.command.*

object MigrateCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("dannyshop.admin")) {
            sender.sendMessage("&cYou lack permission".color())
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("&6[DannyShop] &7Usage: &e/migrate <to>".color())
            return false
        }

        val toBackend = when (args.first().lowercase()) {
            "yaml" -> BackendType.Yaml
            "mongo" -> BackendType.MongoDB
            else -> {
                sender.sendMessage("&6[DannyShop] &7Unrecognized backend type.".color())
                return false
            }
        }

        val pl = DannyShop.instance()
        BackendManager.getProvider(pl, toBackend).saveShop(pl, DannyShop.SHOP)
        sender.sendMessage("&6[DannyShop] &7If all went to plan, the shop has been saved via backend &e$toBackend&7!".color())
        return true
    }
}