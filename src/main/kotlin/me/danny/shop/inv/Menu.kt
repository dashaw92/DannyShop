package me.danny.shop.inv

import me.danny.shop.*
import me.danny.shop.me.danny.shop.inv.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import java.util.*

/**
 * Base type for all GUIs in the plugin
 */
abstract class Menu(private var rows: Int, title: String, val viewer: Player) : InventoryHolder {
    companion object {
        @JvmStatic
        protected val prefix = "&4Shop &7".color()
        protected val openInvs: MutableMap<UUID, Menu> = mutableMapOf()

        fun closeOpenInvs() {
            openInvs.keys.mapNotNull(Bukkit::getPlayer)
                .forEach(Player::closeInventory)
            openInvs.clear()
        }

        fun openInv(id: UUID, menu: Menu) {
            openInvs += id to menu
        }

        init {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(DannyShop.instance(), {
                openInvs.values
                    .filterIsInstance<RefreshPlease>()
                    .forEach(RefreshPlease::refresh)
            }, 5L, 5L)
        }
    }

    protected var inv: Inventory

    init {
        rows = rows.coerceIn(1, 6)
        this.inv = Bukkit.createInventory(
            this,
            rows * 9,
            "$prefix$title".color()
        )
        viewer.openInventory(inv)
    }

    fun close() {
        openInvs.remove(viewer.uniqueId)
    }

    override fun getInventory(): Inventory = inv

    protected abstract fun build()
    abstract fun onClick(event: InventoryClickEvent)
}