package me.danny.shop.inv.editor.categories

import me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.data.Key
import me.danny.shop.me.danny.shop.inv.FullInvListener
import me.danny.shop.me.danny.shop.inv.view.MenuView
import me.danny.shop.me.danny.shop.inv.view.StateMenu
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class CategoryEditor(player: Player) :
    StateMenu(5, "- ${ChatColor.BLUE}Category Editor", player),
    FullInvListener {

    init {
        build()
    }

    override fun loadView(): MenuView = CategoryListingView()

    companion object {
        @Suppress("DEPRECATION")
        internal val CATEGORY_KEY = Key(NamespacedKey("dannyshop", "category"), PersistentDataType.STRING)
    }

}