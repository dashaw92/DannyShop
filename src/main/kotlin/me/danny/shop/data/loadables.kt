package me.danny.shop.data

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.io.StringReader
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * Represents the DannyShop, holding all items
 * grouped by their respective categories
 */
data class Shop(val items: MutableMap<Category, MutableList<Item>>) {
    companion object {
        internal val CATEGORIES = mutableListOf<Category>()

        fun addCategory(category: Category): Boolean = CATEGORIES.add(category)
        fun getCategory(name: String): Category? = CATEGORIES.find { it.name == name }
    }

    fun addItem(item: Item) {
        items.computeIfAbsent(item.category) { mutableListOf() } += item
    }

    fun replaceItem(id: Item.IID, replacement: Item) {
        val old = itemByIid(id.id) ?: return
        val items = items.entries.find { (key, _) -> key.name == old.category.name }?.value ?: return
        val idx = items.indexOf(old)
        items[idx] = replacement
    }

    fun categories(): Collection<Category> = CATEGORIES
    fun items(category: Category): Collection<Item> =
        items.entries.find { (key, _) -> key.name == category.name }?.value ?: listOf()

    fun isEmpty(): Boolean = items.values.flatten().isEmpty()
    fun itemByIid(iid: String): Item? = items.values.flatten().find { it.iid.id == iid }
    fun itemByIid(iid: Item.IID): Item? = items.values.flatten().find { it.iid == iid }
}

/**
 * Categories are how items are grouped in DannyShop
 * It does not matter what they are called, as long as the name
 * fits inside an ItemStack's display name and there are no other
 * categories with the same name.
 *
 * Categories are entirely user-defined, there are no builtin categories.
 */
data class Category(val name: String, var display: Material) {
    fun changeDisplay(display: Material) {
        this.display = display
    }
}

/**
 * Represents a sellable item in DannyShop.
 * An Item is composed of:
 * * An IID (Item ID) - Unique ID to identify an item specifically
 * * ItemType - Holder for one of several variants of item types
 * * Cost - How much the item buys and sells for per unit
 * * Cooldown - How often a player may purchase the item
 * * Quantities - Details about sellable item quantities
 * * Category - The group this item will belong to
 *
 * Instances of this should NOT be held onto.
 * Use the item's IID to fetch it from the Shop.
 * Instances of this class may be outdated otherwise.
 */
data class Item(val iid: IID, val item: ItemType, val cost: Cost, val cooldown: Cooldown, val quantities: Quantities, val category: Category) {

    /**
     * A unique String identifying this Item
     * The only constraint placed on IIDs is that
     * they are unique. Duplicate IIDs will cause
     * undefined behavior.
     */
    data class IID(val id: String) {
        companion object {
            /**
             * Systematically generate a new IID based off the current
             * time and a pRNG.
             */
            fun generate(): IID {
                System.nanoTime()
                val now = System.currentTimeMillis()
                val salt = (Math.random() * 1000).toInt()
                return IID("%x%x".format(now, salt))
            }
        }
    }

    /**
     * Models all possible sellable item types DannyShop supports:
     * * Material - A basic material ItemStack with no attached data
     * * Item - A custom ItemStack with arbitrary attached data
     * * Exp - Experience that will be granted to the player on purchase
     * * Command - Commands that will target the player on purchase
     */
    sealed interface ItemType {
        data class Mat(val material: Material) : ItemType {
            override fun inner(): Any = material
            override fun display(): ItemStack = ItemStack(material, 1)
        }
        data class Item(val item: ItemStack) : ItemType {
            override fun inner(): Any = item
            override fun display(): ItemStack = item
        }
        data class Exp(val exp: Double) : ItemType {
            override fun inner(): Any = exp
            override fun display(): ItemStack = me.danny.shop.inv.ItemBuilder.makeItem(
                Material.EXPERIENCE_BOTTLE,
                "${ChatColor.GOLD}%.0f Experience".format(exp)
            )
        }
        data class Command(val command: String) : ItemType {
            override fun inner(): Any = command
            override fun display(): ItemStack =
                me.danny.shop.inv.ItemBuilder.makeItem(Material.COMMAND_BLOCK, "${ChatColor.BLUE}Run command", command)
        }

        fun inner(): Any
        fun display(): ItemStack
    }

    /**
     * Simple class wrapping the purchase and sell value of an item
     */
    sealed interface Cost {
        object NotSet : Cost
        data class Value(val buy: Double, val sell: Double) : Cost
    }

    /**
     * How often an item may be purchased
     */
    sealed interface Cooldown {

