package me.danny.shop.inv

import org.bukkit.ChatColor
import org.bukkit.inventory.Inventory
import org.bukkit.potion.PotionType

sealed class Page<T>(
    var items: Collection<T>,
    val start: Pair<Int, Int>,
    dim: Pair<Int, Int>,
    private val buttons: Pair<Int, Int>
) {
    protected var page = 0
    protected val size = dim.first * dim.second

    fun page() = page
    open fun numPages(): Int = 1 + items.size / size

    fun nextPage() {
        if (page + 1 == numPages()) return
        page += 1
    }

    fun prevPage() {
        if (page == 0) return
        page -= 1
    }

    fun render(inv: Inventory) {
        display(inv)
        drawButtons(inv)
    }

    protected abstract fun display(inv: Inventory)

    private fun drawButtons(inv: Inventory) {
        val prevPage = Item.makeTippedArrow("${ChatColor.RED}Previous", PotionType.INSTANT_HEAL)
        val nextPage = Item.makeTippedArrow("${ChatColor.DARK_GREEN}Next", PotionType.JUMP)
        if (page() > 0) inv.setItem(buttons.first, prevPage)
        if (page() + 1 < numPages()) inv.setItem(buttons.second, nextPage)
    }
}