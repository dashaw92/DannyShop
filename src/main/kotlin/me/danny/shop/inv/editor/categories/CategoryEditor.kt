package me.danny.shop.inv.editor.categories

import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.view.*
import me.danny.shop.model.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.persistence.*

class CategoryEditor(player: Player) :
    StateMenu(5, "- &9Category Editor", player),
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