        /**
         * The item has no cooldown and can be purchased as often as wanted
         */
        object None : Cooldown

        /**
         * This item can only be purchased one time
         */
        object Infinite : Cooldown

        /**
         * Once the item is purchased, the player must wait this amount of time
         * before they may purchase it again
         */
        data class Duration(val time: Time) : Cooldown

        /**
         * The supported units of time Cooldown recognizes
         */
        sealed class Time(internal val time: Long, private val base: TimeUnit, internal val suffix: String) {

            data class Millis(val millis: Long) : Time(millis, TimeUnit.MILLISECONDS, "ms")
            data class Seconds(val seconds: Long) : Time(seconds, TimeUnit.SECONDS, "s")
            data class Minutes(val minutes: Long) : Time(minutes, TimeUnit.MINUTES, "m")
            data class Hours(val hours: Long) : Time(hours, TimeUnit.HOURS, "h")
            data class Days(val days: Long) : Time(days * 24, TimeUnit.HOURS, "d")
            data class Weeks(val weeks: Long) : Time(weeks * 7 * 24, TimeUnit.HOURS, "w")
            data class Months(val months: Long) : Time(months * 4 * 7 * 24, TimeUnit.HOURS, "mo")
            data class Years(val years: Long) : Time(years * 12 * 4 * 7 * 24, TimeUnit.HOURS, "y")

            fun convertTo(unit: TimeUnit): Long = unit.convert(time, base)
            fun display(): String = "$time$suffix"
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

object ItemStackSerializer : TypeSerializer<ItemStack> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): ItemStack {
        if(node == null) throw IllegalArgumentException("what")

        val map = node.string!!
        return YamlConfiguration.loadConfiguration(StringReader(map)).getItemStack("itemstack")!!
    }

    override fun serialize(type: Type?, obj: ItemStack?, node: ConfigurationNode?) {
        if(obj == null || node == null) return

        val yml = YamlConfiguration()
        yml.set("itemstack", obj)
        node.set(yml.saveToString())
    }


}
object ItemTypeSerializer : TypeSerializer<Item.ItemType> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item.ItemType {
        if(node == null) throw IllegalArgumentException("what")

        val itemType = node.node("type").string!!
        val obj = node.node("object")
        return when(itemType.lowercase()) {
            "material" -> Item.ItemType.Mat(obj.get(Material::class.java)!!)
            "item" -> Item.ItemType.Item(obj.get(ItemStack::class.java)!!)
            "experience" -> Item.ItemType.Exp(obj.double)
            "command" -> Item.ItemType.Command(obj.string!!)
            else -> throw IllegalArgumentException("Unknown item type $itemType")
        }
    }

    override fun serialize(type: Type?, obj: Item.ItemType?, node: ConfigurationNode?) {
        if(node == null || obj == null) return

        val typeNode = node.node("type")
        when(obj) {
            is Item.ItemType.Mat -> typeNode.set("material")
            is Item.ItemType.Item -> typeNode.set("item")
            is Item.ItemType.Exp -> typeNode.set("experience")
            is Item.ItemType.Command -> typeNode.set("command")
        }

        node.node("object").set(obj.inner())
    }

}
object CostSerializer : TypeSerializer<Item.Cost> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item.Cost {
        if (node == null) throw IllegalArgumentException("what")

        if (node.string == "not set") return Item.Cost.NotSet

        val buy = node.node("buy").double
        val sell = node.node("sell").double
        return Item.Cost.Value(buy, sell)
    }

    override fun serialize(type: Type?, obj: Item.Cost?, node: ConfigurationNode?) {
        if (obj == null || node == null) return

        when (obj) {
            is Item.Cost.NotSet -> node.set("not set")
            is Item.Cost.Value -> {
                node.node("buy").set(obj.buy)
                node.node("sell").set(obj.sell)
            }
        }
    }

}
object CooldownSerializer : TypeSerializer<Item.Cooldown> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item.Cooldown {
        if(node == null) throw IllegalArgumentException("what")

