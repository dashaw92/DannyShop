package me.danny.shop.inv.editor.cooldowns

import me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.data.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.*
import org.bukkit.persistence.*

internal val uuidKey = Key("skull_uuid", PersistentDataType.STRING)

class PlayerListing : Page<Player>(Bukkit.getOnlinePlayers(), 1 to 1, 7 to 4, 43 to 44) {

    internal var sortMode = SortMode.Alphabetical

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

    enum class SortMode {
        Alphabetical {
            override fun sort(): Comparator<Player> = Comparator { a, b -> a.name.compareTo(b.name) }
        },
        Reverse {
            override fun sort(): Comparator<Player> = Comparator { a, b -> b.name.compareTo(a.name) }
        };

        abstract fun sort(): Comparator<Player>
    }
}