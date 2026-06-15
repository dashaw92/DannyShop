package me.danny.shop.data

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Wrap over a Namespaced key to avoid using the data type argument everywhere
 */
internal data class Key<T, Z>(val key: NamespacedKey, val type: PersistentDataType<T, Z>) {
    constructor(key: String, type: PersistentDataType<T, Z>) : this(NamespacedKey("dannyshop", key), type)
}

internal fun <T : Any, Z : Any> ItemStack.keyValue(key: Key<T, Z>): Z? {
    val meta = itemMeta!!
    return meta.persistentDataContainer.get(key.key, key.type)
}

internal fun <T : Any, Z : Any> ItemStack.attachKey(key: Key<T, Z>, value: Z): ItemStack {
    val meta = itemMeta!!
    meta.persistentDataContainer.set(key.key, key.type, value)
    itemMeta = meta
    return this
}

internal fun <T : Any, Z : Any> ItemStack.hasKey(key: Key<T, Z>): Boolean =
    itemMeta!!.persistentDataContainer.has(key.key, key.type)

internal fun ItemStack.attachMarker(key: NamespacedKey): ItemStack {
    val meta = itemMeta!!
    meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
    itemMeta = meta
    return this
}

internal fun ItemStack.attachMarker(key: Key<Byte, Byte>): ItemStack = attachKey(key, 1)

internal fun ItemStack.hasMarker(key: NamespacedKey): Boolean =
    itemMeta!!.persistentDataContainer.has(key, PersistentDataType.BYTE)

internal fun ItemStack.hasMarker(key: Key<Byte, Byte>): Boolean = hasMarker(key.key)