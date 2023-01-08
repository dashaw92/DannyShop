package me.danny.shop.inv

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class Menu(private var rows: Int, title: String, val viewer: Player) : InventoryHolder {
    companion object {
        @JvmStatic
        protected val prefix = "${ChatColor.DARK_RED}DannyShop ${ChatColor.GRAY}"
        protected val openInvs: MutableMap<UUID, Menu> = mutableMapOf()

        fun closeOpenInvs() {
            openInvs.keys.map(Bukkit::getPlayer)
                .filterNotNull()
                .forEach(Player::closeInventory)
            openInvs.clear()
        }

        fun openInv(id: UUID, menu: Menu) {
            openInvs += id to menu
        }
    }

    protected var inv: Inventory

    init {
        rows = 6.coerceAtMost(1.coerceAtLeast(rows))
        this.inv = Bukkit.createInventory(this, rows * 9, "$prefix$title")
        viewer.openInventory(inv)
    }

    fun close() {
        openInvs.remove(viewer.uniqueId)
    }

    override fun getInventory(): Inventory = inv

    protected abstract fun build()
    abstract fun onClick(event: InventoryClickEvent)
}

/**
 * Menus that implement this marker interface accept
 * click events from the entire inventory, not just
 * the custom menu's inventory.
 */
interface FullInvListener

fun Inventory.fill(filler: ItemStack) {
    (0 until size).forEach { setItem(it, filler) }
}