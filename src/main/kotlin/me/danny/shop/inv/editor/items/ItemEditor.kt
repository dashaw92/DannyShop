package me.danny.shop.inv.editor.items

import me.danny.shop.data.*
import me.danny.shop.inv.view.*
import me.danny.shop.me.inv.shop.*
import org.bukkit.entity.*

class ItemEditor(viewer: Player, val item: ID, val returnInfo: ShopMenu.ShopReturnInfo) :
    StateMenu(3, "- &9Item Editor", viewer) {

    init {
        build()
    }

    override fun loadView(): MenuView = ItemEditorView(this)

}