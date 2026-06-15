package me.danny.shop.inv.editor.items

import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.StateMenu
import me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.model.ID
import org.bukkit.entity.Player

internal class ItemEditor(viewer: Player, val item: ID, val returnInfo: ShopMenu.ShopReturnInfo) :
    StateMenu(3, "- &9Item Editor", viewer) {

    init {
        build()
    }

    override fun loadView(): MenuView = ItemEditorView(this)

}