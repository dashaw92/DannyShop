package me.danny.shop.backend

import me.danny.shop.backend.MongoOptions.Schema
import me.danny.shop.backend.MongoOptions.Schema.MongoDBSRV
import org.bukkit.plugin.*
import org.spongepowered.configurate.*
import org.spongepowered.configurate.serialize.*
import org.spongepowered.configurate.yaml.*
import java.io.*
import java.lang.reflect.*

object BackendManager {

    private val pathBackend = arrayOf("backend", "provider")
    private val pathOpts = arrayOf("backend", "options")

    private fun loader(plugin: Plugin): YamlConfigurationLoader = YamlConfigurationLoader.builder()
        .defaultOptions { opts ->
            opts.serializers { build -> build.registerAll(collection()) }
        }
        .nodeStyle(NodeStyle.BLOCK)
        .path(File(plugin.dataFolder, "config.yml").toPath())
        .build()

    fun loadDefaultProvider(plugin: Plugin): ShopBackend {
        val loader = loader(plugin)
        val root = loader.load()
        var backendOption = root.node(*pathBackend).get(BackendType::class.java)
        if (backendOption == null) {
            backendOption = BackendType.Yaml
            root.node(*pathBackend).commentIfAbsent(
                """
                                                    The backend provider to use when loading the shop.
                                                    Can be either "Yaml" or "MongoDB" (case-sensitive)
            """.trimIndent()
            )
            root.node(*pathBackend).set(BackendType.Yaml)
            loader.save(root)
        }

        val provider = getProvider(plugin, backendOption)

        plugin.logger.info("Using ${provider.name()} to load shop")
        return provider
    }

    internal fun getProvider(plugin: Plugin, backendType: BackendType): ShopBackend {
        val loader = loader(plugin)
        val root = loader.load()

        val defaults = mapOf(
            "yaml" to YamlOptions("shop.yml"),
            "mongo" to MongoOptions(MongoDBSRV, "bukkit", "walrus", "localhost", 27017)
        )
        defaults.forEach { (type, default) ->
            val node = root.node(*pathOpts, type)
            if (node.virtual()) {
                node.set(default)
            }
        }
        loader.save(root)

        val provider = when (backendType) {
            BackendType.Yaml -> {
                val options: YamlOptions =
                    root.node(*pathOpts, "yaml").get(YamlOptions::class.java, defaults["yaml"]!! as YamlOptions)
                YamlLoader(options)
            }

            BackendType.MongoDB -> {
                val options =
                    root.node(*pathOpts, "mongo").get(MongoOptions::class.java, defaults["mongo"]!! as MongoOptions)
                MongoLoader(options)
            }
        }

        return provider
    }
}

enum class BackendType {
    Yaml,
    MongoDB
}

sealed interface BackendOptions

data class YamlOptions(val path: String) : BackendOptions
data class MongoOptions(
    val schema: Schema,
    val user: String,
    val password: String,
    val host: String,
    val port: Int,
) : BackendOptions {
    enum class Schema {
        MongoDB,
        MongoDBSRV
    }
}

private fun collection(): TypeSerializerCollection = TypeSerializerCollection.builder()
    .register(YamlOptions::class.java, YamlTypeSerializer)
    .register(MongoOptions::class.java, MongoTypeSerializer)
    .build()

object YamlTypeSerializer : TypeSerializer<YamlOptions> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): YamlOptions {
        if (node == null) throw IllegalArgumentException("what")

        val path = node.node("filename").getString("shop.yml")
        return YamlOptions(path)
    }

    override fun serialize(type: Type?, obj: YamlOptions?, node: ConfigurationNode?) {
        if (node == null || obj == null) return

        node.node("filename").set(obj.path)
    }
}

object MongoTypeSerializer : TypeSerializer<MongoOptions> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): MongoOptions {
        if (node == null) throw IllegalArgumentException("what")

        val schema = node.node("schema").get(Schema::class.java) ?: MongoDBSRV
        val user = node.node("username").getString("bukkit")
        val password = node.node("password").getString("walrus")
        val host = node.node("hostname").getString("localhost")
        val port = node.node("port").getInt(27017)
        return MongoOptions(schema, user, password, host, port)
    }

    override fun serialize(type: Type?, obj: MongoOptions?, node: ConfigurationNode?) {
        if (node == null || obj == null) return

        node.node("schema").set(obj.schema)
        node.node("username").set(obj.user)
        node.node("password").set(obj.password)
        node.node("hostname").set(obj.host)
        node.node("port").set(obj.port)
    }
}