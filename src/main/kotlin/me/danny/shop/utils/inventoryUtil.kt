package me.danny.shop.utils

import net.md_5.bungee.api.ChatColor
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

internal fun Inventory.fill(filler: ItemStack) {
    (0..<size).forEach { setItem(it, filler) }
}

internal fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)
internal fun String.hex(): ChatColor = ChatColor.of(this)

internal fun ItemStack.setName(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name.color())
    itemMeta = meta
}