        return when(val cooldown = node.string!!.lowercase()) {
            "none" -> Item.Cooldown.None
            "infinite" -> Item.Cooldown.Infinite
            else -> {
                if(cooldown.trim().isBlank()) throw IllegalArgumentException("Empty cooldown")
                val suffixIdx = cooldown.indexOfFirst(Char::isLetter)
                if(suffixIdx == -1) throw IllegalArgumentException("No time unit in cooldown")
                val time = cooldown.substring(0 until suffixIdx).toLongOrNull() ?: throw IllegalArgumentException("Invalid time in cooldown")
                val suffix = cooldown.substring(suffixIdx until cooldown.length)

                val ctor: (Long) -> Item.Cooldown.Time = when(suffix.lowercase()) {
                    "ms" -> Item.Cooldown.Time::Millis
                    "s" -> Item.Cooldown.Time::Seconds
                    "m" -> Item.Cooldown.Time::Minutes
                    "h" -> Item.Cooldown.Time::Hours
                    "d" -> Item.Cooldown.Time::Days
                    "w" -> Item.Cooldown.Time::Weeks
                    "mo" -> Item.Cooldown.Time::Months
                    "y" -> Item.Cooldown.Time::Years
                    else -> throw IllegalArgumentException("Unknown time unit in cooldown (got \"${suffix}\")")
                }

                return Item.Cooldown.Duration(ctor(time))
            }
        }
    }

    override fun serialize(type: Type?, obj: Item.Cooldown?, node: ConfigurationNode?) {
        if(obj == null || node == null) return

        when(obj) {
            is Item.Cooldown.None -> node.set("none")
            is Item.Cooldown.Infinite -> node.set("infinite")
            is Item.Cooldown.Duration -> {
                val amount = obj.time.time
                val suffix = obj.time.suffix
                node.set("${amount}${suffix}")
            }
        }
    }

}
object QuantitiesSerializer : TypeSerializer<Item.Quantities> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item.Quantities {
        if(node == null) throw IllegalArgumentException("what")

        val predefined = node.node("predefined").getList(java.lang.Integer::class.java)!!
            .map { it.toInt() }
            .toList()
        val allowed = node.node("allowed").get(Item.Quantities.Allowed::class.java)!!
        return Item.Quantities(predefined, allowed)
    }

    override fun serialize(type: Type?, obj: Item.Quantities?, node: ConfigurationNode?) {
        if(obj == null || node == null) return
        node.node("predefined").set(obj.predefined)
        node.node("allowed").set(obj.allowed)
    }

}
object ItemSerializer : TypeSerializer<Item> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item {
        if(node == null) throw IllegalArgumentException("what")

        val iid = Item.IID(node.node("iid").string!!)
        val item = node.node("item").get(Item.ItemType::class.java)!!
        val cost = node.node("cost").get(Item.Cost::class.java)!!
        val cooldown = node.node("cooldown").get(Item.Cooldown::class.java)!!
        val quantities = node.node("quantities").get(Item.Quantities::class.java)!!
        val category = Shop.getCategory(node.node("category").string!!)!!

        return Item(iid, item, cost, cooldown, quantities, category)
    }

    override fun serialize(type: Type?, obj: Item?, node: ConfigurationNode?) {
        if(obj == null || node == null) return

        obj.run {
            node.node("iid").set(iid.id)
            node.node("item").set(item)
            node.node("cost").set(cost)
            node.node("cooldown").set(cooldown)
            node.node("quantities").set(quantities)
            node.node("category").set(category.name)
        }
    }
}
object CategorySerializer : TypeSerializer<Category> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Category {
        if (node == null) throw IllegalArgumentException("what")

        val name = node.node("name").string!!
        val icon = node.node("icon").get(Material::class.java)!!
        return Category(name, icon)
    }

    override fun serialize(type: Type?, obj: Category?, node: ConfigurationNode?) {
        if(obj == null || node == null) return

        node.node("name").set(obj.name)
        node.node("icon").set(obj.display)
    }

}
object ShopSerializer : TypeSerializer<Shop> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Shop {
        if(node == null) throw IllegalArgumentException("what")

        val map = mutableMapOf<Category, MutableList<Item>>()
        node.node("categories").getList(Category::class.java)?.forEach(Shop::addCategory)
        node.node("items").getList(Item::class.java)!!
            .map { it!! }
            .groupByTo(map) { it.category }
        return Shop(map)
    }

    override fun serialize(type: Type?, obj: Shop?, node: ConfigurationNode?) {
        if(obj == null || node == null) return

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
        .register(Item.ItemType::class.java, ItemTypeSerializer)
        .register(Item.Cost::class.java, CostSerializer)
        .register(Item.Cooldown::class.java, CooldownSerializer)
        .register(Item.Quantities::class.java, QuantitiesSerializer)
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
        if(!::loader.isInitialized) return
        val root = loader.load()
        root.set(null)
        root.node("shop").set(shop)
//        root.node("test").set(Category("Nature", Material.GRASS_BLOCK))
        loader.save(root)
    }
}