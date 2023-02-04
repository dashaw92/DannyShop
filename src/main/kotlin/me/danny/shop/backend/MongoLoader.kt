package me.danny.shop.backend

import com.mongodb.*
import com.mongodb.client.*
import me.danny.shop.backend.BackendType.MongoDB
import me.danny.shop.backend.MongoOptions.Schema
import me.danny.shop.model.*
import me.danny.shop.model.Item.*
import me.danny.shop.model.Item.Quantities.Allowed
import org.bson.*
import org.bukkit.*
import org.bukkit.configuration.file.*
import org.bukkit.plugin.*
import java.io.*

internal class MongoLoader(options: MongoOptions) : ShopBackend {

    private var exception: Exception? = null
    private var database: MongoDatabase?

    init {
        try {
            val uri = when (options.schema) {
                Schema.MongoDB -> ConnectionString("mongodb://${options.user}:${options.password}@${options.host}:${options.port}/")
                Schema.MongoDBSRV -> ConnectionString("mongodb+srv://${options.user}:${options.password}@${options.host}/")
            }
            val settings = MongoClientSettings.builder()
                .applyConnectionString(uri)
                .serverApi(
                    ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build()
                )
                .build()
            val client = MongoClients.create(settings)

            database = client.getDatabase("dannyshop")
        } catch (ex: Exception) {
            exception = ex
            database = null
        }
    }

    override fun type(): BackendType = MongoDB
    override fun name(): String = "MongoDBLoader"

    override fun loadShop(plugin: Plugin): LoadResult {
        if (exception != null) return LoadResult.Failure(exception!!)
        val db = database!!

        try {
            val categories = db.getCollection("categories")
                .find()
                .map {
                    val cid = ID(it.getString("cid"))
                    val name = it.getString("name")
                    val display = Material.matchMaterial(it.getString("display")) ?: Material.CHEST
                    val permission = it.getString("permission")
                    Category(cid, name, permission, display)
                }.toList()
            categories.forEach(Shop::addCategory)
        } catch (ex: Exception) {
            return LoadResult.Failure(ex)
        }

        val items = try {
            val items = db.getCollection("items")
                .find()
                .map {
                    val iid = ID(it.getString("iid"))
                    val name = it.getString("name")
                    val itemType = it.getString("itemType")
                    val itemObj = it.getString("itemObject")
                    val item = when (itemType) {
                        "material" -> ItemType.Mat(Material.matchMaterial(itemObj) ?: Material.CHEST)
                        "experience" -> ItemType.Exp(itemObj.toInt())
                        "command" -> ItemType.Command(itemObj)
                        "item" -> {
                            val yml = YamlConfiguration.loadConfiguration(StringReader(itemObj))
                            val item = yml.getItemStack("itemstack")!!
                            ItemType.Item(item)
                        }

                        else -> throw IllegalArgumentException("Invalid item type: $itemType")
                    }
                    val cost = when (val cost = it.getString("cost")) {
                        "not set" -> Cost.NotSet
                        else -> Cost.Value(cost.toDouble())
                    }
                    val cooldown = Cooldown.parse(it.getString("cooldown"))
                    val quantitiesPredefined =
                        it.getString("predefinedQuantities").split(' ').mapNotNull(String::toIntOrNull)
                    val quantitiesAllowed = Allowed.valueOf(it.getString("allowedQuantities"))
                    val quantities = Quantities(quantitiesPredefined, quantitiesAllowed)

                    val category = Shop.getCategory(ID(it.getString("category")))!!
                    Item(iid, name, item, cost, cooldown, quantities, category)
                }.toList()
            items
        } catch (ex: Exception) {
            return LoadResult.Failure(ex)
        }

        val map: MutableMap<Category, MutableList<Item>> = mutableMapOf()
        items
            .groupByTo(map) { it.category }
        return LoadResult.Success(Shop(map))
    }

    override fun saveShop(plugin: Plugin, shop: Shop) {
        val db = database!!
        val categories = db.getCollection("categories")
        categories.deleteMany(Document())
        categories.insertMany(
            shop.categories()
                .map {
                    Document(
                        mapOf(
                            "cid" to it.cid.id,
                            "name" to it.name,
                            "display" to it.display.name,
                            "permission" to it.permission
                        )
                )
            })

        val items = db.getCollection("items")
        items.deleteMany(Document())
        items.insertMany(shop.items.values.flatten()
            .map {
                val (type, obj) = when (it.item) {
                    is ItemType.Mat -> "material" to it.item.material.name
                    is ItemType.Command -> "command" to it.item.command
                    is ItemType.Exp -> "experience" to it.item.exp.toString()
                    is ItemType.Item -> {
                        val yml = YamlConfiguration()
                        yml.set("itemstack", it.item.item)
                        "item" to yml.saveToString()
                    }
                }

                Document(
                    mapOf(
                        "iid" to it.iid.id,
                        "name" to it.name,
                        "itemType" to type,
                        "itemObject" to obj,
                        "cost" to when (it.cost) {
                            is Cost.NotSet -> "not set"
                            is Cost.Value -> it.cost.buy.toString()
                        },
                        "cooldown" to when (it.cooldown) {
                            is Cooldown.None -> "none"
                            is Cooldown.Infinite -> "infinite"
                            is Cooldown.Duration -> it.cooldown.time.display()
                        },
                        "predefinedQuantities" to it.quantities.predefined.joinToString(separator = " "),
                        "allowedQuantities" to it.quantities.allowed.name,
                        "category" to it.category.cid.id,
                    )
                )
            })
    }
}