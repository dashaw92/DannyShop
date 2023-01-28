package me.danny.shop.inv

import org.bukkit.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.potion.*

/**
 * Represents a rectangle in an inventory
 * with support for paging via the sizes provided,
 * along with buttons to navigate the pages
 */
abstract class Page<T>(
    var items: Collection<T>,
    val start: Pair<Int, Int>,
    val dim: Pair<Int, Int>,
    private val buttons: Pair<Int, Int>
) {
    protected var page = 0
    protected val size = dim.first * dim.second

    fun page() = page
    open fun numPages(): Int = 1 + items.size / size

    private fun nextPage(): Boolean {
        if (page + 1 >= numPages()) return false
        page += 1
        return true
    }

    private fun prevPage(): Boolean {
        if (page == 0) return false
        page -= 1
        return true
    }

    fun render(inv: Inventory) {
        clearDisplay(inv)
        display(inv)
        drawButtons(inv)
    }

    private fun clearDisplay(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        for (y in start.second until start.second + dim.second) {
            for (x in start.first until start.first + dim.first) {
                inv.setItem(y * 9 + x, filler)
            }
        }
    }

    protected abstract fun display(inv: Inventory)

    private fun drawButtons(inv: Inventory) {
        val prevPage = ItemBuilder.makeTippedArrow("&cPrevious", PotionType.INSTANT_HEAL)
        val nextPage = ItemBuilder.makeTippedArrow("&2Next", PotionType.JUMP)
        if (page() > 0) inv.setItem(buttons.first, prevPage)
        if (page() + 1 < numPages()) inv.setItem(buttons.second, nextPage)
    }

    fun onClick(event: InventoryClickEvent, callback: () -> Unit) {
        when (event.slot) {
            buttons.first -> {
                if (prevPage()) callback()
            }

            buttons.second -> {
                if (nextPage()) callback()
            }
        }
    }
}