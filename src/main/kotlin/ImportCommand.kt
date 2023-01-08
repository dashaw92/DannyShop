package me.danny.shop.commands

import me.danny.shop.inv.Item
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ImportCommand {

    @Suppress("DEPRECATION")
    private val WAND_KEY = NamespacedKey("dannyshop", "wand_item")
    private val IMPORT_WAND = Item.attachMarker(
        Item.makeItem(
            Material.WOODEN_HOE, "${ChatColor.DARK_AQUA}DannyShop Import Wand",
            "${ChatColor.YELLOW}Left click a named chest to import the items",
            "${ChatColor.YELLOW}in the chest with default options.",
            "${ChatColor.YELLOW}Worth will be determined by Essentials' ${ChatColor.LIGHT_PURPLE}worth.yml",
            "${ChatColor.YELLOW}If Essentials is not on the server, you will",
            "${ChatColor.YELLOW}have to manually do this!"
        ), WAND_KEY
    )

    fun isWand(item: ItemStack) = Item.hasMarker(item, WAND_KEY)

    @Suppress("UNUSED_PARAMETER")
    fun onCommand(player: Player, args: Array<out String>) {
        if (!player.hasPermission("dannyshop.import")) {
            player.sendMessage("${ChatColor.RED}You lack permission.")
            return
        }

        player.inventory.addItem(IMPORT_WAND)
        player.sendMessage("${ChatColor.GOLD}[DannyShop] Import wand given! Info on the wand's tooltip!")
    }
}