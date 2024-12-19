package me.danny.shop.model

import me.danny.shop.Perm
import me.danny.shop.model.Item.ItemType
import me.danny.shop.utils.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
            else -> {}
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
}

/**
 * A unique String identifying this object
 * The only constraint placed on IDs is that
 * they are unique. Duplicate IDs will cause
 * undefined behavior.
 */
data class ID(internal val id: String) {
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
    val cooldown: Cooldown,
    val quantities: Quantities,
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
            override fun display(): ItemStack = item
        }

        /**
         * Represents enchanting experience the player will receive on purchase
         */
        data class Exp(val exp: Int) : ItemType {
            override fun inner(): Any = exp
            override fun display(): ItemStack = ItemBuilder.makeItem(
                Material.EXPERIENCE_BOTTLE,
                "${ChatColor.GOLD}%,d Experience".format(exp)
            )
        }

        /**
         * Represents a command that will be executed by console on purchase.
         * Commands support a few keywords to enable interpolation of special values:
         * - $PLAYER - replaced with the purchasing player's raw name.
         *   ```
         *   "/say Hello $PLAYER" = "/say Hello Theano"
         *   ```
         *
         * - $UUID - replaced with the purchasing player's UUID.
         *   ```
         *   "/lp user $UUID info" = "/lp user cfa295f9-e01f-44f5-93a2-2e4271d7e015 info"
         *   ```
         *
         * - $UUID_NO_DASHES - replaced with the purchasing player's UUID with dashes removed.
         *   Same as above, but with no dashes in the UUID
         */
        data class Command(val command: String) : ItemType {
            override fun inner(): Any = command
            override fun display(): ItemStack =
                ItemBuilder.makeItem(Material.COMMAND_BLOCK, "${ChatColor.BLUE}Run command", command)
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

    /**
     * How often an item may be purchased
     */
    sealed interface Cooldown {
        companion object {
            internal fun parse(serialized: String): Cooldown {
                return when (val cooldown = serialized.lowercase()) {
                    "none" -> None
                    "infinite" -> Infinite
                    else -> {
                        if (cooldown.trim().isBlank()) return None
                        else {
                            val time = cooldown.takeWhile(Char::isDigit).toLongOrNull()
                            val unit = cooldown.takeLastWhile(Char::isLetter)

                            if (time == null) return None
                            if (unit.isBlank()) return None

                            val ctor: (Long) -> Time = when (unit.lowercase()) {
                                "ms" -> Cooldown.Time::Millis
                                "s" -> Cooldown.Time::Seconds
                                "m" -> Cooldown.Time::Minutes
                                "h" -> Cooldown.Time::Hours
                                "d" -> Cooldown.Time::Days
                                "w" -> Cooldown.Time::Weeks
                                "mo" -> Cooldown.Time::Months
                                "y" -> Cooldown.Time::Years
                                else -> return None
                            }

                            Duration(ctor(time))
                        }
                    }
                }
            }
        }

        /**
         * The item has no cooldown and can be purchased as often as wanted
         */
        data object None : Cooldown

        /**
         * This item can only be purchased one time
         */
        data object Infinite : Cooldown

        /**
         * Once the item is purchased, the player must wait this amount of time
         * before they may purchase it again
         */
        data class Duration(val time: Time) : Cooldown

        /**
         * The supported units of time Cooldown recognizes
         */
        sealed class Time(internal val time: Long, private val suffix: String) {
            data class Millis(val millis: Long) : Time(millis, "ms")
            data class Seconds(val seconds: Long) : Time(seconds, "s")
            data class Minutes(val minutes: Long) : Time(minutes, "m")
            data class Hours(val hours: Long) : Time(hours, "h")
            data class Days(val days: Long) : Time(days, "d")
            data class Weeks(val weeks: Long) : Time(weeks, "w")
            data class Months(val months: Long) : Time(months, "mo")
            data class Years(val years: Long) : Time(years, "y")

            fun display(): String = "$time$suffix"
            internal fun multiplier(): Long {
                return when (this) {
                    is Millis -> multipliers[Units.Ms]
                    is Seconds -> multipliers[Units.S]
                    is Minutes -> multipliers[Units.M]
                    is Hours -> multipliers[Units.H]
                    is Days -> multipliers[Units.D]
                    is Weeks -> multipliers[Units.W]
                    is Months -> multipliers[Units.Mo]
                    is Years -> multipliers[Units.Y]
                }!!
            }

            companion object {
                internal enum class Units {
                    Ms, S, M, H, D, W, Mo, Y
                }

                private val multipliers = mapOf(
                    Units.Y to 1000L * 60 * 60 * 24 * 7 * 4 * 12,
                    Units.Mo to 1000L * 60 * 60 * 24 * 7 * 4,
                    Units.W to 1000L * 60 * 60 * 24 * 7,
                    Units.D to 1000L * 60 * 60 * 24,
                    Units.H to 1000L * 60 * 60,
                    Units.M to 1000L * 60,
                    Units.S to 1000L,
                    Units.Ms to 1L
                )

                internal fun multiplier(unit: Units): Long = multipliers[unit]!!
            }
        }
    }

    /**
     * Permits customization of how much of an item can be purchased at once.
     * * predefined - A list of predefined quantities the item is available in
     * * allowed - What quantities are available for purchase
     */
    data class Quantities(val predefined: List<Int>, val allowed: Allowed) {
        enum class Allowed {
            /**
             * The player may enter any number (within reason)
             * to purchase that many units of the item.
             * The predefined list in this case serves only as a convenience.
             */
            Any,

            /**
             * The player can only purchase the item in amounts listed in the
             * predefined list, they cannot enter a custom amount.
             * The main purpose for this option is to support items that the player
             * can only purchase N of one time:
             * ```yaml
             * ...:
             *   cooldown: infinite
             *   quantities:
             *     predefined: [1]
             *     allowed: Predefined
             * ```
             */
            Predefined
        }
    }
}