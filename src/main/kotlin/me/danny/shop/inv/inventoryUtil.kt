package me.danny.shop.me.danny.shop.inv

import org.bukkit.*
import org.bukkit.inventory.*

internal fun Inventory.fill(filler: ItemStack) {
    (0 until size).forEach { setItem(it, filler) }
}

internal fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)

internal fun ItemStack.setName(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name.color())
    itemMeta = meta
}