package me.danny.shop.commands

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.inv.editor.categories.CategoryEditor
import me.danny.shop.inv.editor.cooldowns.CooldownEditor
import me.danny.shop.me.inv.shop.ShopMenu
import me.danny.shop.pluginMsg
import me.danny.shop.utils.color
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
            requirePlayer(sender) {
                ShopMenu(it)
            }
            return true
        }

        val cmdArgs = args.sliceArray(1 until args.size)
        when (args.first().lowercase()) {
            "import" -> requireAdminPlayer(sender) {
                ImportCommand.onCommand(it, cmdArgs)
            }

            "catedit" -> requireAdminPlayer(sender, ::CategoryEditor)
            "cooldowns" -> requireAdminPlayer(sender, ::CooldownEditor)

            "backend" -> requireAdmin(sender) {
                BackendAdminCommand.onCommand(it, cmdArgs)
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
        if (args.size != 1) return null
        if (sender.hasPermission(Perm.ADMIN)) return mutableListOf("import", "catedit", "cooldowns", "backend")
        return null
    }

    private fun requireAdminPlayer(sender: CommandSender, func: (Player) -> Unit) {
        requireAdmin(sender) {
            requirePlayer(it, func)
        }
    }

    private fun requireAdmin(sender: CommandSender, func: (CommandSender) -> Unit) {
        if (!sender.hasPermission(Perm.ADMIN)) {
            sender.pluginMsg("&cYou lack permission.")
            return
        }
        func(sender)
    }

    private fun requirePlayer(sender: CommandSender, func: (Player) -> Unit) {
        if (sender !is Player) {
            sender.pluginMsg("Sorry, but only players may use this command.".color())
            return
        }
        func(sender)
    }
}