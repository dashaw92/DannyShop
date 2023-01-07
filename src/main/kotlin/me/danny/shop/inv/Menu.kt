package me.danny.shop.inv

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.UUID

abstract class Menu(var rows: Int, var title: String, protected val viewer: Player) : InventoryHolder {
    companion object {
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
        if(title.length > 32) title = title.substring(0, 32)
        rows = 6.coerceAtLeast(1.coerceAtMost(rows))
        this.inv = Bukkit.createInventory(this, rows * 9, title)
        viewer.openInventory(inv)
    }

    fun close() {
        openInvs.remove(viewer.uniqueId)
    }

    override fun getInventory(): Inventory = inv

    protected abstract fun build()
    abstract fun onClick(event: InventoryClickEvent)
}