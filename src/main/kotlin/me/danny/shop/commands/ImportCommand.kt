package me.danny.shop.commands

import me.danny.libinput.providers.*
import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.importing.*
import me.danny.shop.inv.*
import me.danny.shop.model.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.*

internal object ImportCommand {

    @Suppress("DEPRECATION")
    private val WAND_KEY = NamespacedKey("dannyshop", "wand_item")
    private val IMPORT_WAND = ItemBuilder.addEnchantGlow(
        ItemBuilder.addAttribute(
            ItemBuilder.makeItem(
                Material.WOODEN_HOE, "&3DannyShop Import Wand",
                "&eLeft click&7 a &6named chest&7 to import the items",
                "&7in the chest with default options.",
                "&7Worth will be determined by Essentials' &6worth.yml",
                "&7If Essentials is not on the server, you will",
                "&7have to manually do this!",
                "",
                "&3[Permission: &8&o${Perm.ADMIN}&3]",
                "&4Warning: The chest will be cleared after importing!"
            ), *ItemFlag.values()
        )
    ).attachMarker(WAND_KEY)

    fun isWand(item: ItemStack) = item.hasMarker(WAND_KEY)

    fun onCommand(player: Player, args: Array<out String>) {
        if (!player.hasPermission(Perm.ADMIN)) {
            player.sendMessage("&cYou lack permission.".color())
            return
        }

        if (args.isEmpty()) {
            player.inventory.addItem(IMPORT_WAND)
            player.pluginMsg("&7 Import wand given! Info on the wand's tooltip!".color())
            return
        }

        if (!ImportSession.isInSession(player.uniqueId)) return
        when (args[0].lowercase()) {
            "set" -> {
                if (args.size != 3) return
                val id = args[1]
                val prop = args[2]

                val needsExtraLength = prop == "name"

                player.closeInventory()
                askInput("&9Set $prop", Material.SPRUCE_WALL_SIGN, needsExtraLength = needsExtraLength)
                    .getInput(player) { pl, input -> handlePropValue(pl, input, id, prop) }
            }

            "finish" -> {
                player.closeInventory()
                val session = ImportSession.getSession(player.uniqueId)!!
                session.importAll()
                ImportSession.delete(player.uniqueId)
            }
        }
    }

    private fun handlePropValue(player: Player, input: Input, id: String, prop: String) {
        val line = input.collapse()

        val session = ImportSession.getSession(player.uniqueId)!!
        session.updateItem(id, prop, line)
    }
}