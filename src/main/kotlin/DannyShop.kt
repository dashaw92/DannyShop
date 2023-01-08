package me.danny.shop

import me.danny.shop.commands.ImportCommand
import me.danny.shop.data.Category
import me.danny.shop.data.DannyShopLoadables
import me.danny.shop.data.Demo.items
import me.danny.shop.data.Item
import me.danny.shop.data.Shop
import me.danny.shop.inv.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
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
            sender.sendMessage("Usage: /dannyshop <test/open>")
            return true
        }

        when (args.first().lowercase()) {
            "test" -> {
                buildSampleShop()
                sender.sendMessage("Done! Check config!")
            }

            "open" -> ShopMenu(SHOP, sender)
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

    private fun buildSampleShop() {
        Shop.CATEGORIES.clear()

        val categories = listOf(
            Category("Ores", Material.DEEPSLATE_GOLD_ORE),
            Category("Nature", Material.PODZOL),
            Category("Wood", Material.SPRUCE_PLANKS),
            Category("Redstone", Material.REDSTONE_BLOCK),
            Category("Building", Material.BIRCH_DOOR),
            Category("Food", Material.SWEET_BERRIES),
        )
        Shop.CATEGORIES.addAll(categories)

        val items2 = items.map {
            val out: MutableList<Item> = mutableListOf()
            for(category in categories) {
                val newItem = it.copy(category = category)
                out += newItem
            }
            out
        }

        val map: MutableMap<Category, MutableList<Item>> = mutableMapOf()
        items2
            .flatten()
            .shuffled()
            .groupByTo(map) { it.category }
        SHOP = Shop(map)

        DannyShopLoadables.saveShop(SHOP)
    }
}