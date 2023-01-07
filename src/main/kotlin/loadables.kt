package me.danny.shop.data

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.StringReader
import java.lang.IllegalArgumentException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

data class Category(val name: String)
data class Item(val iid: IID, val item: ItemType, val cost: Cost, val cooldown: Cooldown, val quantities: Quantities, val category: Category) {

    data class IID(val id: String)

    sealed interface ItemType {
        data class Material(val material: org.bukkit.Material) : ItemType {
            override fun inner(): Any = material
        }
        data class Item(val item: ItemStack) : ItemType {
            override fun inner(): Any = item
        }
        data class Exp(val exp: Double) : ItemType {
            override fun inner(): Any = exp
        }
        data class Command(val command: String) : ItemType {
            override fun inner(): Any = command
        }

        fun inner(): Any
    }

    data class Cost(val buy: Double, val sell: Double)

    sealed class Cooldown {

        object None : Cooldown()
        object Infinite : Cooldown()
        data class Duration(val time: Time) : Cooldown()

        sealed class Time(internal val time: Long, internal val base: TimeUnit, internal val suffix: String) {

            data class Millis(val millis: Long) : Time(millis, TimeUnit.MILLISECONDS, "ms")
            data class Seconds(val seconds: Long) : Time(seconds, TimeUnit.SECONDS, "s")
            data class Minutes(val minutes: Long) : Time(minutes, TimeUnit.MINUTES, "m")
            data class Hours(val hours: Long) : Time(hours, TimeUnit.HOURS, "h")
            data class Days(val days: Long) : Time(days * 24, TimeUnit.HOURS, "d")
            data class Weeks(val weeks: Long) : Time(weeks * 7 * 24, TimeUnit.HOURS, "w")
            data class Months(val months: Long) : Time(months * 4 * 7 * 24, TimeUnit.HOURS, "mo")
            data class Years(val years: Long) : Time(years * 12 * 4 * 7 * 24, TimeUnit.HOURS, "y")

            fun convertTo(unit: TimeUnit): Long = unit.convert(time, base)
        }
    }

    data class Quantities(val predefined: List<Int>, val allowed: Allowed) {
        enum class Allowed {
            Any,
            Predefined
        }
    }
}

object ItemStackSerializer : TypeSerializer<ItemStack> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): ItemStack {
        if(node == null) throw IllegalArgumentException("what")

        val map = node.node("itemstack").string!!
        return YamlConfiguration.loadConfiguration(StringReader(map)).getItemStack("item")!!
    }

    override fun serialize(type: Type?, obj: ItemStack?, node: ConfigurationNode?) {
        if(obj == null || node == null) return

        val yml = YamlConfiguration()
        yml.set("item", obj)
        node.node("itemstack").set(yml.saveToString())
    }


}
object ItemTypeSerializer : TypeSerializer<Item.ItemType> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item.ItemType {
        if(node == null) throw IllegalArgumentException("what")

        val itemType = node.node("type").string!!
        val obj = node.node("object")
        return when(itemType.lowercase()) {
            "material" -> Item.ItemType.Material(obj.get(Material::class.java)!!)
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
            is Item.ItemType.Material -> typeNode.set("material")
            is Item.ItemType.Item -> typeNode.set("item")
            is Item.ItemType.Exp -> typeNode.set("experience")
            is Item.ItemType.Command -> typeNode.set("command")
        }

        node.node("object").set(obj.inner())
    }

}
object CostSerializer : TypeSerializer<Item.Cost> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item.Cost {
        if(node == null) throw IllegalArgumentException("what")

        val buy = node.node("buy").double
        val sell = node.node("sell").double
        return Item.Cost(buy, sell)
    }

    override fun serialize(type: Type?, obj: Item.Cost?, node: ConfigurationNode?) {
        if(obj == null || node == null) return

        node.node("buy").set(obj.buy)
        node.node("sell").set(obj.sell)
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
        val category = Category(node.node("category").string!!)

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

object DannyShopLoadables {

    fun collection(): TypeSerializerCollection = TypeSerializerCollection.builder()
        .register(ItemStack::class.java, ItemStackSerializer)
        .register(Item.ItemType::class.java, ItemTypeSerializer)
        .register(Item.Cost::class.java, CostSerializer)
        .register(Item.Cooldown::class.java, CooldownSerializer)
        .register(Item.Quantities::class.java, QuantitiesSerializer)
        .register(Item::class.java, ItemSerializer)
        .build()

}