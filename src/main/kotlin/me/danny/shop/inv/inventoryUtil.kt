package me.danny.shop.me.danny.shop.inv

import org.bukkit.ChatColor
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

fun Inventory.fill(filler: ItemStack) {
    (0 until size).forEach { setItem(it, filler) }
}

fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)

fun ItemStack.setName(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name.color())
    itemMeta = meta
}