package me.danny.shop.commands

import me.danny.shop.DannyShop
import me.danny.shop.pluginMsg
import org.bukkit.command.CommandSender

internal object BackendAdminCommand {
    fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.pluginMsg("Backend in use: &e${DannyShop.instance().backend.name()} &8(${DannyShop.instance().backend.type()})&7.")
            return true
        }

        if (args[0].lowercase() == "save") {
            save(sender)
        } else {
            sender.pluginMsg("Usage: /shop backend [save]")
        }

//        when (args[0].lowercase()) {
//            "migrate" -> migrationCommand(sender, args.copyOfRange(1, args.size))
//            "save" -> saveCommand(sender)
//            "switch" -> switchCommand(sender, args.copyOfRange(1, args.size))
//            else -> sender.pluginMsg("Usage: /shop backend [save | migrate <yaml|mongo> | switch <yaml|mongo>]")
//        }

        return true
    }

//    private fun saveCommand(sender: CommandSender) {
//        save(sender)
//    }

//    private fun migrationCommand(sender: CommandSender, args: Array<out String>) {
//        val toBackend = when (args.first().lowercase()) {
//            "yaml" -> BackendType.Yaml
//            else -> {
//                sender.pluginMsg("&cUnrecognized backend type. Supported: &7yaml&c.")
//                return
//            }
//        }
//
//        migrate(sender, toBackend)
//    }

    private fun save(sender: CommandSender) {
        val backend = DannyShop.instance().backend
        sender.pluginMsg("Attempting to save shop data via backend &e${backend.type()}&7...")
        DannyShop.instance().logger.info("${sender.name} invoked a manual save.")
        DannyShop.instance().saveAll()
        sender.pluginMsg("Save complete.")
    }

//    private fun migrate(sender: CommandSender, target: BackendType): ShopBackend? {
//        val pl = DannyShop.instance()
//        if (pl.backend.type() == target) {
//            sender.pluginMsg("&cCannot migrate to that backend: Already in use!")
//            return null
//        }
//
//        sender.pluginMsg("Migrating shop data to &e$target&7...")
//        DannyShop.instance().logger.info("${sender.name} invoked a migration (from=${pl.backend.type()}, to=$target)")
//        val targetProvider = BackendManager.getProvider(pl, target)
//        targetProvider.saveShop(pl, DannyShop.SHOP)
//        sender.pluginMsg("If all went to plan, the shop has been saved via backend &e$target&7!")
//        DannyShop.instance().logger.info("Migration complete")
//        return targetProvider
//    }
//
//    private fun switchCommand(sender: CommandSender, args: Array<out String>) {
//        val toBackend = when (args.first().lowercase()) {
//            "yaml" -> BackendType.Yaml
//            else -> {
//                sender.pluginMsg("&cUnrecognized backend type. Supported: &7yaml&c, &7mongo&c.")
//                return
//            }
//        }
//
//        val pl = DannyShop.instance()
//        sender.pluginMsg("&d1/3&7: Saving current data...")
//        save(sender)
//        sender.pluginMsg("&d2/3&7: Migrating data to &e$toBackend&7...")
//        val newProvider = migrate(sender, toBackend)
//        if (newProvider == null) {
//            sender.pluginMsg("&cFailed to migrate.")
//            return
//        }
//
//        pl.backend = newProvider
//        sender.pluginMsg("&d3/3&7: Switch complete. Now using &e${newProvider.type()}&7 for the backend.")
//    }
}