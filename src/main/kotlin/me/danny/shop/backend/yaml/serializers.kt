package me.danny.shop.backend.yaml

import me.danny.shop.model.*
import me.danny.shop.model.Item.*
import me.danny.shop.model.Item.ItemType.Mat
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.io.StringReader
import java.lang.reflect.Type

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
            "item" -> ItemType.Item(obj.get(ItemStack::class.java)!!)
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
            else -> SellLimit.Amount(limit.toULong())
        }
    }

    override fun serialize(type: Type?, obj: SellLimit?, node: ConfigurationNode?) {
        if (obj == null || node == null) return
        node.set(
            when (obj) {
                is SellLimit.None -> 0
                is SellLimit.Amount -> obj.amount.toInt()
            }
        )
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
            val dynamicPricing = node("dynamic-pricing").get(DynamicPricing::class.java)
            val isDynamic =
                dynamicPricing != null
                        && node("dynamic").getBoolean(false)
            return Item(iid, name, item, cost, sellLimit, category, isDynamic, dynamicPricing)
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
                node("dynamic").set(dynamic)
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

internal object DynamicPricingSerializer : TypeSerializer<DynamicPricing> {
    override fun deserialize(
        type: Type?,
        node: ConfigurationNode?
    ): DynamicPricing? {
        if (node == null) return null

        val enabled = node.node("enabled").getBoolean(false)
        val serverDemand = node.node("server-demand").getLong(0L).coerceAtLeast(0L)
        val replenishIntervalTicks = node.node("replenish-interval-ticks").getLong(0L).coerceAtLeast(0L)
        val replenishVolume = node.node("replenish-volume").getLong(0L).coerceAtLeast(0L)
        val minimumPrice = node.node("minimum-price").getDouble(0.0).coerceAtLeast(0.0)
        val playerImmunityVolume = node.node("player-immunity-volume").getLong(0L).coerceAtLeast(0L)
        val functions =
            node.node("valuation-functions").getList(ValuationFunction::class.java, mutableListOf<ValuationFunction>())

        return DynamicPricing(
            enabled,
            serverDemand,
            replenishIntervalTicks,
            replenishVolume,
            minimumPrice,
            playerImmunityVolume,
            functions
        )
    }

    override fun serialize(
        type: Type?,
        obj: DynamicPricing?,
        node: ConfigurationNode?
    ) {
        if (node == null) return

        if (obj == null || (!obj.enabled && obj.functions.isEmpty() && obj.playerImmunityVolume == 0L && obj.minimumPrice == 0.0 && obj.replenishVolume == 0L && obj.replenishIntervalTicks == 0L && obj.serverDemand == 0L)) {
            node.set(null)
            return
        }

        obj.run {
            with(node) {
                node("enabled").set(enabled)
                node("server-demand").set(serverDemand.coerceAtLeast(0L))
                node("replenish-interval-ticks").set(replenishIntervalTicks.coerceAtLeast(0L))
                node("replenish-volume").set(replenishVolume.coerceAtLeast(0L))
                node("minimum-price").set(minimumPrice.coerceAtLeast(0.0))
                node("player-immunity-volume").set(playerImmunityVolume.coerceAtLeast(0L))
                node("valuation-functions").set(functions)
            }
        }
    }

}

internal object ValuationFunctionSerializer : TypeSerializer<ValuationFunction> {
    override fun deserialize(
        type: Type?,
        node: ConfigurationNode?
    ): ValuationFunction {
        if (node == null) throw IllegalArgumentException("what")

        val range = node.node("volume-range").getLong(0L)
        val function = node.node("function").getString("0")
        return ValuationFunction(range, function)
    }

    override fun serialize(
        type: Type?,
        obj: ValuationFunction?,
        node: ConfigurationNode?
    ) {
        if (obj == null || node == null) throw IllegalArgumentException("what")

        node.node("volume-range").set(obj.range)
        node.node("function").set(obj.function)
    }

}
//</editor-fold>