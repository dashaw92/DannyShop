package me.danny.shop.model

import me.danny.shop.Perm
import me.danny.shop.model.Item.ItemType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.Serializable

/**
 * Represents the DannyShop, holding all items
 * grouped by their respective categories
 */
data class Shop(val items: MutableMap<Category, MutableList<Item>>) {
    companion object {
        internal val CATEGORIES = mutableListOf<Category>()

        internal fun addCategory(category: Category): Boolean = CATEGORIES.add(category)
        fun getCategory(id: ID): Category? = CATEGORIES.find { it.cid == id }
        fun findCategoryByName(name: String): Category? = CATEGORIES.find { it.name == name }
    }

    /**
     * Add an item to the shop
     * Does nothing if the item represents a null or Air item
     *
     * Will create the category the item points to if it does not
     * exist
     */
    internal fun addItem(item: Item) {
        when (item.item) {
            is ItemType.Mat -> if (item.item.material.isAir) return
            is ItemType.Item -> if (item.item.item.type.isAir) return
        }

        items.computeIfAbsent(item.category) { mutableListOf() } += item
    }

    /**
     * Replace the item by the ID with the [replacement]
     * If there is no Item with the ID, this does nothing
     *
     * Care is taken to ensure the replacement item takes
     * the same slot as the old item, so ordering should
     * not be impacted by this method.
     */
    internal fun replaceItem(id: ID, replacement: Item) {
        val old = itemByIid(id.id) ?: return
        val items = items.entries.find { (key, _) -> key.cid == old.category.cid }?.value ?: return
        if (old.category.cid != replacement.category.cid) {
            items.removeAll { it.iid.id == old.iid.id }
            addItem(replacement)
        } else {
            val idx = items.indexOf(old)
            items[idx] = replacement
        }
    }

    /**
     * All categories
     */
    fun categories(): Collection<Category> = CATEGORIES

    /**
     * Get all items belonging to the category with ID [cid]
     */
    fun items(cid: ID): Collection<Item> =
        items.entries.find { (key, _) -> key.cid.id == cid.id }?.value ?: listOf()

    /**
     * Does the shop contain any items?
     */
    fun isEmpty(): Boolean = items.values.flatten().isEmpty()

    /**
     * Find an item by IID
     */
    fun itemByIid(iid: String): Item? = items.values.flatten().find { it.iid.id == iid }

    /**
     * Find an item by IID
     */
    fun itemByIid(iid: ID): Item? = items.values.flatten().find { it.iid == iid }

    /**
     * Remove the category with ID [cid]
     * This will also delete all items belonging to that category
     */
    //XXX: This might have a bug? I encountered items not being deleted...
    internal fun deleteCategory(cid: ID) {
        val category = getCategory(cid) ?: return
        items.remove(category)
        CATEGORIES.remove(category)
        category.deleted = true
    }

    fun deleteItem(item: ID) {
        val item = itemByIid(item) ?: return
        items[item.category]?.removeIf { it.iid == item.iid }
    }

    internal fun sellableItems(subject: Player, category: ID?): List<Item> = items
        .filterKeys { it.isVisible(subject) }
        .filterKeys { category == null || it.cid == category }
        .values.flatten()
        .filter { it.cost is Item.Cost.Value }
}

/**
 * A unique String identifying this object
 * The only constraint placed on IDs is that
 * they are unique. Duplicate IDs will cause
 * undefined behavior.
 */
data class ID(internal val id: String) : Serializable {
    companion object {
        /**
         * Systematically generate a new IID based off the current
         * time and a pRNG.
         */
        internal fun generate(): ID {
            System.nanoTime()
            val now = System.currentTimeMillis()
            val salt = (Math.random() * 1000).toInt()
            return ID("%x%x".format(now, salt))
        }
    }
}

/**
 * Categories are how items are grouped in DannyShop
 * It does not matter what they are called, as long as the name
 * fits inside an ItemStack's display name and there are no other
 * categories with the same name.
 *
 * Categories are entirely user-defined, there are no builtin categories.
 */
data class Category(val cid: ID, var name: String, var permission: String?, var display: Material) {

    internal var deleted = false

