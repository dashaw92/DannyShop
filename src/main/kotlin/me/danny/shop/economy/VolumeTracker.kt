package me.danny.shop.economy

import me.danny.shop.model.ID
import org.bukkit.entity.Player
import java.util.*

object VolumeTracker {
    private typealias VolumeMap = MutableMap<ID, Long>
    private val playerHistory: MutableMap<UUID, VolumeMap> = mutableMapOf()

    private fun playerMap(pl: Player): VolumeMap = playerHistory.computeIfAbsent(pl.uniqueId) { mutableMapOf() }

    operator fun get(player: Player, item: ID): Long = playerMap(player).computeIfAbsent(item) { 0 }
    operator fun set(player: Player, item: ID, amount: Long) {
        playerMap(player)[item] = amount.coerceAtLeast(0)
    }

    fun applyAll(remap: (ID) -> Long) {
        playerHistory.forEach { (_, history) ->
            history.replaceAll { id, _ -> remap(id) }
        }
    }
}