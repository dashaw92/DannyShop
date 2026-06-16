package me.danny.shop.backend

import me.danny.shop.config.Config
import org.bukkit.plugin.Plugin
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.lang.reflect.Type

internal object BackendManager {

    private val pathBackend = arrayOf("backend", "provider")
    private val pathOpts = arrayOf("backend", "options")

    private val config = arrayOf("config")

    private fun loader(plugin: Plugin): YamlConfigurationLoader = YamlConfigurationLoader.builder()
        .defaultOptions { opts ->
            opts.serializers { build -> build.registerAll(collection()) }
        }
        .nodeStyle(NodeStyle.BLOCK)
        .path(File(plugin.dataFolder, "config.yml").toPath())
        .build()

    internal fun loadDefaultProvider(plugin: Plugin): ShopBackend {
        val loader = loader(plugin)
        val root = loader.load()
        var backendOption = root.node(*pathBackend).get(BackendType::class.java)
        if (backendOption == null) {
            backendOption = BackendType.Yaml
            root.node(*pathBackend).set(BackendType.Yaml)
            loader.save(root)
        }

        val provider = getProvider(plugin, backendOption)

        plugin.logger.info("Using ${provider.name()} to load shop")
        return provider
    }

    internal fun getConfig(plugin: Plugin): Config {
        val loader = loader(plugin)
        val root = loader.load()
        var c = root.node(*config).get(Config::class.java)
        if (c == null) {
            c = Config()
            root.node(*config).set(c)
            loader.save(root)
        }

        return c
    }

    internal fun getProvider(plugin: Plugin, backendType: BackendType): ShopBackend {
        val loader = loader(plugin)
        val root = loader.load()

        val defaults = mapOf(
            BackendType.Yaml to YamlOptions("shop.yml"),
        )
        defaults.forEach { (type, default) ->
            val node = root.node(*pathOpts, type.serName)
            if (node.virtual()) {
                node.set(default)
            }
        }
        loader.save(root)

        val options = root.node(*pathOpts, backendType.serName)
            .get(backendType.clazz, defaults[backendType]!!) as BackendOptions

        return backendType.loaderFn(options)
    }
}

internal enum class BackendType(
    val serName: String,
    val clazz: Class<out BackendOptions>,
    val loaderFn: (BackendOptions) -> ShopBackend
) {
    Yaml("yaml", YamlOptions::class.java, { opts -> YamlLoader(opts as YamlOptions) }),
}

internal sealed interface BackendOptions

internal data class YamlOptions(val path: String) : BackendOptions

private fun collection(): TypeSerializerCollection = TypeSerializerCollection.builder()
    .register(YamlOptions::class.java, YamlTypeSerializer)
    .register(Config::class.java, ConfigSerializer)
    .build()

internal object YamlTypeSerializer : TypeSerializer<YamlOptions> {
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

internal object ConfigSerializer : TypeSerializer<Config> {
    override fun deserialize(
        type: Type?,
        node: ConfigurationNode?
    ): Config {
        if (node == null) throw IllegalArgumentException("what")

        val sellLimitRefresh = node.node("sell-limit-refresh-ticks").getLong(72000L)
        val ecoAnalytics = node.node("eco-analytics-enabled").getBoolean(true)
        val logs = node.node("sale-logs-enabled").getBoolean(true)
        val persistLogs = node.node("sale-logs-persist").getBoolean(true)

        return Config(
            ecoAnalyticsEnabled = ecoAnalytics,
            loggingEnabled = logs,
            loggingPersistLogs = persistLogs,
            sellLimitRefreshTicks = sellLimitRefresh
        )
    }

    override fun serialize(
        type: Type?,
        obj: Config?,
        node: ConfigurationNode?
    ) {
        if (obj == null || node == null) return

        node.node("sell-limit-refresh-ticks").set(obj.sellLimitRefreshTicks)
        node.node("eco-analytics-enabled").set(obj.ecoAnalyticsEnabled)
        node.node("sale-logs-enabled").set(obj.loggingEnabled)
        node.node("sale-logs-persist").set(obj.loggingPersistLogs)
    }

}