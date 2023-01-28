package me.danny.shop.commands

import me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.*

object ImportCommand {

    @Suppress("DEPRECATION")
    private val WAND_KEY = NamespacedKey("dannyshop", "wand_item")
    private val IMPORT_WAND = ItemBuilder.addEnchantGlow(
        ItemBuilder.addAttribute(
            ItemBuilder.makeItem(
                Material.WOODEN_HOE, "&3DannyShop Import Wand",
                "&6Left click&7 a &dnamed chest&7 to import the items",
                "&7in the chest with default options.",
                "&7Worth will be determined by Essentials' &dworth.yml",
                "&6If Essentials is not on the server&7, you will",
                "&7have to manually do this!",
                "",
                "&3[Permission: &8&odannyshop.import&3]",
                "&4Warning: The chest will be cleared after importing!"
            ), *ItemFlag.values()
        )
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