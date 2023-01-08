package me.danny.shop.me.danny.shop.inv

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

fun Inventory.fill(filler: ItemStack) {
    (0 until size).forEach { setItem(it, filler) }
}