package me.danny.shop.inv

import me.danny.shop.data.Category
import me.danny.shop.data.Shop
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ShopMenu(private val shop: Shop, viewer: Player) : Menu(6, "${ChatColor.GOLD}DannyShop", viewer) {

    companion object {
        @Suppress("DEPRECATION")
        private val ITEM_KEY = NamespacedKey("dannyshop", "item_iid")
    }

    private var categories: List<Category> = shop.categories().take(6)
    private val itemPage: ItemPage
    private val categoryPage: CategoryPage

    init {
        val selected = categories.firstOrNull() ?: Category("All", Material.CHEST)
        itemPage = ItemPage(shop.items(selected), Pair(inv.size - 2, inv.size - 1))
        categoryPage = CategoryPage(shop.categories(), selected, Pair(1, 46))
        build()
    }

    override fun build() {
        if (shop.isEmpty()) {
            inv = Bukkit.createInventory(this, 27, "${ChatColor.DARK_RED}DannyShop")
            val filler = Item.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
            val notice = Item.makeItem(
                Material.REDSTONE_TORCH, "${ChatColor.GOLD}The shop is empty!",
                "${ChatColor.YELLOW}But don't worry! Creating a shop is simple!",
                "${ChatColor.YELLOW}Check out the command ${ChatColor.LIGHT_PURPLE}/dannyshop import${ChatColor.YELLOW}.",
            )
            (0 until inv.size).forEach { inv.setItem(it, filler) }
            inv.setItem(13, notice)
            viewer.openInventory(inv)
            return
        }

        val page = itemPage.page() + 1
        val maxPages = itemPage.numPages()
        inv = Bukkit.createInventory(
            this, 6 * 9,
            "${ChatColor.DARK_RED}DannyShop ${ChatColor.GRAY}- ${ChatColor.BLUE}${categoryPage.selected().name} ${ChatColor.DARK_GRAY}(%d/%d)".format(
                page,
                maxPages
            )
        )

        val catBorder = Item.makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
        listOf(1, 10, 19, 28, 37, 46)
            .forEach { inv.setItem(it, catBorder) }
        val ctrlBorder = Item.makeItem(Material.BLUE_STAINED_GLASS_PANE, " ")
        (47 until inv.size)
            .forEach { inv.setItem(it, ctrlBorder) }

        categoryPage.render(inv)
        itemPage.render(inv)

        viewer.openInventory(inv)
    }

    override fun onClick(event: InventoryClickEvent) {
        when {
            event.slot % 9 == 0 -> {
                val row = event.slot / 9
                val selected = categoryPage.displayedCategories()[row]
                itemPage.changeCategory(shop, selected)
                categoryPage.changeCategory(selected)
                build()
            }
            event.slot == 1 -> { categoryPage.prevPage(); build() }
            event.slot == 46 -> { categoryPage.nextPage(); build() }
            event.slot == inv.size - 2 -> { itemPage.prevPage(); build() }
            event.slot == inv.size - 1 -> { itemPage.nextPage(); build() }
        }
    }

    class ItemPage(coll: Collection<me.danny.shop.data.Item>, buttons: Pair<Int, Int>) :
        Page<me.danny.shop.data.Item>(coll, Pair(2, 0), Pair(7, 5), buttons) {
        override fun display(inv: Inventory) {
            var invIdx = start.second * 9 + start.first
            for (item in items.drop(page * size).take(size)) {
                inv.setItem(invIdx, makeMenuItem(item))
                invIdx += 1
                if (invIdx % 9 == 0) invIdx += 2
            }
        }

        fun changeCategory(shop: Shop, category: Category) {
            page = 0
            items = shop.items(category)
        }

        private fun makeMenuItem(item: me.danny.shop.data.Item): ItemStack {
            val tagged = Item.attachKey(item.item.display(), ITEM_KEY, PersistentDataType.STRING, item.iid.id)

            val header =
                "${ChatColor.GOLD}+${ChatColor.STRIKETHROUGH}${ChatColor.BOLD}------[${ChatColor.DARK_RED} DannyShop ${ChatColor.GOLD}${ChatColor.STRIKETHROUGH}${ChatColor.BOLD}]------${ChatColor.GOLD}+"
            val footer =
                "${ChatColor.GOLD}+${ChatColor.STRIKETHROUGH}${ChatColor.BOLD}-----------------------${ChatColor.GOLD}+"
            val fields: MutableList<String> = mutableListOf()

            fields.add("${ChatColor.LIGHT_PURPLE}Worth:")
            when (item.cost) {
                is me.danny.shop.data.Item.Cost.NotSet -> fields.add("${ChatColor.RED}  No price set!")
                is me.danny.shop.data.Item.Cost.Value -> {
                    fields.add("  ${ChatColor.YELLOW}Buy: ${ChatColor.GRAY}\$%.2f".format(item.cost.buy))
                    fields.add("  ${ChatColor.YELLOW}Sell: ${ChatColor.GRAY}\$%.2f".format(item.cost.sell))
                }
            }

            when (item.cooldown) {
                is me.danny.shop.data.Item.Cooldown.None -> fields.add("${ChatColor.LIGHT_PURPLE}Cooldown: ${ChatColor.DARK_GREEN}None")
                is me.danny.shop.data.Item.Cooldown.Infinite -> fields.add("${ChatColor.LIGHT_PURPLE}Cooldown: ${ChatColor.DARK_RED}Purchasable only once")
                is me.danny.shop.data.Item.Cooldown.Duration -> fields.add(
                    "${ChatColor.LIGHT_PURPLE}Cooldown: ${ChatColor.GRAY}%d%s".format(
                        item.cooldown.time.time,
                        item.cooldown.time.suffix
                    )
                )
            }

            val display: MutableList<String> = fields.map { field ->
                "${ChatColor.GOLD}| %s ${ChatColor.GOLD}|".format(field.padEnd(40))
            }.toMutableList()
            display.add(0, header)
            display.add(footer)
            return Item.addLore(tagged, *display.toTypedArray())
        }
    }

    class CategoryPage(coll: Collection<Category>, private var selected: Category, buttons: Pair<Int, Int>) :
        Page<Category>(coll, Pair(0, 0), Pair(1, 6), buttons) {
        override fun numPages(): Int = items.size - size + 1

        override fun display(inv: Inventory) {
            items
                .drop(page)
                .take(size)
                .map {
                    var item = Item.makeItem(it.display, "${ChatColor.BLUE}${it.name}")
                    if (it == selected) item = Item.addEnchantGlow(item)
                    item
                }
                .forEachIndexed { index, item -> inv.setItem(index * 9, item) }
        }

        fun selected(): Category = selected

        fun displayedCategories(): List<Category> = items.drop(page).take(size)

        fun changeCategory(category: Category) {
            selected = category
        }
    }
}