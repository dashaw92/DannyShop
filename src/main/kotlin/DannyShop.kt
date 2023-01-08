package me.danny.shop

import me.danny.shop.commands.ImportCommand
import me.danny.shop.data.DannyShopLoadables
import me.danny.shop.data.Shop
import me.danny.shop.inv.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class DannyShop : JavaPlugin() {
    companion object {
        lateinit var SHOP: Shop
            private set
    }

    override fun onEnable() {
        SHOP = DannyShopLoadables.loadShop(this)
        Bukkit.getPluginManager().registerEvents(MenuListener, this)
        Bukkit.getPluginManager().registerEvents(ImportListener, this)
    }

    override fun onDisable() {
        DannyShopLoadables.saveShop(SHOP)
        Menu.closeOpenInvs()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(label.lowercase() != "dannyshop") return true
        if(sender !is Player) {
            sender.sendMessage("${ChatColor.RED}Nope!")
            return true
        }

        if(args.isEmpty()) {
            ShopMenu(SHOP, sender)
            return true
        }

        when (args.first().lowercase()) {
            "import" -> ImportCommand.onCommand(sender, args.sliceArray(2 until args.size))
            "catedit" -> {
                if (!sender.hasPermission("dannyshop.admin")) {
                    sender.sendMessage("${ChatColor.RED}You lack permission.")
                    return true
                }

                CategoryEditor(sender)
            }
        }
        return true
    }
}