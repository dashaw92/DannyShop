package me.danny.shop.me.inv.shop

import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.data.Item
import me.danny.shop.data.Item.Quantities.Allowed.Any
import me.danny.shop.economy.*
import me.danny.shop.inv.*
import me.danny.shop.inv.LoreList.toEntry
import me.danny.shop.inv.editor.categories.CategoryEditor.Companion.CATEGORY_KEY
import me.danny.shop.inv.editor.items.*
import me.danny.shop.inv.shop.*
import me.danny.shop.inv.shop.purchasing.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.persistence.*

class ShopMenu(viewer: Player, shopReturnInfo: ShopReturnInfo? = null) : Menu(6, "", viewer), RefreshPlease {

    companion object {
        /**
         * Attached to items to retrieve their IID in onClick
         */
        internal val ITEM_KEY = Key("item_iid", PersistentDataType.STRING)
    }

    /**
     * Easier access to the shop
     */
    private val shop = DannyShop.SHOP

    private var categories: List<Category> = shop.categories().take(6)
    private val selected = categories.firstOrNull() ?: Category("All", Material.CHEST)

    /**
     * Handles the currently displayed shop items and category listing view
     * If the `shopReturnInfo` object provided isn't null, this will
     * be reset to a value from before.
     */
    private var itemPage: ItemPage =
        shopReturnInfo?.itemPage ?: ItemPage(
            viewer,
            shop.items(selected.cid),
            FilterType.All,
            Pair(inv.size - 2, inv.size - 1)
        )
    private var categoryPage: CategoryPage =
        shopReturnInfo?.categoryPage ?: CategoryPage(viewer, shop.categories(), selected, Pair(1, 46))

    init {
        rebuildInv()
        build()
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

    override fun build() {
        //Needed because otherwise, the shop gui would
        //just be an empty inventory (ugly)
        if (shop.isEmpty()) {
            showEmptyShop()
            return
        }

        //<editor-fold desc="Display border">
        val catBorder = ItemBuilder.makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
        listOf(1, 10, 19, 28, 37, 46)
            .forEach { inv.setItem(it, catBorder) }
        val ctrlBorder = ItemBuilder.makeItem(Material.BLUE_STAINED_GLASS_PANE, " ")
        (47 until inv.size)
            .forEach { inv.setItem(it, ctrlBorder) }
        //</editor-fold>

        val filterButton = ItemBuilder.makeItem(Material.HOPPER, "&6Item Filter", *filterButton())

        inv.setItem(inv.size - 5, filterButton)

        refresh()

        viewer.openInventory(inv)
    }

    override fun refresh() {
        if (!categoryPage.selected.isVisible(viewer)) {
            val changeTo = categories.firstOrNull { it.isVisible(viewer) }
            if (changeTo == null) {
                showEmptyShop()
                return
            }

            itemPage.changeCategory(changeTo)
            categoryPage.changeCategory(changeTo)
            rebuildInv()
            build()
            return
        }

        if (inv.size == 27) {
            rebuildInv()
            build()
        }

        itemPage.render(inv)
        categoryPage.render(inv)
    }

    private fun showEmptyShop() {
        if (inv.size != 27) {
            inv = Bukkit.createInventory(this, 27, "$prefix- &8Uh oh!".color())
            val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
            var notice = ItemBuilder.makeItem(
                Material.REDSTONE_TORCH, "&6The shop is empty!"
            )

            if (viewer.hasPermission("dannyshop.admin")) {
                notice = ItemBuilder.addLore(
                    notice,
                    "&eBut don't worry! Creating a shop is simple!",
                    "&eCheck out the command &d/dannyshop import&e.",
                )
            }
            inv.fill(filler)
            inv.setItem(13, notice)
            viewer.openInventory(inv)
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        val clicked = event.currentItem!!
        if (clicked.hasKey(ITEM_KEY)) {
            val iid = event.currentItem?.keyValue(ITEM_KEY) ?: ""
            if (iid.trim().isBlank()) return

            val item = shop.itemByIid(iid) ?: return

            val returnInfo = ShopReturnInfo(itemPage, categoryPage)
            if (event.click == ClickType.SHIFT_LEFT && viewer.hasPermission("dannyshop.admin")) {
                ItemEditor(viewer, item.iid, returnInfo)
            } else {
                if (!Economy.hasEconomy()) {
                    viewer.sendMessage("&6[DannyShop] &cCannot purchase this! No economy is active!".color())
                    return
                }

                if (!item.category.isVisible(viewer)) {
                    viewer.sendMessage("&6[DannyShop] &cCannot purchase this! You lack permission!".color())
                    return
                }

                fun doPurchase() {
                    if (event.click == ClickType.RIGHT && (item.quantities.allowed == Any || item.quantities.predefined.size > 1)) PurchaseMenu(
                        viewer,
                        item.iid,
                        returnInfo
                    )
                    else Economy.purchase(viewer, item.iid)
                }

                when (item.cost) {
                    is Item.Cost.Value -> doPurchase()
                    else -> {
                        if (viewer.hasPermission("dannyshop.admin")) doPurchase()
                        else viewer.sendMessage("&6[DannyShop] &cCannot purchase this! No price is set.".color())
                    }
                }
            }
            return
        }

        if (clicked.type == Material.HOPPER) {
            val filterType = when (event.click) {
                ClickType.RIGHT -> itemPage.filterType.prev()
                else -> itemPage.filterType.next()
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
                rebuildInv()
                build()
                return
            }
        }

        fun updateAndBuild() {
            rebuildInv()
            build()
        }

        itemPage.onClick(event, ::updateAndBuild)
        categoryPage.onClick(event, ::updateAndBuild)
    }

    data class ShopReturnInfo(val itemPage: ItemPage, val categoryPage: CategoryPage)

    private fun filterButton(): Array<out String> {
        return LoreList.makeList(
            listOf(
                FilterType.All toEntry listOf("Displaying everything"),
                FilterType.Materials toEntry listOf("Displaying only raw materials"),
                FilterType.Items toEntry listOf("Displaying only custom items"),
                FilterType.Commands toEntry listOf("Displaying only commands"),
                FilterType.Experience toEntry listOf("Displaying only experience packs"),
            ), itemPage.filterType
        )
    }

    /**
     * Controls what type of items are included in the item page
     */
    enum class FilterType {
        /**
         * Everything is displayed
         */
        All,

        /**
         * Only raw materials
         */
        Materials,

        /**
         * Only custom items
         */
        Items,

        /**
         * Only commands
         */
        Commands,

        /**
         * Only experience items
         */
        Experience
    }
}