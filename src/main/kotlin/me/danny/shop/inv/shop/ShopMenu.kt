package me.danny.shop.me.danny.shop.inv.shop

import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.data.Item
import me.danny.shop.economy.*
import me.danny.shop.inv.*
import me.danny.shop.inv.LoreList.toEntry
import me.danny.shop.inv.editor.categories.CategoryEditor.Companion.CATEGORY_KEY
import me.danny.shop.inv.editor.items.*
import me.danny.shop.inv.shop.*
import me.danny.shop.inv.shop.purchasing.*
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.persistence.*

class ShopMenu(viewer: Player, shopReturnInfo: ShopReturnInfo? = null) : Menu(6, "", viewer) {

    companion object {
        internal val ITEM_KEY = Key("item_iid", PersistentDataType.STRING)
    }

    private val shop = DannyShop.SHOP
    private var categories: List<Category> = shop.categories().take(6)
    private val selected = categories.firstOrNull() ?: Category("All", Material.CHEST)
    private var itemPage: ItemPage =
        shopReturnInfo?.itemPage ?: ItemPage(viewer, shop.items(selected), Pair(inv.size - 2, inv.size - 1))
    private var categoryPage: CategoryPage =
        shopReturnInfo?.categoryPage ?: CategoryPage(shop.categories(), selected, Pair(1, 46))
    private var filterType = shopReturnInfo?.filter ?: FilterType.All

    init {
        build()
    }

    override fun build() {
        if (shop.isEmpty()) {
            showEmptyShop()
            return
        }

        rebuildInv()

        val catBorder = ItemBuilder.makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
        listOf(1, 10, 19, 28, 37, 46)
            .forEach { inv.setItem(it, catBorder) }
        val ctrlBorder = ItemBuilder.makeItem(Material.BLUE_STAINED_GLASS_PANE, " ")
        (47 until inv.size)
            .forEach { inv.setItem(it, ctrlBorder) }

        val filterButton = ItemBuilder.makeItem(Material.HOPPER, "&6Item Filter", *filterButton())

        inv.setItem(inv.size - 5, filterButton)

        categoryPage.render(inv)
        itemPage.render(inv)

        viewer.openInventory(inv)
    }

    private fun rebuildInv() {
        val page = itemPage.page() + 1
        val maxPages = itemPage.numPages()
        inv = Bukkit.createInventory(
            this, 6 * 9,
            "$prefix- ${ChatColor.BLUE}${categoryPage.selected().name} ${ChatColor.DARK_GRAY}(%d/%d)".format(
                page,
                maxPages
            )
        )
    }

    private fun showEmptyShop() {
        inv = Bukkit.createInventory(this, 27, "$prefix- &8Uh oh!".color())
        val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
        val notice = ItemBuilder.makeItem(
            Material.REDSTONE_TORCH, "&6The shop is empty!",
            "&eBut don't worry! Creating a shop is simple!",
            "&eCheck out the command &d/dannyshop import&e.",
        )
        inv.fill(filler)
        inv.setItem(13, notice)
        viewer.openInventory(inv)
    }

    override fun onClick(event: InventoryClickEvent) {
        val clicked = event.currentItem!!
        if (clicked.hasKey(ITEM_KEY)) {
            val iid = event.currentItem?.keyValue(ITEM_KEY) ?: ""
            if (iid.trim().isBlank()) return

            val item = shop.itemByIid(iid) ?: return

            val returnInfo = ShopReturnInfo(itemPage, categoryPage, filterType)
            if (event.click == ClickType.SHIFT_LEFT && viewer.hasPermission("dannyshop.admin")) {
                ItemEditor(viewer, item.iid, returnInfo)
            } else {
                if (!Economy.hasEconomy()) {
                    viewer.sendMessage("&6[DannyShop] &cCannot purchase this! No economy is active!".color())
                    return
                }

                when (item.cost) {
                    is Item.Cost.Value -> {
                        if (event.click == ClickType.RIGHT) PurchaseMenu(viewer, item.iid, returnInfo)
                        else Economy.purchase(viewer, item.iid, item.cost.buy)
                    }

                    else -> viewer.sendMessage("&6[DannyShop] &cCannot purchase this! No price is set.".color())
                }
            }
            return
        }

        if (clicked.type == Material.HOPPER) {
            filterType = when (event.click) {
                ClickType.RIGHT -> filterType.previous()
                else -> filterType.next()
            }
            itemPage.setFilter(filterType)
            build()
            return
        }

        when {
            event.slot % 9 == 0 -> {
                if (!clicked.hasKey(CATEGORY_KEY)) return

                val row = event.slot / 9
                val selected = categoryPage.displayedCategories()[row]
                itemPage.changeCategory(selected)
                categoryPage.changeCategory(selected)
                build()
                return
            }
        }

        itemPage.onClick(event, ::build)
        categoryPage.onClick(event, ::build)
    }

    data class ShopReturnInfo(val itemPage: ItemPage, val categoryPage: CategoryPage, val filter: FilterType)

    private fun filterButton(): Array<out String> {
        return LoreList.makeList(
            listOf(
                FilterType.All toEntry listOf("Displaying everything"),
                FilterType.Materials toEntry listOf("Displaying only raw materials"),
                FilterType.Items toEntry listOf("Displaying only custom items"),
                FilterType.Commands toEntry listOf("Displaying only commands"),
                FilterType.Experience toEntry listOf("Displaying only experience packs"),
            ), filterType
        )
    }

    enum class FilterType {
        All,
        Materials,
        Items,
        Commands,
        Experience;

        fun previous(): FilterType = when (this) {
            All -> Experience
            Experience -> Commands
            Commands -> Items
            Items -> Materials
            Materials -> All
        }

        fun next(): FilterType = when (this) {
            Experience -> All
            Commands -> Experience
            Items -> Commands
            Materials -> Items
            All -> Materials
        }
    }
}