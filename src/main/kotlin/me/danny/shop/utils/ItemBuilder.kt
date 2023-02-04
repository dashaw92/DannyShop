package me.danny.shop.utils

import org.bukkit.*
import org.bukkit.enchantments.*
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.*
import org.bukkit.potion.*
import java.util.*


/**
 * Convenience functions for building custom items
 */
internal object ItemBuilder {
    fun makeItem(mat: Material, name: String, vararg lore: String?): ItemStack {
        return makeItem(mat, 1, name, *lore)
    }

    fun makeItem(mat: Material, amount: Int, name: String, vararg lore: String?): ItemStack {
        var item = ItemStack(mat, amount)
        val im = item.itemMeta!!
        im.setDisplayName(name.color())
        item.itemMeta = im
        if (lore.isNotEmpty()) item = setLore(item, *lore)
        return item
    }

    fun addLore(item: ItemStack, vararg lore: String): ItemStack {
        val original = item.itemMeta!!.lore
        val current = original?.toMutableList() ?: mutableListOf()
        Arrays.stream(lore)
            .map(String::color)
            .forEach(current::add)
        val clone = item.clone()
        val meta = clone.itemMeta!!
        meta.lore = current
        clone.itemMeta = meta
        return clone
    }

    private fun setLore(item: ItemStack, vararg lore: String?): ItemStack {
        val clone = item.clone()
        val im = item.itemMeta!!
        val colored: List<String> = lore
            .mapNotNull { it?.color() }
        im.lore = colored
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

    fun addAttribute(item: ItemStack, vararg hideAttributes: ItemFlag): ItemStack {
        val clone = item.clone()
        val im = clone.itemMeta!!
        im.addItemFlags(*hideAttributes)
        clone.itemMeta = im
        return clone
    }

    fun setName(item: ItemStack, name: String): ItemStack {
        val clone = item.clone()
        val im = clone.itemMeta!!
        im.setDisplayName(name.color())
        clone.itemMeta = im
        return clone
    }
}