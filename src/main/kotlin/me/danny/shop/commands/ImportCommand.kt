package me.danny.shop.commands

import me.danny.shop.Perm
import me.danny.shop.askInput
import me.danny.shop.collapse
import me.danny.shop.data.attachMarker
import me.danny.shop.data.hasMarker
import me.danny.shop.importing.ImportSession
import me.danny.shop.input.Input
import me.danny.shop.pluginMsg
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

internal object ImportCommand {

    @Suppress("DEPRECATION")
    private val WAND_KEY = NamespacedKey("dannyshop", "wand_item")
    private val IMPORT_WAND = ItemBuilder.addEnchantGlow(
        ItemBuilder.addAttribute(
            ItemBuilder.makeItem(
                Material.WOODEN_HOE, "&3DannyShop Import Wand",
                "&eLeft click&7 a &6named chest&7 to import the items",
                "&7in the chest with default options.",
                "",
                "&4Warning: &7Once imported, the chest will be emptied",
                "&3[Permission: &8&o${Perm.ADMIN}&3]",
            ), *ItemFlag.entries.toTypedArray()
        )
    ).attachMarker(WAND_KEY)

    fun isWand(item: ItemStack) = item.hasMarker(WAND_KEY)

    fun onCommand(player: Player, args: Array<out String>) {
        if (!player.hasPermission(Perm.ADMIN)) {
            player.sendMessage("&cYou lack permission.".color())
            return
        }

        if (args.isEmpty()) {
            for (i in player.inventory.contents.indices) {
                val item = player.inventory.getItem(i) ?: continue
                if (item.type.isAir) continue
                if (isWand(item)) {
                    player.pluginMsg("&cYou already have an import wand!")
                    return
                }
            }

            player.inventory.addItem(IMPORT_WAND)
            player.pluginMsg("&7Import wand given! Info on the wand's tooltip!".color())
            return
        }

        if (!ImportSession.isInSession(player.uniqueId)) return
        when (args[0].lowercase()) {
            "set" -> {
                if (args.size != 3) return
                val id = args[1]
                val prop = args[2]

                player.closeInventory()
                askInput("&9Set $prop")
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