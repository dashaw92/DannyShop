package me.danny.shop.backend.yaml

import me.danny.shop.model.Category
import me.danny.shop.model.ID
import me.danny.shop.model.Item
import me.danny.shop.model.Item.*
import me.danny.shop.model.Item.ItemType.*
import me.danny.shop.model.Shop
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.io.StringReader
import java.lang.reflect.Type
import kotlin.IllegalArgumentException
import kotlin.collections.MutableList
import kotlin.collections.filter
import kotlin.collections.flatten
import kotlin.collections.forEach
import kotlin.collections.groupByTo
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.toList
import kotlin.run
import kotlin.text.lowercase
import kotlin.toUInt
import kotlin.with

//<editor-fold desc="Type serializers">
internal object ItemStackSerializer : TypeSerializer<ItemStack> {
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

internal object ItemTypeSerializer : TypeSerializer<ItemType> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): ItemType {
        if (node == null) throw IllegalArgumentException("what")

        val itemType = node.node("type").string!!
        val obj = node.node("object")
        return when (itemType.lowercase()) {
            "material" -> Mat(obj.get(Material::class.java)!!)
            "item" -> Item(obj.get(ItemStack::class.java)!!)
            else -> throw IllegalArgumentException("Unknown item type $itemType")
        }
    }

    override fun serialize(type: Type?, obj: ItemType?, node: ConfigurationNode?) {
        if (node == null || obj == null) return

        val typeNode = node.node("type")
        when (obj) {
            is Mat -> typeNode.set("material")
            is ItemType.Item -> typeNode.set("item")
        }

        node.node("object").set(obj.inner())
    }

}

internal object CostSerializer : TypeSerializer<Cost> {
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

internal object SellLimitSerializer : TypeSerializer<SellLimit> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): SellLimit {
        if (node == null) throw IllegalArgumentException("what")

        val limit = node.getInt(0)
        return when {
            limit <= 0 -> SellLimit.None
            else -> SellLimit.Amount(limit.toUInt())
        }
    }

    override fun serialize(type: Type?, obj: SellLimit?, node: ConfigurationNode?) {
        if (obj == null || node == null) return
        node.set(when(obj) {
            is SellLimit.None -> 0
            is SellLimit.Amount -> obj.amount.toInt()
        })
    }

}

internal object ItemSerializer : TypeSerializer<Item> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item {
        if (node == null) throw IllegalArgumentException("what")


        with(node) {
            val iid = ID(node("iid").string!!)
            val name = node("name").string
            val item = node("item").get(ItemType::class.java)!!
            val cost = node("cost").get(Cost::class.java)!!
            val sellLimit = node("sell-limit").get(SellLimit::class.java)!!
            val category = Shop.getCategory(ID(node("category").string!!))!!
            return Item(iid, name, item, cost, sellLimit, category)
        }

    }

    override fun serialize(type: Type?, obj: Item?, node: ConfigurationNode?) {
        if (obj == null || node == null) return

        when (obj.item) {
            is Mat -> if (obj.item.material.isAir) return
            is ItemType.Item -> if (obj.item.item.type.isAir) return
        }

        obj.run {
            with(node) {
                node("iid").set(iid.id)
                node("name").set(name)
                node("item").set(item)
                node("cost").set(cost)
                node("sell-limit").set(sellLimit)
                node("category").set(category.cid.id)
            }
        }
    }
}

internal object CategorySerializer : TypeSerializer<Category> {
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

internal object ShopSerializer : TypeSerializer<Shop> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Shop {
        if (node == null) throw IllegalArgumentException("what")

        val map = mutableMapOf<Category, MutableList<Item>>()
        node.node("categories").getList(Category::class.java)?.forEach(Shop::addCategory)
        node.node("items").getList(Item::class.java)!!
            .filter { item ->
                when (item.item) {
                    is Mat -> !item.item.material.isAir
                    is ItemType.Item -> !item.item.item.type.isAir
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
//</editor-fold>