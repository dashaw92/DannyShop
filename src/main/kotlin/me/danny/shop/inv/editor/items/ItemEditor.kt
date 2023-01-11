package me.danny.shop.inv.editor.items

import me.danny.shop.data.Item
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.me.danny.shop.inv.view.MenuView
import me.danny.shop.me.danny.shop.inv.view.StateMenu
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class ItemEditor(viewer: Player, val item: Item, val returnInfo: ShopMenu.ShopReturnInfo) :
    StateMenu(3, "- ${ChatColor.BLUE}Item Editor", viewer) {

    override fun loadView(): MenuView = ItemEditorView(this)

}