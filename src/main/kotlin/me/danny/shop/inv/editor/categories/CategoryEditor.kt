package me.danny.shop.inv.editor.categories

import me.danny.shop.data.Key
import me.danny.shop.inv.FullInvListener
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.StateMenu
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

internal class CategoryEditor(player: Player) :
    StateMenu(5, "- &9Category Editor", player),
    FullInvListener {

    init {
        build()
    }

    override fun loadView(): MenuView = CategoryListingView()

    companion object {
        internal val CATEGORY_KEY = Key(NamespacedKey("dannyshop", "category"), PersistentDataType.STRING)
    }

}