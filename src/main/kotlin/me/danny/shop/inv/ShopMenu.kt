package me.danny.shop.inv

import me.danny.shop.data.Category
import me.danny.shop.data.Shop
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.potion.PotionType

class ShopMenu(private val shop: Shop, viewer: Player) : Menu(6, "${ChatColor.GOLD}DannyShop", viewer) {

    private var categories: List<Category> = shop.categories().take(6)
    private val itemPage: ItemPage
    private val categoryPage: CategoryPage

    init {
        val selected = categories.firstOrNull() ?: Category("All", Material.CHEST)
        itemPage = ItemPage(shop.items(selected))
        categoryPage = CategoryPage(shop.categories(), selected)
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

        categoryPage.display(inv)
        itemPage.display(inv)

        val prevItemPage = Item.makeTippedArrow("${ChatColor.RED}Previous page", PotionType.INSTANT_HEAL)
        val nextItemPage = Item.makeTippedArrow("${ChatColor.DARK_GREEN}Next page", PotionType.JUMP)

        if(itemPage.page() > 0) inv.setItem(inv.size - 2, prevItemPage)
        if(itemPage.page() + 1 < itemPage.numPages()) inv.setItem(inv.size - 1, nextItemPage)

        val catScrollUp = Item.makeTippedArrow("${ChatColor.RED}Previous categories", PotionType.INSTANT_HEAL)
        val catScrollDown = Item.makeTippedArrow("${ChatColor.DARK_GREEN}More categories", PotionType.JUMP)

        if(categoryPage.page() > 0) inv.setItem(1, catScrollUp)
        if(categoryPage.page() + 1 < categoryPage.numPages()) inv.setItem(46, catScrollDown)

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

    sealed class Page<T>(var items: Collection<T>, val startX: Int, val startY: Int, val width: Int, val height: Int) {
        protected var page = 0
        protected val size = width * height

        fun page() = page
        open fun numPages(): Int = 1 + items.size / size

        fun nextPage() {
            if(page + 1 == numPages()) return
            page += 1
        }

        fun prevPage() {
            if(page == 0) return
            page -= 1
        }

        abstract fun display(inv: Inventory)
    }

    class ItemPage(coll: Collection<me.danny.shop.data.Item>) : Page<me.danny.shop.data.Item>(coll, 2, 0, 7, 5) {
        override fun display(inv: Inventory) {
            var invIdx = startY * 9 + startX
            for(item in items.drop(page * size).take(size)) {
                inv.setItem(invIdx, item.item.display())
                invIdx += 1
                if(invIdx % 9 == 0) invIdx += 2
            }
        }

        fun changeCategory(shop: Shop, category: Category) {
            page = 0
            items = shop.items(category)
        }
    }

    class CategoryPage(coll: Collection<Category>, private var selected: Category) : Page<Category>(coll, 0, 0, 1, 6) {
        override fun numPages(): Int = items.size - size + 1

        override fun display(inv: Inventory) {
            items
                .drop(page)
                .take(size)
                .map {
                    var item = Item.makeItem(it.display, "${ChatColor.BLUE}${it.name}")
                    if(it == selected) item = Item.addEnchantGlow(item)
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