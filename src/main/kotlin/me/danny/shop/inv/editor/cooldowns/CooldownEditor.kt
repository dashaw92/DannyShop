package me.danny.shop.inv.editor.cooldowns

import me.danny.shop.inv.*
import me.danny.shop.inv.LoreList.toEntry
import me.danny.shop.inv.editor.cooldowns.PlayerListing.SortMode
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*

class CooldownEditor(player: Player) : Menu(6, "- &5Cooldown Editor", player), RefreshPlease {

    private val playerPages = PlayerListing()

    init {
        build()
    }

    override fun refresh() {
        playerPages.render(inv)
    }

    override fun build() {
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
        inv.setItem(inv.size - 5, ItemBuilder.makeItem(Material.BARRIER, "&6Close"))
    }

    override fun onClick(event: InventoryClickEvent) {
        when (event.slot) {
            inv.size - 5 -> {
                viewer.closeInventory()
                return
            }

            inv.size - 8 -> {
                playerPages.sortMode = playerPages.sortMode.next()
                build()
            }
        }

        val clicked = event.currentItem!!
        if (clicked.hasKey(uuidKey)) {
            val uuid = clicked.keyValue(uuidKey)!!
            //TODO
        }
        playerPages.onClick(event, ::refresh)
    }
}