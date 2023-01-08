package me.danny.shop.inv

import me.danny.shop.DannyShop
import me.danny.shop.data.Category
import me.danny.shop.data.Shop
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

class CategoryEditor(player: Player) :
    Menu(4, "${ChatColor.DARK_RED}DannyShop ${ChatColor.GRAY}- ${ChatColor.BLUE}Category Editor", player),
    FullInvListener {

    companion object {
        @Suppress("DEPRECATION")
        private val CATEGORY_KEY = NamespacedKey("dannyshop", "category")
    }

    private var view: MenuView = CategoryListing(inv)

    init {
        build()
    }

    override fun build() {
        inv.clear()
        view.build(inv)
    }

    override fun onClick(event: InventoryClickEvent) {
        when (val outcome = view.onClick(inv, event)) {
            is MenuView.ViewAction.ChangeView -> {
                view = outcome.newView; build()
            }

            else -> {}
        }
    }

    private class CategoryListing(inv: Inventory) : MenuView() {
        private val page = CategoryPages(DannyShop.SHOP.categories(), Pair(inv.size - 2, inv.size - 1))

        override fun build(inv: Inventory) {
            page.render(inv)
        }

        override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
            when {
                event.slot == inv.size - 2 -> {
                    page.prevPage(); build(inv)
                }

                event.slot == inv.size - 1 -> {
                    page.nextPage(); build(inv)
                }

                event.currentItem != null && Item.hasKey(
                    event.currentItem!!,
                    CATEGORY_KEY,
                    PersistentDataType.STRING
                ) -> {
                    val name = Item.getValue(event.currentItem!!, CATEGORY_KEY, PersistentDataType.STRING, "")
                    if (name.trim().isBlank()) return ViewAction.Pass

                    val category = Shop.getCategory(name) ?: return ViewAction.Pass
                    return ViewAction.ChangeView(EditorView(category))
                }
            }

            return ViewAction.Pass
        }

        private class CategoryPages(coll: Collection<Category>, buttons: Pair<Int, Int>) :
            Page<Category>(coll, Pair(0, 0), Pair(9, 3), buttons) {
            override fun display(inv: Inventory) {
                var invIdx = start.second * 9 + start.second
                for (item in items
                    .drop(page * size)
                    .take(size)
                    .map {
                        Item.attachKey(
                            Item.makeItem(it.display, "${ChatColor.BLUE}${it.name}"),
                            CATEGORY_KEY,
                            PersistentDataType.STRING,
                            it.name
                        )
                    }) {
                    inv.setItem(invIdx, item)
                    invIdx += 1
                }
            }
        }

    }

    private class EditorView(val category: Category) : MenuView() {
        override fun build(inv: Inventory) {
            val filler = Item.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
            (0 until inv.size)
                .forEach { inv.setItem(it, filler) }

            inv.setItem(
                13, Item.makeItem(
                    category.display, "${ChatColor.BLUE}${category.name}",
                    "${ChatColor.YELLOW}Click an item to change the icon"
                )
            )
            inv.setItem(inv.size - 1, Item.makeItem(Material.ARROW, "${ChatColor.BLUE}Back"))
        }

        override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
            if (event.slot == inv.size - 1) return ViewAction.ChangeView(CategoryListing(inv))
            if (event.clickedInventory == inv) return ViewAction.Pass

            category.changeDisplay(event.currentItem!!.type)
            build(inv)
            return ViewAction.Pass
        }

    }
}