package me.danny.shop.commands

import me.danny.shop.inv.ItemBuilder
import me.danny.shop.me.danny.shop.data.attachMarker
import me.danny.shop.me.danny.shop.data.hasMarker
import me.danny.shop.me.danny.shop.inv.color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ImportCommand {

    @Suppress("DEPRECATION")
    private val WAND_KEY = NamespacedKey("dannyshop", "wand_item")
    private val IMPORT_WAND = ItemBuilder.makeItem(
        Material.WOODEN_HOE, "&3DannyShop Import Wand",
        "&eLeft click a named chest to import the items",
        "&ein the chest with default options.",
        "&eWorth will be determined by Essentials' &dworth.yml",
        "&eIf Essentials is not on the server, you will",
        "&ehave to manually do this!"
    ).attachMarker(WAND_KEY)

    fun isWand(item: ItemStack) = item.hasMarker(WAND_KEY)

    @Suppress("UNUSED_PARAMETER")
    fun onCommand(player: Player, args: Array<out String>) {
        if (!player.hasPermission("dannyshop.import")) {
            player.sendMessage("&cYou lack permission.".color())
            return
        }

        player.inventory.addItem(IMPORT_WAND)
        player.sendMessage("&6[DannyShop] Import wand given! Info on the wand's tooltip!".color())
    }
}