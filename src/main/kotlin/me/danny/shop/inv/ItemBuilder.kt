package me.danny.shop.inv

import me.danny.shop.me.danny.shop.inv.color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import java.util.*


/**
 * Convenience functions for building custom items
 * TODO: Maybe convert this to use extension functions?
 */
object ItemBuilder {
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
}