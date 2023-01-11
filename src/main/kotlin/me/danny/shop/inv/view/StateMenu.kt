package me.danny.shop.me.danny.shop.inv.view

import me.danny.shop.inv.Menu
import me.danny.shop.inv.view.ViewAction
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Represents a menu that utilizes the view system to render
 * Does all of the plumbing to set it up
 */
abstract class StateMenu(size: Int, title: String, viewer: Player) : Menu(size, title, viewer) {

    var view: MenuView = this.loadView()

    /**
     * Return the default view of this menu
     */
    abstract fun loadView(): MenuView

    /**
     * If you override this method, ensure you
     * either call this version with super.build(),
     * or copy what this implementation does
     */
    override fun build() {
        inv.clear()
        view.build(inv)
    }

    /**
     * You should perform all actions in the view's onClick
     */
    final override fun onClick(event: InventoryClickEvent) {
        when (val outcome = view.onClick(inv, event)) {
            is ViewAction.ChangeView -> {
                view = outcome.newView; build()
            }

            is ViewAction.Resize -> {
                val newRows = outcome.rows.coerceIn(1, 6)
                val title = "$prefix${(outcome.title ?: event.view.title)}"

                inv = Bukkit.createInventory(this, newRows * 9, title)
                build()
                viewer.openInventory(inv)
            }

            else -> {}
        }
    }

}