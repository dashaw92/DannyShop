package me.danny.shop.utils

import me.danny.shop.data.Key
import me.danny.shop.data.attachKey
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

internal object PlayerCache {
    private val cachedSkulls: MutableMap<UUID, ItemStack> = mutableMapOf()
    private val cachedNames: MutableMap<UUID, String> = mutableMapOf()

    val uuidKey = Key("skull_uuid", PersistentDataType.STRING)

    fun getNameOrUUID(uuid: UUID): String = cachedNames.computeIfAbsent(uuid) {
        Bukkit.getOfflinePlayer(it).name ?: it.toString()
    }

    fun getSkull(id: UUID): ItemStack = cachedSkulls.computeIfAbsent(id) {
        val player = Bukkit.getOfflinePlayer(id)
        val skull = ItemBuilder.makeItem(
            Material.PLAYER_HEAD, "&f${player.name}"
        ).attachKey(uuidKey, id.toString())

        val meta = skull.itemMeta!! as SkullMeta
        meta.owningPlayer = player
        skull.itemMeta = meta
        skull
    }
}