package me.danny.shop

import me.danny.shop.data.*
import me.danny.shop.data.Item.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

class DannyShop : JavaPlugin() {

    override fun onEnable() {
        logger.info("DannyShop enabled!")

        val loader = YamlConfigurationLoader.builder()
            .defaultOptions { opts -> opts.serializers { build -> build.registerAll(DannyShopLoadables.collection()) } }
            .path(File(dataFolder, "test.yml").toPath())
            .build()

        val root = loader.load()
        root.node("item").set(exampleItem())
        loader.save(root)
        logger.info("If all went well, the item should be in the config!")

        val output = root.node("item").get(Item::class.java)!!
        logger.info("$output")
    }

    private fun exampleItem(): Item = Item(
        IID("123445"),
        ItemType.Item(ItemStack(Material.GRASS_BLOCK, 2)),
        Cost(20.0, 10.0),
        Cooldown.Duration(Cooldown.Time.Hours(2)),
        Quantities(listOf(1, 32, 64), Quantities.Allowed.Any),
        Category("Nature")
    )
}