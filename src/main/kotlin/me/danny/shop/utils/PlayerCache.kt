package me.danny.shop.utils

import org.bukkit.Bukkit
import java.util.*

internal object PlayerCache {
    private val cachedNames: MutableMap<UUID, String> = mutableMapOf()

    fun getNameOrUUID(uuid: UUID): String = cachedNames.computeIfAbsent(uuid) {
        Bukkit.getOfflinePlayer(it).name ?: it.toString()
    }

}