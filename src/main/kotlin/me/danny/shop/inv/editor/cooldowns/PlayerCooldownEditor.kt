package me.danny.shop.inv.editor.cooldowns

import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.view.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.persistence.*
import java.util.*

private val idKey = Key("id_key", PersistentDataType.STRING)

class PlayerCooldownEditor(uuid: UUID) : MenuView {

    private val player = Bukkit.getPlayer(uuid)!!
    private val listing = CooldownList(player)
    private var editMode = false

    override fun onOpen(): ViewAction = ViewAction.Resize(6, "&7- &5Editing &d${player.name}")

    override fun refresh(inv: Inventory) {
        listing.render(inv)
    }

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        listing.render(inv)

        if (editMode) {
            inv.setItem(
                inv.size - 8, ItemBuilder.makeItem(
                    Material.FLINT_AND_STEEL, "&cReset all",
                    "&4Clears all cooldowns"
                )
            )
        }

        inv.setItem(
            inv.size - 2, if (editMode) {
                ItemBuilder.makeItem(Material.REDSTONE_TORCH, "&9Edit mode: &6On")
            } else {
                ItemBuilder.makeItem(Material.SOUL_TORCH, "&9Edit mode: &7Off")
            }
        )
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(CooldownEditorMainView())
        if (event.slot == inv.size - 2) {
            editMode = !editMode
            build(inv)
            return ViewAction.Pass
        }

        val viewer = event.whoClicked as Player
        val clicked = event.currentItem!!

        if (editMode) {
            if (clicked.hasKey(idKey)) {
                val iid = clicked.keyValue(idKey)!!
                player.cooldowns().resetCooldown(ID(iid))
                viewer.sendMessage("&6[DannyShop] &7Reset cooldown on item &e$iid&7 for &e${player.name}&7.".color())
                build(inv)
                return ViewAction.Pass
            }

            if (clicked.type == Material.FLINT_AND_STEEL) {
                player.cooldowns().resetAll()
                viewer.sendMessage("&6[DannyShop] &7Reset all cooldowns on items for &e${player.name}&7.".color())
                build(inv)
                return ViewAction.Pass
            }
        }

        listing.onClick(event) { refresh(inv) }
        return ViewAction.Pass
    }

    private inner class CooldownList(private val player: Player) :
        Page<Pair<ID, Long>>(player.cooldowns().toList(), 1 to 1, 7 to 4, 43 to 44) {
        override fun display(inv: Inventory) {
            items = player.cooldowns().toList()
                .filterNot { (id, _) ->
                    val expiration = CooldownHandler.getCooldownTime(player, id)
                    expiration is Expiration.None
                }
            val icons = items.drop(page * size).take(size)
                .map(::makeIcon)

            var invIdx = start.second * 9 + start.first

            for (icon in icons) {
                inv.setItem(invIdx, icon)
                invIdx += 1
                if (invIdx % 9 == 0) invIdx += 2
            }
        }

        private fun makeIcon(cooldown: Pair<ID, Long>): ItemStack {
            val item = DannyShop.SHOP.itemByIid(cooldown.first)!!
            val expiration = CooldownHandler.getCooldownTime(player, item.iid)
            val timestamp = Date(cooldown.second)

            val name = item.name ?: "&e${item.iid.id}"
            val display = ItemBuilder.makeItem(
                item.item.display().type, name,
                "&9ID: &7${item.iid.id}",
                "&9Category:",
                " &3Name: &7${item.category.name}",
                " &3CID: &7${item.category.cid.id}",
                "",
                "&9Expiration:",
                when (expiration) {
                    is Expiration.Never -> "&4Never"
                    is Expiration.Future -> {
                        val formatted = expiration.format().joinToString(" ").ifBlank { "<1s" }
                        "&7$formatted"
                    }

                    else -> "&2Not on cooldown"
                },
                "",
                "&9Last purchased:",
                "&7$timestamp",
                *if (editMode) {
                    arrayOf(
                        "",
                        "&c[Reset: Click]"
                    )
                } else {
                    arrayOf()
                }
            ).attachKey(idKey, item.iid.id)

            return display
        }
    }
}