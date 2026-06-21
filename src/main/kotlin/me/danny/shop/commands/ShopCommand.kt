package me.danny.shop.commands

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.inv.ecolog.EcoLogMenu
import me.danny.shop.inv.editor.categories.CategoryEditor
import me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.pluginMsg
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

internal object ShopCommand : CommandExecutor, TabCompleter {
    init {
        DannyShop.instance().getCommand("dannyshop")!!.tabCompleter = this
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            requirePlayer(sender, Perm.OPEN_SHOP) {
                ShopMenu(it)
            }
            return true
        }

        val cmdArgs = args.sliceArray(1 until args.size)
        when (args.first().lowercase()) {
            "import" -> requireAdminPlayer(sender) {
                ImportCommand.onCommand(it, cmdArgs)
            }

            "sell-wand" -> requirePlayer(sender, Perm.SELL_WAND) {
                SellWandCommand.onCommand(it, cmdArgs)
            }

            "edit-categories" -> requireAdminPlayer(sender, ::CategoryEditor)

            "backend" -> requireAdmin(sender) {
                BackendAdminCommand.onCommand(it, cmdArgs)
            }

            "reset-limits" -> requireAdmin(sender) {
                it.pluginMsg("&7Sell limits refreshed.")
                DannyShop.instance().startTask()
            }

            "eco-log" -> requiresPermission(sender, Perm.ECOLOG) {
                EcoLogCommand.onCommand(it, cmdArgs)
            }

            "eco-log-gui" -> requirePlayer(sender, Perm.ECOLOG) {
//                sender.pluginMsg("&cThis feature is W.I.P. and is disabled until further notice.")

                if (!DannyShop.instance().config.loggingEnabled) {
                    sender.pluginMsg("&cEconomy logging is not enabled.")
                    return@requirePlayer
                }

                EcoLogMenu(it)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        val available = mutableListOf<String>()
        if (sender.hasPermission(Perm.SELL_WAND)) available.add("sell-wand")
        if (sender.hasPermission(Perm.ECOLOG) && DannyShop.instance().config.loggingEnabled) {
            available.addAll(listOf("eco-log", "eco-log-gui"))
        }
        if (sender.hasPermission(Perm.ADMIN)) available.addAll(
            listOf(
                "import",
                "edit-categories",
                "backend",
                "reset-limits"
            )
        )

        if (args.isEmpty()) return available
        if (args.size == 1) return available.filter { cmd -> cmd.lowercase().startsWith(args[0].lowercase()) }
        if (args.size == 2 && args[0].equals("eco-log", true)) return EcoLogCommand.onTabComplete(sender, args[1])
        return null
    }

    private fun requireAdminPlayer(sender: CommandSender, func: (Player) -> Unit) {
        requireAdmin(sender) {
            requirePlayer(it, func = func)
        }
    }

    private fun requireAdmin(sender: CommandSender, func: (CommandSender) -> Unit) {
        if (!sender.hasPermission(Perm.ADMIN)) {
            sender.pluginMsg("&cYou don't have permission to do this.")
            return
        }
        func(sender)
    }

    private fun requiresPermission(sender: CommandSender, permission: String, func: (CommandSender) -> Unit) {
        if (!sender.hasPermission(permission)) {
            sender.pluginMsg("&cYou don't have permission to do this.")
            return
        }

        func(sender)
    }

    private fun requirePlayer(sender: CommandSender, permission: String? = null, func: (Player) -> Unit) {
        if (sender !is Player) {
            sender.pluginMsg("&cSorry, but only players may use this command.")
            return
        }

        if (permission != null && !sender.hasPermission(permission)) {
            sender.pluginMsg("&cYou don't have permission to do this.")
            return
        }

        func(sender)
    }
}