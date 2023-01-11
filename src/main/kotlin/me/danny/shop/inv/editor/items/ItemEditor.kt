package me.danny.shop.inv.editor.items

import me.danny.shop.data.Item
import me.danny.shop.me.danny.shop.inv.HotbarSlotListener
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.me.danny.shop.inv.view.MenuView
import me.danny.shop.me.danny.shop.inv.view.StateMenu
import org.bukkit.entity.Player

class ItemEditor(viewer: Player, val item: Item.IID, val returnInfo: ShopMenu.ShopReturnInfo) :
    StateMenu(3, "- &9Item Editor", viewer),
    HotbarSlotListener {

    init {
        build()
    }

    override fun loadView(): MenuView = ItemEditorView(this)

}