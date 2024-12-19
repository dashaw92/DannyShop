package me.danny.shop.inv

import me.danny.shop.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionType

/**
 * Represents a rectangle in an inventory
 * with support for paging via the sizes provided,
 * along with buttons to navigate the pages
 */
internal abstract class Page<T>(
    var items: Collection<T>,
    val start: Pair<Int, Int>,
    val dim: Pair<Int, Int>,
    private val buttons: Pair<Int, Int>
) {
    protected var page = 0
    protected val size = dim.first * dim.second

    /**
     * Current page
     */
    fun page() = page

    /**
     * Total number of pages
     * Overrideable if needed
     */
    open fun numPages(): Int = 1 + items.size / size

    /**
     * Go to the next page or do nothing if already at the last page
     */
    private fun nextPage(): Boolean {
        if (page + 1 >= numPages()) return false
        page += 1
        return true
    }

    /**
     * Go to the previous page or do nothing if already at the first page
     */
    private fun prevPage(): Boolean {
        if (page == 0) return false
        page -= 1
        return true
    }

    /**
     * Aggregation of clearing, displaying, and rendering buttons
     */
    fun render(inv: Inventory) {
        clearDisplay(inv)
        display(inv)
        drawButtons(inv)
    }

    /**
     * Clear the rectangle represented by this Page with a filler item.
     * Can be customized by overriding [fillerItem]
     */
    private fun clearDisplay(inv: Inventory) {
        renderRect(inv, fillerItem())
    }

    fun renderRect(inv: Inventory, item: ItemStack) {
        for (y in start.second until start.second + dim.second) {
            for (x in start.first until start.first + dim.first) {
                inv.setItem(y * 9 + x, item)
            }
        }
    }

    /**
     * The item that should be used to clear the Page in [clearDisplay]
     */
    open fun fillerItem(): ItemStack = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")

    /**
     * Provide custom rendering logic to render the page.
     * Sample code to render a rectangle:
     * ```
     * val invIdx = start.second * 9 + start.first
     * val displayed = items.drop(page * size).take(size)
     * for(item in displayed) {
     *  inv.setItem(invIdx, item)
     *  invIdx += 1
     *  if (invIdx % 9 == 0) invIdx += 2
     * }
     * ```
     */
    protected abstract fun display(inv: Inventory)

    /**
     * Renders the previous and next page buttons (if needed)
     */
    private fun drawButtons(inv: Inventory) {
        val prevPage = ItemBuilder.makeTippedArrow("&cPrevious", PotionType.HEALING)
        val nextPage = ItemBuilder.makeTippedArrow("&2Next", PotionType.LEAPING)
        if (page() > 0) inv.setItem(buttons.first, prevPage)
        if (page() + 1 < numPages()) inv.setItem(buttons.second, nextPage)
    }

    /**
     * Handles previous and next page button clicks
     * Must be called from the menu's onClick
     */
    fun onClick(event: InventoryClickEvent, callback: Runnable) {
        when (event.slot) {
            buttons.first -> {
                if (prevPage()) callback.run()
            }

            buttons.second -> {
                if (nextPage()) callback.run()
            }
        }
    }
}