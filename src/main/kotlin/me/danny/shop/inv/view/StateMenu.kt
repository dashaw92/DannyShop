package me.danny.shop.inv.view

import me.danny.shop.inv.Menu
import me.danny.shop.utils.color
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Represents a menu that utilizes the view system to render
 * Does all of the plumbing to set it up
 */
internal abstract class StateMenu(size: Int, private val title: String, viewer: Player) : Menu(size, title, viewer) {

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
                view = outcome.newView

                when (val open = view.onOpen()) {
                    is ViewAction.Resize -> handleResize(open)
                    else -> {} //ignore all other actions
                }

                build()
            }

            is ViewAction.Resize -> handleResize(outcome)
            is ViewAction.Refresh -> build()
            else -> {}
        }
    }

    private fun handleResize(resize: ViewAction.Resize) {
        val newRows = resize.rows.coerceIn(1, 6)
        val newTitle = when (resize.title) {
            null -> title
            else -> resize.title
        }
        inv = Bukkit.createInventory(
            this,
            newRows * 9,
            "$prefix$newTitle".color()
        )
        build()
        viewer.openInventory(inv)
    }
}