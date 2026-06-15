package me.danny.shop.inv.shop

import me.danny.shop.*
import me.danny.shop.data.Key
import me.danny.shop.data.hasKey
import me.danny.shop.data.keyValue
import me.danny.shop.economy.Economy
import me.danny.shop.input.Input
import me.danny.shop.inv.*
import me.danny.shop.inv.LoreList.toEntry
import me.danny.shop.inv.editor.categories.CategoryEditor.Companion.CATEGORY_KEY
import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.shop.items.FilterType
import me.danny.shop.inv.shop.items.ItemPage
import me.danny.shop.inv.shop.purchasing.PurchaseMenu
import me.danny.shop.model.Category
import me.danny.shop.model.Item
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.color
import me.danny.shop.utils.fill
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

internal class ShopMenu(viewer: Player, shopReturnInfo: ShopReturnInfo? = null) : Menu(6, "", viewer), RefreshPlease {

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

        val searchButton = ItemBuilder.makeItem(
            Material.SPYGLASS, "&eSearch",
            *when (itemPage.query) {
                null -> arrayOf("&9No query")
                else -> arrayOf("&9Current: &7${itemPage.query}")
            },
            "",
            "&e[Search: Click]",
            "&e[Reset: Right click]"
        )
        val typeFilter = ItemBuilder.makeItem(
            Material.HOPPER, "&eItem Filter", *LoreList.makeList(
                listOf(
                    FilterType.All toEntry listOf("Displaying everything"),
                    FilterType.Materials toEntry listOf("Displaying only raw materials"),
                    FilterType.Items toEntry listOf("Displaying only custom items"),
                ), itemPage.filterType
            )
        )
        inv.setItem(inv.size - 7, searchButton)
        inv.setItem(inv.size - 6, typeFilter)

        refresh()

        viewer.openInventory(inv)
    }

    override fun refresh() {
        //Hide categories when the player cannot see them anymore
        if (!categoryPage.selected.isVisible(viewer)) {
            val changeTo = categories.firstOrNull { it.isVisible(viewer) }
            //If no categories are visible to this player,
            //or there are no categories in the shop,
            //display the empty shop (changes inventory size to 27)
            if (changeTo == null) {
                showEmptyShop()
                return
            }

            //Otherwise, change the display to reflect
            //the new category
            itemPage.changeCategory(changeTo)
            categoryPage.changeCategory(changeTo)

            //And ensure the inventory is the correct size with the new title
            rebuildInv()
            build()
            return
        }

        //If the player gains access to a previously
        //inaccessible category, or a new one was added,
        //reset the inventory size from the empty shop
        if (inv.size == 27) {
            rebuildInv()
            build()
            return
        }

        //Render the items and categories
        itemPage.render(inv)
        categoryPage.render(inv)
    }

    private fun showEmptyShop() {
        if (inv.size != 27) {
            inv = Bukkit.createInventory(this, 27, "$prefix- &8Uh oh!".color())
            val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
            var notice = ItemBuilder.makeItem(
                Material.REDSTONE_TORCH, "&eThe shop is empty!"
            )

            if (viewer.hasPermission(Perm.ADMIN)) {
                notice = ItemBuilder.addLore(
                    notice,
                    "&7But don't worry! Creating a shop is simple!",
                    "&7Check out the command &9/dannyshop import&e.",
                )
            }
            inv.fill(filler)
            inv.setItem(13, notice)
            viewer.openInventory(inv)
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        val clicked = event.currentItem!!

        //<editor-fold desc="Purchasing: Clicked a shop item">
        if (clicked.hasKey(ITEM_KEY)) {
            val iid = event.currentItem?.keyValue(ITEM_KEY) ?: ""
            if (iid.trim().isBlank()) return

            val item = shop.itemByIid(iid) ?: return

            val returnInfo = ShopReturnInfo(itemPage, categoryPage)
            if (event.click == ClickType.SHIFT_LEFT && viewer.hasPermission(Perm.ADMIN)) {
                ItemEditor(viewer, item.iid, returnInfo)
            } else {
                if (!Economy.hasEconomy()) {
                    viewer.pluginMsg("&cCannot purchase this! No economy is active!")
                    return
                }

                if (!item.category.isVisible(viewer)) {
                    viewer.pluginMsg("&cCannot purchase this! You lack permission!")
                    return
                }

                fun doPurchase() {
                    Economy.purchase(viewer, item.iid)
                }

                when (item.cost) {
                    is Item.Cost.Value -> doPurchase()
                    else -> {
                        if (viewer.hasPermission(Perm.ADMIN)) doPurchase()
                        else viewer.pluginMsg("&cCannot purchase this! No price is set.")
                    }
                }
            }
            return
        }
        //</editor-fold>
        //<editor-fold desc="Switching categories: Clicked a category display icon">
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
        //</editor-fold>
        //<editor-fold desc="Shop menu controls (filter, search, etc)">
        when (clicked.type) {
            Material.HOPPER -> {
                val filterType = when (event.click) {
                    ClickType.RIGHT -> itemPage.filterType.prev()
                    else -> itemPage.filterType.next()
                }
                itemPage.setFilter(filterType)
                build()
                return
            }

            Material.SPYGLASS -> {
                if (event.click.isRightClick) {
                    itemPage.searchFor(null)
                    rebuildInv()
                    build()
                    return
                }

                viewer.closeInventory()
                askInput("Search")
                    .getInput(viewer) { _, input -> changeSearchQuery(input) }
                return
            }

            else -> {}
        }
        //</editor-fold>
        //<editor-fold desc="Handle switching item and category pages">
        fun updateAndBuild() {
            rebuildInv()
            build()
        }

        itemPage.onClick(event, ::updateAndBuild)
        categoryPage.onClick(event, ::updateAndBuild)
        //</editor-fold>
    }

    private fun changeSearchQuery(query: Input) {
        val newQuery = query.collapse()

        itemPage.searchFor(newQuery.ifBlank { null })
        viewer.openInventory(inv)
        rebuildInv()
        build()
    }

    data class ShopReturnInfo(val itemPage: ItemPage, val categoryPage: CategoryPage)
}