    /**
     * Helper for generating a CID for the category
     */
    constructor(name: String, display: Material) : this(ID.generate(), name, null, display)

    /**
     * Change the name of this category
     */
    internal fun changeName(name: String) {
        this.name = name
    }

    /**
     * Apply a permission to the category.
     * Players without this permission will not be able
     * to see or purchase from this category.
     */
    internal fun setPermission(permission: String?) {
        this.permission = permission
    }

    /**
     * Change the icon used to represent this category
     */
    internal fun changeDisplay(display: Material) {
        this.display = display
    }

    /**
     * Check if the player has permission to view
     * this category.
     * If no permission is set, this will return true
     */
    fun isVisible(player: Player): Boolean {
        if (deleted) return false
        if (permission == null) return true
        return player.hasPermission(permission!!) || player.hasPermission(Perm.ADMIN)
    }
}

/**
 * Represents a purchasable item in DannyShop.
 * An Item is composed of:
 * * ID - Unique ID to identify an item
 * * Name - Optional name used for searching for an item
 * * ItemType - Holder for one of several variants of item types
 * * Cost - How much the item buys for per unit
 * * Cooldown - How often a player may purchase the item
 * * Quantities - Details about sellable item quantities
 * * Category ID - The group this item will belong to
 *
 * Instances of this should NOT be held onto.
 * Use the item's IID to fetch it from the Shop.
 * Instances of this class may be outdated otherwise.
 */
data class Item(
    val iid: ID,
    val name: String? = null,
    val item: ItemType,
    val cost: Cost,
    val sellLimit: SellLimit,
    val category: Category
) {
    /**
     * Models all possible sellable item types DannyShop supports:
     * * Material - A basic material ItemStack with no attached data
     * * Item - A custom ItemStack with arbitrary attached data
     * * Exp - Experience that will be granted to the player on purchase
     * * Command - Commands that will target the player on purchase
     */
    sealed interface ItemType {
        /**
         * Supports only raw materials with no extra attached data
         */
        data class Mat(val material: Material) : ItemType {
            override fun inner(): Any = material
            override fun display(): ItemStack = ItemStack(material, 1)
        }

        /**
         * Represents a complex item (an item with extra data), such as items with:
         * * Custom names
         * * Custom lore
         * * Enchantments
         * * Persistent data keys
         * * Potion data
         * and more.
         */
        data class Item(val item: ItemStack) : ItemType {
            override fun inner(): Any = item
            override fun display(): ItemStack = item.clone()
        }

        fun inner(): Any
        fun display(): ItemStack
    }

    /**
     * Simple class wrapping the purchase value of an item
     */
    sealed interface Cost {
        /**
         * The price for this item is not set.
         * Items with this Cost are not purchasable.
         */
        data object NotSet : Cost

        data class Value(val buy: Double) : Cost
    }

    sealed interface SellLimit {
        object None : SellLimit

        data class Amount(val amount: UInt) : SellLimit
    }

    fun matchesItemStack(stack: ItemStack): Boolean = when (item) {
        is ItemType.Mat -> item.material == stack.type
        is ItemType.Item -> {
            val self = item.item
            return self.isSimilar(stack)

//            if (self.type != stack.type) return false
//            if (self.enchantments.size != stack.enchantments.size) return false
//            if (self.enchantments != stack.enchantments) return false
//            if (self.hasItemMeta()) {
//                if (!stack.hasItemMeta()) return false
//                val myMeta = self.itemMeta!!
//                val otherMeta = stack.itemMeta!!
//
//                val name =
//                    if (myMeta.hasDisplayName()) otherMeta.hasDisplayName() && myMeta.displayName == otherMeta.displayName else true
//                val lore = if (myMeta.hasLore()) otherMeta.hasLore() && myMeta.lore == otherMeta.lore else true
//
//                if (!name || !lore) return false
//            }
//
//            return true
        }
    }

    internal fun itemName(): String = when (item) {
        is ItemType.Mat -> humanize(item.material.name)
        is ItemType.Item -> name ?: humanize(item.item.type.name)
    }

    private fun humanize(name: String): String =
        name.split('_').joinToString(" ") { word -> word.first().uppercaseChar() + word.substring(1).lowercase() }
}