package me.danny.shop.inv.editor.categories

import me.danny.shop.inv.*
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.me.danny.shop.inv.FullInvListener
import me.danny.shop.me.danny.shop.inv.view.MenuView
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 *
 */
class CategoryEditor(player: Player) :
    Menu(5, "- ${ChatColor.BLUE}Category Editor", player),
    FullInvListener {

    companion object {
        @Suppress("DEPRECATION")
        internal val CATEGORY_KEY = NamespacedKey("dannyshop", "category")
    }

    private var view: MenuView = CategoryListingView(inv)

    init {
        build()
    }

    override fun build() {
        inv.clear()
        view.build(inv)
    }

    override fun onClick(event: InventoryClickEvent) {
        when (val outcome = view.onClick(inv, event)) {
            is ViewAction.ChangeView -> {
                view = outcome.newView; build()
            }

            else -> {}
        }
    }

}