package me.danny.shop

import me.danny.shop.data.*
import me.danny.shop.inv.Menu
import me.danny.shop.inv.MenuListener
import me.danny.shop.inv.ShopMenu
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

import me.danny.shop.data.Item
import me.danny.shop.data.Item.*
import me.danny.shop.data.Item.ItemType.*

class DannyShop : JavaPlugin() {
    companion object {
        lateinit var SHOP: Shop
            private set
    }

    override fun onEnable() {
        SHOP = DannyShopLoadables.loadShop(this)
        Bukkit.getPluginManager().registerEvents(MenuListener, this)
    }

    override fun onDisable() {
        DannyShopLoadables.saveShop(SHOP)
        Menu.closeOpenInvs()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(label.lowercase() != "dannyshop") return true
        if(sender !is Player) {
            sender.sendMessage("${ChatColor.RED}Nope!")
            return true
        }

        if(args.isEmpty()) {
            sender.sendMessage("Usage: /dannyshop <test/open>")
            return true
        }

        when(args.first().lowercase()) {
            "test" -> {
                buildSampleShop()
                sender.sendMessage("Done! Check config!")
            }
            "open" -> ShopMenu(SHOP, sender)
        }
        return true
    }

    private fun buildSampleShop() {
        Shop.CATEGORIES.clear()

        Shop.CATEGORIES.addAll(listOf(
            Category("Ores", Material.DEEPSLATE_GOLD_ORE),
            Category("Nature", Material.PODZOL),
            Category("Wood", Material.SPRUCE_PLANKS),
            Category("Redstone", Material.REDSTONE_BLOCK),
            Category("Building", Material.BIRCH_DOOR),
            Category("Food", Material.SWEET_BERRIES),
        ))

        val enchPick = me.danny.shop.inv.Item.addEnchantGlow(me.danny.shop.inv.Item.makeItem(Material.DIAMOND_PICKAXE, "${ChatColor.GREEN}Miner's Pickaxe", "${ChatColor.YELLOW}The pickaxe of legend"))

        val items = listOf(
            Item(IID("pickaxe"),        ItemType.Item(enchPick),        Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Ores")!!),
            Item(IID("iron_ore"),       Mat(Material.IRON_ORE),         Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Ores")!!),
            Item(IID("gold_ore"),       Mat(Material.GOLD_ORE),         Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Ores")!!),
            Item(IID("emerald_ore"),    Mat(Material.EMERALD_ORE),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Ores")!!),
            Item(IID("diamond_ore"),    Mat(Material.DIAMOND_ORE),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Ores")!!),
            Item(IID("exp_10"),         Exp(20.0),                  Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Ores")!!),
            Item(IID("command"),        Command("give %PLAYER diamond 64"),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Ores")!!),

            Item(IID("grass"),          Mat(Material.GRASS_BLOCK),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("dirt"),           Mat(Material.DIRT),             Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("coarse_dirt"),    Mat(Material.COARSE_DIRT),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("podzol"),         Mat(Material.PODZOL),           Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("rooted_dirt"),    Mat(Material.ROOTED_DIRT),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("warped_nylium"),  Mat(Material.WARPED_NYLIUM),    Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("crimson_nylium"), Mat(Material.CRIMSON_NYLIUM),   Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("daisy"),          Mat(Material.OXEYE_DAISY),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("poppy"),          Mat(Material.POPPY),            Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("blue_flower"),    Mat(Material.BLUE_ORCHID),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("purple_flower"),  Mat(Material.ALLIUM),           Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("weird_flower"),   Mat(Material.AZURE_BLUET),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("red_tulip"),      Mat(Material.RED_TULIP),        Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("orange_tulip"),   Mat(Material.ORANGE_TULIP),     Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("brown_mushroom"), Mat(Material.BROWN_MUSHROOM),   Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("red_mushroom"),   Mat(Material.RED_MUSHROOM),     Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("white_tulip"),    Mat(Material.WHITE_TULIP),      Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
            Item(IID("pink_tulip"),     Mat(Material.PINK_TULIP),       Cost(10.0, 5.0), Cooldown.None, Quantities(listOf(1), Quantities.Allowed.Any), Shop.getCategory("Nature")!!),
        )

        val map: MutableMap<Category, MutableList<Item>> = mutableMapOf()
        items
            .groupByTo(map) { it.category }
        SHOP = Shop(map)

        DannyShopLoadables.saveShop(SHOP)
    }
}