import me.danny.shop.data.Item
import me.danny.shop.data.Shop
import org.bukkit.ChatColor
import org.bukkit.Material

object Demo {
    private val enchPick = me.danny.shop.inv.Item.addEnchantGlow(
        me.danny.shop.inv.Item.makeItem(
            Material.DIAMOND_PICKAXE,
            "${ChatColor.GREEN}Miner's Pickaxe",
            "${ChatColor.YELLOW}The pickaxe of legend"
        )
    )
    val items = listOf(
        Item(
            Item.IID("pickaxe"),
            Item.ItemType.Item(enchPick),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("iron_ore"),
            Item.ItemType.Mat(Material.IRON_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("gold_ore"),
            Item.ItemType.Mat(Material.GOLD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("emerald_ore"),
            Item.ItemType.Mat(Material.EMERALD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("diamond_ore"),
            Item.ItemType.Mat(Material.DIAMOND_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("exp_10"),
            Item.ItemType.Exp(20.0),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("command"),
            Item.ItemType.Command("give %PLAYER diamond 64"),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("grass"),
            Item.ItemType.Mat(Material.GRASS_BLOCK),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("dirt"),
            Item.ItemType.Mat(Material.DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("coarse_dirt"),
            Item.ItemType.Mat(Material.COARSE_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("podzol"),
            Item.ItemType.Mat(Material.PODZOL),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("rooted_dirt"),
            Item.ItemType.Mat(Material.ROOTED_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("warped_nylium"),
            Item.ItemType.Mat(Material.WARPED_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("crimson_nylium"),
            Item.ItemType.Mat(Material.CRIMSON_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("daisy"),
            Item.ItemType.Mat(Material.OXEYE_DAISY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("poppy"),
            Item.ItemType.Mat(Material.POPPY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("blue_flower"),
            Item.ItemType.Mat(Material.BLUE_ORCHID),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("purple_flower"),
            Item.ItemType.Mat(Material.ALLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("weird_flower"),
            Item.ItemType.Mat(Material.AZURE_BLUET),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("red_tulip"),
            Item.ItemType.Mat(Material.RED_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("orange_tulip"),
            Item.ItemType.Mat(Material.ORANGE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("brown_mushroom"),
            Item.ItemType.Mat(Material.BROWN_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("red_mushroom"),
            Item.ItemType.Mat(Material.RED_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("white_tulip"),
            Item.ItemType.Mat(Material.WHITE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("pink_tulip"),
            Item.ItemType.Mat(Material.PINK_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("pickaxe1"),
            Item.ItemType.Item(enchPick),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("iron_ore1"),
            Item.ItemType.Mat(Material.IRON_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("gold_ore1"),
            Item.ItemType.Mat(Material.GOLD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("emerald_ore1"),
            Item.ItemType.Mat(Material.EMERALD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("diamond_ore1"),
            Item.ItemType.Mat(Material.DIAMOND_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("exp_101"),
            Item.ItemType.Exp(20.0),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("command1"),
            Item.ItemType.Command("give %PLAYER diamond 64"),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("grass1"),
            Item.ItemType.Mat(Material.GRASS_BLOCK),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("dirt1"),
            Item.ItemType.Mat(Material.DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("coarse_dirt1"),
            Item.ItemType.Mat(Material.COARSE_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("podzol1"),
            Item.ItemType.Mat(Material.PODZOL),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("rooted_dirt1"),
            Item.ItemType.Mat(Material.ROOTED_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("warped_nylium1"),
            Item.ItemType.Mat(Material.WARPED_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("crimson_nylium1"),
            Item.ItemType.Mat(Material.CRIMSON_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("daisy1"),
            Item.ItemType.Mat(Material.OXEYE_DAISY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("poppy1"),
            Item.ItemType.Mat(Material.POPPY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("blue_flower1"),
            Item.ItemType.Mat(Material.BLUE_ORCHID),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("purple_flower1"),
            Item.ItemType.Mat(Material.ALLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("weird_flower1"),
            Item.ItemType.Mat(Material.AZURE_BLUET),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("red_tulip1"),
            Item.ItemType.Mat(Material.RED_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("orange_tulip1"),
            Item.ItemType.Mat(Material.ORANGE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("brown_mushroom1"),
            Item.ItemType.Mat(Material.BROWN_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("red_mushroom1"),
            Item.ItemType.Mat(Material.RED_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("white_tulip1"),
            Item.ItemType.Mat(Material.WHITE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("pink_tulip1"),
            Item.ItemType.Mat(Material.PINK_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("pickaxe2"),
            Item.ItemType.Item(enchPick),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("iron_ore2"),
            Item.ItemType.Mat(Material.IRON_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("gold_ore2"),
            Item.ItemType.Mat(Material.GOLD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("emerald_ore2"),
            Item.ItemType.Mat(Material.EMERALD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("diamond_ore2"),
            Item.ItemType.Mat(Material.DIAMOND_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("exp_102"),
            Item.ItemType.Exp(20.0),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("command2"),
            Item.ItemType.Command("give %PLAYER diamond 64"),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("grass2"),
            Item.ItemType.Mat(Material.GRASS_BLOCK),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("dirt2"),
            Item.ItemType.Mat(Material.DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("coarse_dirt2"),
            Item.ItemType.Mat(Material.COARSE_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("podzol2"),
            Item.ItemType.Mat(Material.PODZOL),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("rooted_dirt2"),
            Item.ItemType.Mat(Material.ROOTED_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("warped_nylium2"),
            Item.ItemType.Mat(Material.WARPED_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("crimson_nylium2"),
            Item.ItemType.Mat(Material.CRIMSON_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("daisy2"),
            Item.ItemType.Mat(Material.OXEYE_DAISY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("poppy2"),
            Item.ItemType.Mat(Material.POPPY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("blue_flower2"),
            Item.ItemType.Mat(Material.BLUE_ORCHID),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("purple_flower2"),
            Item.ItemType.Mat(Material.ALLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("weird_flower2"),
            Item.ItemType.Mat(Material.AZURE_BLUET),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("red_tulip2"),
            Item.ItemType.Mat(Material.RED_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("orange_tulip2"),
            Item.ItemType.Mat(Material.ORANGE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("brown_mushroom2"),
            Item.ItemType.Mat(Material.BROWN_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("red_mushroom2"),
            Item.ItemType.Mat(Material.RED_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("white_tulip2"),
            Item.ItemType.Mat(Material.WHITE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("pink_tulip2"),
            Item.ItemType.Mat(Material.PINK_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3pickaxe"),
            Item.ItemType.Item(enchPick),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3iron_ore"),
            Item.ItemType.Mat(Material.IRON_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3gold_ore"),
            Item.ItemType.Mat(Material.GOLD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3emerald_ore"),
            Item.ItemType.Mat(Material.EMERALD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3diamond_ore"),
            Item.ItemType.Mat(Material.DIAMOND_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3exp_10"),
            Item.ItemType.Exp(20.0),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3command"),
            Item.ItemType.Command("give %PLAYER diamond 64"),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3grass"),
            Item.ItemType.Mat(Material.GRASS_BLOCK),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3dirt"),
            Item.ItemType.Mat(Material.DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3coarse_dirt"),
            Item.ItemType.Mat(Material.COARSE_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3podzol"),
            Item.ItemType.Mat(Material.PODZOL),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3rooted_dirt"),
            Item.ItemType.Mat(Material.ROOTED_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3warped_nylium"),
            Item.ItemType.Mat(Material.WARPED_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3crimson_nylium"),
            Item.ItemType.Mat(Material.CRIMSON_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3daisy"),
            Item.ItemType.Mat(Material.OXEYE_DAISY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3poppy"),
            Item.ItemType.Mat(Material.POPPY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3blue_flower"),
            Item.ItemType.Mat(Material.BLUE_ORCHID),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3purple_flower"),
            Item.ItemType.Mat(Material.ALLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3weird_flower"),
            Item.ItemType.Mat(Material.AZURE_BLUET),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3red_tulip"),
            Item.ItemType.Mat(Material.RED_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3orange_tulip"),
            Item.ItemType.Mat(Material.ORANGE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3brown_mushroom"),
            Item.ItemType.Mat(Material.BROWN_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3red_mushroom"),
            Item.ItemType.Mat(Material.RED_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3white_tulip"),
            Item.ItemType.Mat(Material.WHITE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("3pink_tulip"),
            Item.ItemType.Mat(Material.PINK_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4pickaxe"),
            Item.ItemType.Item(enchPick),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4iron_ore"),
            Item.ItemType.Mat(Material.IRON_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4gold_ore"),
            Item.ItemType.Mat(Material.GOLD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4emerald_ore"),
            Item.ItemType.Mat(Material.EMERALD_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4diamond_ore"),
            Item.ItemType.Mat(Material.DIAMOND_ORE),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4exp_10"),
            Item.ItemType.Exp(20.0),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4command"),
            Item.ItemType.Command("give %PLAYER diamond 64"),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4grass"),
            Item.ItemType.Mat(Material.GRASS_BLOCK),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4dirt"),
            Item.ItemType.Mat(Material.DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4coarse_dirt"),
            Item.ItemType.Mat(Material.COARSE_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4podzol"),
            Item.ItemType.Mat(Material.PODZOL),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4rooted_dirt"),
            Item.ItemType.Mat(Material.ROOTED_DIRT),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4warped_nylium"),
            Item.ItemType.Mat(Material.WARPED_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4crimson_nylium"),
            Item.ItemType.Mat(Material.CRIMSON_NYLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4daisy"),
            Item.ItemType.Mat(Material.OXEYE_DAISY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4poppy"),
            Item.ItemType.Mat(Material.POPPY),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4blue_flower"),
            Item.ItemType.Mat(Material.BLUE_ORCHID),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4purple_flower"),
            Item.ItemType.Mat(Material.ALLIUM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4weird_flower"),
            Item.ItemType.Mat(Material.AZURE_BLUET),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4red_tulip"),
            Item.ItemType.Mat(Material.RED_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4orange_tulip"),
            Item.ItemType.Mat(Material.ORANGE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4brown_mushroom"),
            Item.ItemType.Mat(Material.BROWN_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4red_mushroom"),
            Item.ItemType.Mat(Material.RED_MUSHROOM),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4white_tulip"),
            Item.ItemType.Mat(Material.WHITE_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
        Item(
            Item.IID("4pink_tulip"),
            Item.ItemType.Mat(Material.PINK_TULIP),
            Item.Cost(10.0, 5.0),
            Item.Cooldown.None,
            Item.Quantities(listOf(1), Item.Quantities.Allowed.Any),
            Shop.getCategory("Nature")!!
        ),
    )
}