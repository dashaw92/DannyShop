package me.danny.shop.me.danny.shop.data

import org.bukkit.*
import org.bukkit.inventory.*
import org.bukkit.persistence.*

/**
 * Wrap over a Namespaced key to avoid using the data type argument everywhere
 */
data class Key<T, Z>(val key: NamespacedKey, val type: PersistentDataType<T, Z>) {
    @Suppress("DEPRECATION")
    constructor(key: String, type: PersistentDataType<T, Z>) : this(NamespacedKey("dannyshop", key), type)
}

fun <T, Z> ItemStack.keyValue(key: Key<T, Z>): Z? {
    val meta = itemMeta!!
    return meta.persistentDataContainer.get(key.key, key.type)
}

fun <T, Z> ItemStack.attachKey(key: Key<T, Z>, value: Z & Any): ItemStack {
    val meta = itemMeta!!
    meta.persistentDataContainer.set(key.key, key.type, value)
    itemMeta = meta
    return this
}

fun <T, Z> ItemStack.hasKey(key: Key<T, Z>): Boolean = itemMeta!!.persistentDataContainer.has(key.key, key.type)

fun ItemStack.attachMarker(key: NamespacedKey): ItemStack {
    val meta = itemMeta!!
    meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
    itemMeta = meta
    return this
}

fun ItemStack.attachMarker(key: Key<Byte, Byte>): ItemStack = attachKey(key, 1)

fun ItemStack.hasMarker(key: NamespacedKey): Boolean =
    itemMeta!!.persistentDataContainer.has(key, PersistentDataType.BYTE)

fun ItemStack.hasMarker(key: Key<Byte, Byte>): Boolean = hasMarker(key.key)