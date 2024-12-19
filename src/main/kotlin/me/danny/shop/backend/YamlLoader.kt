package me.danny.shop.backend

import me.danny.shop.backend.BackendType.Yaml
import me.danny.shop.backend.yaml.*
import me.danny.shop.model.*
import me.danny.shop.model.Item.*
import org.bukkit.inventory.*
import org.bukkit.plugin.*
import org.spongepowered.configurate.serialize.*
import org.spongepowered.configurate.yaml.*
import java.io.*

/**
 * Exposes all custom type serializes required to successfully load
 * and save a DannyShop Item to a config file.
 */
private fun collection(): TypeSerializerCollection = TypeSerializerCollection.builder()
    .register(ItemStack::class.java, ItemStackSerializer)
    .register(Category::class.java, CategorySerializer)
    .register(Item.ItemType::class.java, ItemTypeSerializer)
    .register(Cost::class.java, CostSerializer)
    .register(Cooldown::class.java, CooldownSerializer)
    .register(Quantities::class.java, QuantitiesSerializer)
    .register(Item::class.java, ItemSerializer)
    .register(Shop::class.java, ShopSerializer)
    .build()

internal class YamlLoader(private val options: YamlOptions) : ShopBackend {

    private fun loader(plugin: Plugin): YamlConfigurationLoader = YamlConfigurationLoader.builder()
        .defaultOptions { opts ->
            opts.serializers { build -> build.registerAll(collection()) }
        }
        .nodeStyle(NodeStyle.BLOCK)
        .path(File(plugin.dataFolder, options.path).toPath())
        .build()

    override fun type(): BackendType = Yaml
    override fun name(): String = "YamlBackend"

    override fun loadShop(plugin: Plugin): LoadResult {
        val root = loader(plugin).load()
        return try {
            //Attempt to load the shop (can be null if no shop.yml exists)
            val shop: Shop? = root.node("shop").get(Shop::class.java)
            LoadResult.Success(shop ?: Shop(mutableMapOf()))
        } catch (ex: SerializationException) {
            LoadResult.Failure(ex)
        }
    }

    override fun saveShop(plugin: Plugin, shop: Shop) {
        val loader = loader(plugin)
        val root = loader.load()
        root.set(null)
        root.node("shop").set(shop)
        loader.save(root)
    }
}