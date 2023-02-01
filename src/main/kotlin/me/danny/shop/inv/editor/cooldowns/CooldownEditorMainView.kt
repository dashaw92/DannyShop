package me.danny.shop.inv.editor.cooldowns

import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.LoreList.toEntry
import me.danny.shop.inv.view.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.*
import org.bukkit.persistence.*
import java.util.*

private val uuidKey = Key("skull_uuid", PersistentDataType.STRING)

class CooldownEditorMainView : MenuView {
    private val playerPages = PlayerListing()

    override fun onOpen(): ViewAction = ViewAction.Resize(6, "- &5Cooldown Editor")

    override fun refresh(inv: Inventory) {
        playerPages.render(inv)
    }

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        playerPages.render(inv)

        inv.setItem(
            inv.size - 8, ItemBuilder.makeItem(
                Material.HOPPER, "&9Sort:",
                *LoreList.makeList(
                    listOf(
                        SortMode.Alphabetical toEntry listOf("Sorting A-Z"),
                        SortMode.Reverse toEntry listOf("Sorting Z-A")
                    ), playerPages.sortMode
                )
            )
        )
        inv.setItem(inv.size - 5, ItemBuilder.makeItem(Material.BARRIER, "&eClose"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        val viewer = event.whoClicked as Player
        when (event.slot) {
            inv.size - 5 -> {
                viewer.closeInventory()
                return ViewAction.Pass
            }

            inv.size - 8 -> {
                playerPages.sortMode = playerPages.sortMode.next()
                build(inv)
            }
        }

        val clicked = event.currentItem!!
        if (clicked.hasKey(uuidKey)) {
            val uuid = UUID.fromString(clicked.keyValue(uuidKey) ?: return ViewAction.Pass)
            return ViewAction.ChangeView(PlayerCooldownEditor(uuid))
        }
        playerPages.onClick(event) { refresh(inv) }
        return ViewAction.Pass
    }

    private class PlayerListing : Page<Player>(Bukkit.getOnlinePlayers(), 1 to 1, 7 to 4, 43 to 44) {

        var sortMode = SortMode.Alphabetical

        override fun display(inv: Inventory) {
            items = Bukkit.getOnlinePlayers().sortedWith(sortMode.sort())
            val skulls = items.drop(page * size)
                .take(size)
                .map(::makeSkull)
            var invIdx = start.second * 9 + start.first
            for (skull in skulls) {
                inv.setItem(invIdx, skull)
                invIdx += 1
                if (invIdx % 9 == 0) invIdx += 2
            }
        }

        private fun makeSkull(player: Player): ItemStack {
            val skull = ItemBuilder.makeItem(
                Material.PLAYER_HEAD, "&e${player.name}",
                "&3[Edit: Click]"
            ).attachKey(uuidKey, player.uniqueId.toString())

            val meta = skull.itemMeta!! as SkullMeta
            meta.owningPlayer = player
            skull.itemMeta = meta
            return skull
        }
    }

    enum class SortMode {
        Alphabetical {
            override fun sort(): Comparator<Player> = Comparator { a, b -> b.name.compareTo(a.name) }
        },
        Reverse {
            override fun sort(): Comparator<Player> = Comparator { a, b -> a.name.compareTo(b.name) }
        };

        abstract fun sort(): Comparator<Player>
    }
}