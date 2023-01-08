package me.danny.shop.inv.editor.items

import me.danny.shop.data.Item
import me.danny.shop.inv.Menu
import me.danny.shop.inv.MenuView
import me.danny.shop.inv.root.ShopMenu
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ItemEditor(viewer: Player, val item: Item, val returnInfo: ShopMenu.ShopReturnInfo) :
    Menu(3, "- ${ChatColor.BLUE}Item Editor", viewer) {

    private var view: MenuView = EditorMainView(this)

    init {
        build()
    }

    override fun build() {
        view.build(inv)
    }

    override fun onClick(event: InventoryClickEvent) {
        when (val outcome = view.onClick(inv, event)) {
            is MenuView.ViewAction.ChangeView -> {
                view = outcome.newView; build()
            }

            else -> {}
        }
    }


}