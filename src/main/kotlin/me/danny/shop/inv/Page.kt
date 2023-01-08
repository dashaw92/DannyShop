package me.danny.shop.inv

import org.bukkit.ChatColor
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.potion.PotionType

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

    fun nextPage() {
        if (page + 1 >= numPages()) return
        page += 1
    }

    fun prevPage() {
        if (page == 0) return
        page -= 1
    }

    fun render(inv: Inventory) {
        clearDisplay(inv)
        display(inv)
        drawButtons(inv)
    }

    private fun clearDisplay(inv: Inventory) {
        for (y in start.second until start.second + dim.second) {
            for (x in start.first until start.first + dim.first) {
                inv.setItem(y * 9 + x, null)
            }
        }
    }

    protected abstract fun display(inv: Inventory)

    private fun drawButtons(inv: Inventory) {
        val prevPage = ItemBuilder.makeTippedArrow("${ChatColor.RED}Previous", PotionType.INSTANT_HEAL)
        val nextPage = ItemBuilder.makeTippedArrow("${ChatColor.DARK_GREEN}Next", PotionType.JUMP)
        if (page() > 0) inv.setItem(buttons.first, prevPage)
        if (page() + 1 < numPages()) inv.setItem(buttons.second, nextPage)
    }

    fun onClick(event: InventoryClickEvent, callback: () -> Unit) {
        when (event.slot) {
            buttons.first -> {
                prevPage(); callback()
            }

            buttons.second -> {
                nextPage(); callback()
            }
        }
    }
}