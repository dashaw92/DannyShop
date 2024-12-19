package me.danny.shop.backend.yaml

import me.danny.shop.model.*
import me.danny.shop.model.Item
import me.danny.shop.model.Item.*
import me.danny.shop.model.Item.ItemType.*
import org.bukkit.*
import org.bukkit.configuration.file.*
import org.bukkit.inventory.*
import org.spongepowered.configurate.*
import org.spongepowered.configurate.serialize.*
import java.io.*
import java.lang.reflect.*

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

internal object ItemTypeSerializer : TypeSerializer<Item.ItemType> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item.ItemType {
        if (node == null) throw IllegalArgumentException("what")

        val itemType = node.node("type").string!!
        val obj = node.node("object")
        return when (itemType.lowercase()) {
            "material" -> Mat(obj.get(Material::class.java)!!)
            "item" -> Item.ItemType.Item(obj.get(ItemStack::class.java)!!)
            "experience" -> Exp(obj.int)
            "command" -> Command(obj.string!!)
            else -> throw IllegalArgumentException("Unknown item type $itemType")
        }
    }

    override fun serialize(type: Type?, obj: Item.ItemType?, node: ConfigurationNode?) {
        if (node == null || obj == null) return

        val typeNode = node.node("type")
        when (obj) {
            is Mat -> typeNode.set("material")
            is Item.ItemType.Item -> typeNode.set("item")
            is Exp -> typeNode.set("experience")
            is Command -> typeNode.set("command")
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

internal object CooldownSerializer : TypeSerializer<Cooldown> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Cooldown {
        if (node == null) throw IllegalArgumentException("what")

        val cooldown = node.string!!.lowercase()
        return Cooldown.parse(cooldown)
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

internal object QuantitiesSerializer : TypeSerializer<Quantities> {
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

internal object ItemSerializer : TypeSerializer<Item> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Item {
        if (node == null) throw IllegalArgumentException("what")


        with(node) {
            val iid = ID(node("iid").string!!)
            val name = node("name").string
            val item = node("item").get(Item.ItemType::class.java)!!
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
            is Item.ItemType.Item -> if (obj.item.item.type.isAir) return
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
                    is Item.ItemType.Item -> !item.item.item.type.isAir
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
//</editor-fold>