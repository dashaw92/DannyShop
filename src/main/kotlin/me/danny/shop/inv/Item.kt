package me.danny.shop.inv

import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import java.util.*
import java.util.stream.Collectors


object Item {
    fun makeItem(mat: Material, name: String, vararg lore: String?): ItemStack {
        return makeItem(mat, 1, name, *lore)
    }

    fun makeItem(mat: Material, amount: Int, name: String, vararg lore: String?): ItemStack {
        var item = ItemStack(mat, amount)
        val im = item.itemMeta!!
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name))
        item.itemMeta = im
        if (lore.isNotEmpty()) item = setLore(item, *lore)
        return item
    }

    fun addLore(item: ItemStack, vararg lore: String): ItemStack {
        val original = item.itemMeta!!.lore
        val current = original?.toMutableList() ?: mutableListOf()
        Arrays.stream(lore)
            .map { line -> ChatColor.translateAlternateColorCodes('&', line) }
            .forEach(current::add)
        val clone = item.clone()
        val meta = clone.itemMeta!!
        meta.lore = current
        clone.itemMeta = meta
        return clone
    }

    fun setLore(item: ItemStack, vararg lore: String?): ItemStack {
        val clone = item.clone()
        val im = item.itemMeta!!
        val colored: List<String> = Arrays.stream(lore)
            .map { line -> ChatColor.translateAlternateColorCodes('&', line) }
            .collect(Collectors.toList())
        im.lore = colored
        clone.itemMeta = im
        return clone
    }

    fun setDisplayName(item: ItemStack, name: String): ItemStack {
        val clone = item.clone()
        val im = item.itemMeta!!
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name))
        clone.itemMeta = im
        return clone
    }

    fun addEnchantGlow(item: ItemStack): ItemStack {
        val clone = item.clone()
        val im = item.itemMeta!!
        im.addItemFlags(*ItemFlag.values())
        clone.itemMeta = im
        clone.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
        return clone
    }

    fun makeTippedArrow(name: String, type: PotionType, vararg lore: String?): ItemStack {
        val arrow: ItemStack = makeItem(Material.TIPPED_ARROW, 1, name, *lore)
        val pm = arrow.itemMeta as PotionMeta?
        pm!!.addItemFlags(*ItemFlag.values())
        pm.basePotionData = PotionData(type)
        arrow.itemMeta = pm
        return arrow
    }

    fun hasMarker(item: ItemStack, key: NamespacedKey): Boolean = hasKey(item, key, PersistentDataType.BYTE)

    fun attachMarker(item: ItemStack, key: NamespacedKey): ItemStack = attachKey(item, key, PersistentDataType.BYTE, 1)

    fun <T, Z : Any> hasKey(item: ItemStack, key: NamespacedKey, type: PersistentDataType<T, Z>): Boolean {
        val im = item.itemMeta!!
        return im.persistentDataContainer.has(key, type)
    }

    fun <T, Z : Any> attachKey(
        item: ItemStack,
        key: NamespacedKey,
        type: PersistentDataType<T, Z>,
        value: Z
    ): ItemStack {
        val clone = item.clone()
        val im = clone.itemMeta!!
        im.persistentDataContainer.set(key, type, value)
        clone.itemMeta = im
        return clone
    }

    fun <T, Z : Any> getValue(item: ItemStack, key: NamespacedKey, type: PersistentDataType<T, Z>, default: Z): Z {
        val im = item.itemMeta!!
        return im.persistentDataContainer.getOrDefault(key, type, default)
    }

    fun getPotionType(arrow: ItemStack): PotionType {
        val im = arrow.itemMeta
        return if (im !is PotionMeta) PotionType.AWKWARD else im.basePotionData.type //awkward...
    }
}