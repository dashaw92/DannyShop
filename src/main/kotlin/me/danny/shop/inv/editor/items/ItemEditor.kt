package me.danny.shop.inv.editor.items

import me.danny.shop.data.Item
import me.danny.shop.inv.Menu
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.me.danny.shop.inv.view.MenuView
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ItemEditor(viewer: Player, val item: Item, val returnInfo: ShopMenu.ShopReturnInfo) :
    Menu(3, "- ${ChatColor.BLUE}Item Editor", viewer) {

    private var view: MenuView = ItemEditorView(this)

    init {
        build()
    }

    override fun build() {
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