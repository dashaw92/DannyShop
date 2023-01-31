package me.danny.shop.inv.shop

import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.editor.categories.CategoryEditor.Companion.CATEGORY_KEY
import org.bukkit.entity.*
import org.bukkit.inventory.*

class CategoryPage(
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
                    ItemBuilder.addAttribute(ItemBuilder.makeItem(it.display, "&9${it.name}"), *ItemFlag.values())
                if (it == selected) item = ItemBuilder.addEnchantGlow(item)
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