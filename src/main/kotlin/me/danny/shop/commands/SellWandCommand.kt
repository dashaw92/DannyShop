package me.danny.shop.commands

import me.danny.shop.Perm
import me.danny.shop.data.attachMarker
import me.danny.shop.data.hasMarker
import me.danny.shop.pluginMsg
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

internal object SellWandCommand {
    @Suppress("DEPRECATION")
    private val WAND_KEY = NamespacedKey("dannyshop", "sell_wand_item")
    private val SELL_WAND = ItemBuilder.addEnchantGlow(
        ItemBuilder.addAttribute(
            ItemBuilder.makeItem(
                Material.ORANGE_CARPET, "&3DannyShop Sell Wand",
                "&eRight click&7 a &6container&7 to sell all items",
                "&7in the chest.",
                "",
                "&3[Permission: &8&o${Perm.SELL_WAND}&3]",
            ), *ItemFlag.entries.toTypedArray()
        )
    ).attachMarker(WAND_KEY)

    fun isWand(item: ItemStack) = item.hasMarker(WAND_KEY)

    fun onCommand(player: Player, args: Array<out String>) {
        if (!player.hasPermission(Perm.SELL_WAND)) {
            player.sendMessage("&cYou lack permission.".color())
            return
        }

        for (i in player.inventory.contents.indices) {
            val item = player.inventory.getItem(i) ?: continue
            if (item.type.isAir) continue
            if (isWand(item)) {
                player.pluginMsg("&cYou already have a sell wand!")
                return
            }
        }

        player.inventory.addItem(SELL_WAND)
        player.pluginMsg("&7Sell wand given! Info on the wand's tooltip!".color())
    }
}