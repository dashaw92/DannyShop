package me.danny.shop.utils

import org.bukkit.*
import org.bukkit.inventory.*

internal fun Inventory.fill(filler: ItemStack) {
    (0..<size).forEach { setItem(it, filler) }
}

internal fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)

internal fun ItemStack.setName(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name.color())
    itemMeta = meta
}