package me.danny.shop.inv.shop

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.data.attachKey
import me.danny.shop.inv.Page
import me.danny.shop.inv.editor.categories.CategoryEditor.Companion.CATEGORY_KEY
import me.danny.shop.model.Category
import me.danny.shop.utils.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag

internal class CategoryPage(
    private val viewer: Player,
    coll: Collection<Category>,
    internal var selected: Category,
    buttons: Pair<Int, Int>
) :
    Page<Category>(coll, Pair(0, 0), Pair(1, 6), buttons) {
    override fun numPages(): Int {
        if (items.size <= dim.second) return 0
        return items.size - size + 1
    }

    override fun display(inv: Inventory) {
        displayedCategories()
            .map {
                var item =
                    ItemBuilder.makeItem(
                        it.display, "&e${it.name}",
                        "&6Items: &7${DannyShop.SHOP.items(it.cid).size}",
                    )
                        .let { i -> ItemBuilder.addAttribute(i, *ItemFlag.entries.toTypedArray()) }
                if (it == selected) item = ItemBuilder.addEnchantGlow(item)

                if (viewer.hasPermission(Perm.ADMIN)) {
                    item = ItemBuilder.addLore(item, "&9Permission: &e&o${it.permission ?: "&2None"}")
                }

                item.attachKey(CATEGORY_KEY, it.name)
            }
            .forEachIndexed { index, item -> inv.setItem(index * 9, item) }
    }

    fun selected(): Category = selected

    fun displayedCategories(): List<Category> =
        items
            .filter { it.isVisible(viewer) }
            .drop(page)
            .take(size)

    fun changeCategory(category: Category) {
        selected = category
    }
}