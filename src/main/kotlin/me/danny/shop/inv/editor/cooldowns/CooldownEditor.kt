package me.danny.shop.inv.editor.cooldowns

import CooldownEditorMainView
import me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.inv.view.*
import org.bukkit.entity.*

class CooldownEditor(player: Player) : StateMenu(6, "- &5Cooldown Editor", player), RefreshPlease {
    init {
        build()
    }

    override fun refresh() {
        view.refresh(inv)
    }

    override fun loadView(): MenuView = CooldownEditorMainView()
}