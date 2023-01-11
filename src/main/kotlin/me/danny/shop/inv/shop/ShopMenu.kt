package me.danny.shop.me.danny.shop.inv.shop

import me.danny.shop.data.Category
import me.danny.shop.data.Item.*
import me.danny.shop.data.Shop
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Menu
import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.shop.CategoryPage
import me.danny.shop.me.danny.shop.data.Key
import me.danny.shop.me.danny.shop.data.hasKey
import me.danny.shop.me.danny.shop.data.keyValue
import me.danny.shop.me.danny.shop.inv.fill
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

class ShopMenu(private val shop: Shop, viewer: Player, shopReturnInfo: ShopReturnInfo? = null) : Menu(6, "", viewer) {

    companion object {
        @Suppress("DEPRECATION")
        internal val ITEM_KEY = Key(NamespacedKey("dannyshop", "item_iid"), PersistentDataType.STRING)
    }

    private var categories: List<Category> = shop.categories().take(6)
    private val selected = categories.firstOrNull() ?: Category("All", Material.CHEST)
    private var itemPage: ItemPage =
        shopReturnInfo?.itemPage ?: ItemPage(viewer, shop.items(selected), Pair(inv.size - 2, inv.size - 1))
    private var categoryPage: CategoryPage =
        shopReturnInfo?.categoryPage ?: CategoryPage(shop.categories(), selected, Pair(1, 46))

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
        inv = Bukkit.createInventory(this, 27, "- ${ChatColor.DARK_GRAY}Uh oh!")
        val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
        val notice = ItemBuilder.makeItem(
            Material.REDSTONE_TORCH, "${ChatColor.GOLD}The shop is empty!",
            "${ChatColor.YELLOW}But don't worry! Creating a shop is simple!",
            "${ChatColor.YELLOW}Check out the command ${ChatColor.LIGHT_PURPLE}/dannyshop import${ChatColor.YELLOW}.",
        )
        inv.fill(filler)
        inv.setItem(13, notice)
        viewer.openInventory(inv)
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.currentItem!!.hasKey(ITEM_KEY)) {
            val iid = event.currentItem?.keyValue(ITEM_KEY) ?: ""
            if (iid.trim().isBlank()) return

            val item = shop.itemByIid(iid) ?: return

            val returnInfo = ShopReturnInfo(itemPage, categoryPage)
            if (event.click.isRightClick && viewer.hasPermission("dannyshop.admin"))
                ItemEditor(viewer, item, returnInfo)
            else
                PurchaseMenu(viewer, item, returnInfo)
            return
        }

        when {
            event.slot % 9 == 0 -> {
                val row = event.slot / 9
                val selected = categoryPage.displayedCategories()[row]
                itemPage.changeCategory(shop, selected)
                categoryPage.changeCategory(selected)
                build()
                return
            }
        }

        itemPage.onClick(event, ::build)
        categoryPage.onClick(event, ::build)
    }

    data class ShopReturnInfo(val itemPage: ItemPage, val categoryPage: CategoryPage)
}