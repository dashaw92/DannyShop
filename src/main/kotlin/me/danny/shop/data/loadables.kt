package me.danny.shop.data

import me.danny.shop.data.Item.*
import me.danny.shop.data.Item.ItemType.*
import me.danny.shop.inv.*
import org.bukkit.*
import org.bukkit.configuration.file.*
import org.bukkit.entity.*
import org.bukkit.inventory.*
import org.bukkit.plugin.*
import org.spongepowered.configurate.*
import org.spongepowered.configurate.serialize.*
import org.spongepowered.configurate.yaml.*
import java.io.*
import java.lang.reflect.*

/**
 * Represents the DannyShop, holding all items
 * grouped by their respective categories
 */
data class Shop(val items: MutableMap<Category, MutableList<Item>>) {
    companion object {
        internal val CATEGORIES = mutableListOf<Category>()

        fun addCategory(category: Category): Boolean = CATEGORIES.add(category)
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
    fun addItem(item: Item) {
        when (item.item) {
            is Mat -> if (item.item.material.isAir) return
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
    fun replaceItem(id: ID, replacement: Item) {
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
    fun deleteCategory(cid: ID) {
        val category = getCategory(cid) ?: return
        items.remove(category)
        CATEGORIES.remove(category)
    }
}

/**
 * A unique String identifying this object
 * The only constraint placed on IDs is that
 * they are unique. Duplicate IDs will cause
 * undefined behavior.
 */
data class ID(val id: String) {
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
        if (permission == null) return true
        return player.hasPermission(permission!!) || player.hasPermission("dannyshop.admin")
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
        object NotSet : Cost {
            override fun toString(): String = "NotSet"
        }

        data class Value(val buy: Double) : Cost
    }

    /**
     * How often an item may be purchased
     */
    sealed interface Cooldown {

        /**
         * The item has no cooldown and can be purchased as often as wanted
         */
        object None : Cooldown {
            override fun toString(): String = "None"
        }

        /**
         * This item can only be purchased one time
         */
        object Infinite : Cooldown {
            override fun toString(): String = "Infinite"
        }

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
                enum class Units {
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

//<editor-fold desc="Type serializers">
object ItemStackSerializer : TypeSerializer<ItemStack> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): ItemStack {
        if (node == null) throw IllegalArgumentException("what")

        val map = node.string!!
        return YamlConfiguration.loadConfiguration(StringReader(map)).getItemStack("itemstack")!!
    }

    override fun serialize(type: Type?, obj: ItemStack?, node: ConfigurationNode?) {
        if (obj == null || node == null) return

        val yml = YamlConfiguration()
        yml.set("itemstack", obj)
        node.set(yml.saveToString())
    }


}

object ItemTypeSerializer : TypeSerializer<ItemType> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): ItemType {
        if (node == null) throw IllegalArgumentException("what")

        val itemType = node.node("type").string!!
        val obj = node.node("object")
        return when (itemType.lowercase()) {
            "material" -> Mat(obj.get(Material::class.java)!!)
            "item" -> ItemType.Item(obj.get(ItemStack::class.java)!!)
            "experience" -> Exp(obj.int)
            "command" -> Command(obj.string!!)
            else -> throw IllegalArgumentException("Unknown item type $itemType")
        }
    }

    override fun serialize(type: Type?, obj: ItemType?, node: ConfigurationNode?) {
        if (node == null || obj == null) return

        val typeNode = node.node("type")
        when (obj) {
            is Mat -> typeNode.set("material")
            is ItemType.Item -> typeNode.set("item")
            is Exp -> typeNode.set("experience")
            is Command -> typeNode.set("command")
        }

        node.node("object").set(obj.inner())
    }

}

object CostSerializer : TypeSerializer<Cost> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Cost {
        if (node == null) throw IllegalArgumentException("what")

        if (node.string == "not set") return Cost.NotSet

        val buy = node.node("buy").double
        return Cost.Value(buy)
    }

    override fun serialize(type: Type?, obj: Cost?, node: ConfigurationNode?) {
        if (obj == null || node == null) return

        when (obj) {
            is Cost.NotSet -> node.set("not set")
            is Cost.Value -> {
                node.node("buy").set(obj.buy)
            }
        }
    }

}

object CooldownSerializer : TypeSerializer<Cooldown> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Cooldown {
        if (node == null) throw IllegalArgumentException("what")

        return when (val cooldown = node.string!!.lowercase()) {
            "none" -> Cooldown.None
            "infinite" -> Cooldown.Infinite
            else -> {
                if (cooldown.trim().isBlank()) throw IllegalArgumentException("Empty cooldown")
                val time = cooldown.takeWhile(Char::isDigit).toLongOrNull()
                val unit = cooldown.takeLastWhile(Char::isLetter)

                if (time == null) throw IllegalArgumentException("Invalid time in cooldown")
                if (unit.isBlank()) throw IllegalArgumentException("No time unit in cooldown")

                val ctor: (Long) -> Cooldown.Time = when (unit.lowercase()) {
                    "ms" -> Cooldown.Time::Millis
                    "s" -> Cooldown.Time::Seconds
                    "m" -> Cooldown.Time::Minutes
                    "h" -> Cooldown.Time::Hours
                    "d" -> Cooldown.Time::Days
                    "w" -> Cooldown.Time::Weeks
                    "mo" -> Cooldown.Time::Months
                    "y" -> Cooldown.Time::Years
                    else -> throw IllegalArgumentException("Unknown time unit in cooldown (got \"${unit}\")")
                }

                return Cooldown.Duration(ctor(time))
            }
        }
    }

    override fun serialize(type: Type?, obj: Cooldown?, node: ConfigurationNode?) {
        if (obj == null || node == null) return

        when (obj) {
            is Cooldown.None -> node.set("none")
            is Cooldown.Infinite -> node.set("infinite")
            is Cooldown.Duration -> node.set(obj.time.display())
        }
    }

}

object QuantitiesSerializer : TypeSerializer<Quantities> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Quantities {
        if (node == null) throw IllegalArgumentException("what")

        val predefined = node.node("predefined").getList(java.lang.Integer::class.java)!!
            .map { it.toInt() }
            .toList()
        val allowed = node.node("allowed").get(Quantities.Allowed::class.java)!!
        return Quantities(predefined, allowed)
    }

    override fun serialize(type: Type?, obj: Quantities?, node: ConfigurationNode?) {
        if (obj == null || node == null) return
        node.node("predefined").set(obj.predefined)
        node.node("allowed").set(obj.allowed)
    }

}

object ItemSerializer : TypeSerializer<Item> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item {
        if (node == null) throw IllegalArgumentException("what")


        with(node) {
            val iid = ID(node("iid").string!!)
            val name = node("name").string
            val item = node("item").get(ItemType::class.java)!!
            val cost = node("cost").get(Cost::class.java)!!
            val cooldown = node("cooldown").get(Cooldown::class.java)!!
            val quantities = node("quantities").get(Quantities::class.java)!!
            val category = Shop.getCategory(ID(node("category").string!!))!!
            return Item(iid, name, item, cost, cooldown, quantities, category)
        }

    }

    override fun serialize(type: Type?, obj: Item?, node: ConfigurationNode?) {
        if (obj == null || node == null) return

        when (obj.item) {
            is Mat -> if (obj.item.material.isAir) return
            is ItemType.Item -> if (obj.item.item.type.isAir) return
            else -> {}
        }

        obj.run {
            with(node) {
                node("iid").set(iid.id)
                node("name").set(name)
                node("item").set(item)
                node("cost").set(cost)
                node("cooldown").set(cooldown)
                node("quantities").set(quantities)
                node("category").set(category.cid.id)
            }
        }
    }
}

object CategorySerializer : TypeSerializer<Category> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Category {
        if (node == null) throw IllegalArgumentException("what")

        val cid = ID(node.node("cid").string ?: ID.generate().id)
        val name = node.node("name").string!!
        val permission = node.node("permission").string
        val icon = node.node("icon").get(Material::class.java)!!
        return Category(cid, name, permission, icon)
    }

    override fun serialize(type: Type?, obj: Category?, node: ConfigurationNode?) {
        if (obj == null || node == null) return

        node.node("cid").set(obj.cid.id)
        node.node("name").set(obj.name)
        node.node("permission").set(obj.permission)
        node.node("icon").set(obj.display)
    }

}

object ShopSerializer : TypeSerializer<Shop> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Shop {
        if (node == null) throw IllegalArgumentException("what")

        val map = mutableMapOf<Category, MutableList<Item>>()
        node.node("categories").getList(Category::class.java)?.forEach(Shop::addCategory)
        node.node("items").getList(Item::class.java)!!
            .filter { item ->
                when (item.item) {
                    is Mat -> !item.item.material.isAir
                    is ItemType.Item -> !item.item.item.type.isAir
                    else -> true
                }
            }
            .map { it!! }
            .groupByTo(map) { it.category }
        return Shop(map)
    }

    override fun serialize(type: Type?, obj: Shop?, node: ConfigurationNode?) {
        if (obj == null || node == null) return

        node.node("categories").setList(Category::class.java, obj.categories().toList())
        node.node("items").setList(Item::class.java, obj.items.values.flatten())
    }
}

object DannyShopLoadables {

    private lateinit var loader: YamlConfigurationLoader

    /**
     * Exposes all custom type serializes required to successfully load
     * and save a DannyShop Item to a config file.
     */
    private fun collection(): TypeSerializerCollection = TypeSerializerCollection.builder()
        .register(ItemStack::class.java, ItemStackSerializer)
        .register(Category::class.java, CategorySerializer)
        .register(ItemType::class.java, ItemTypeSerializer)
        .register(Cost::class.java, CostSerializer)
        .register(Cooldown::class.java, CooldownSerializer)
        .register(Quantities::class.java, QuantitiesSerializer)
        .register(Item::class.java, ItemSerializer)
        .register(Shop::class.java, ShopSerializer)
        .build()


    /**
     * Loads the shop from shop.yml
     */
    fun loadShop(plugin: Plugin): Shop {
        loader = YamlConfigurationLoader.builder()
            .defaultOptions { opts ->
                opts.serializers { build -> build.registerAll(collection()) }
            }
            .nodeStyle(NodeStyle.BLOCK)
            .path(File(plugin.dataFolder, "shop.yml").toPath())
            .build()
        val root = loader.load()
        return root.node("shop").get(Shop::class.java) ?: Shop(mutableMapOf())
    }

    fun saveShop(shop: Shop) {
        if (!::loader.isInitialized) return
        val root = loader.load()
        root.set(null)
        root.node("shop").set(shop)
        loader.save(root)
    }
}
//</editor-fold>