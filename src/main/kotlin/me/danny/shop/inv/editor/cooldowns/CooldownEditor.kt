package me.danny.shop.inv.editor.cooldowns

import me.danny.shop.inv.RefreshPlease
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.StateMenu
import org.bukkit.entity.Player

internal class CooldownEditor(player: Player) : StateMenu(6, "- &5Cooldown Editor", player), RefreshPlease {
    init {
        build()
    }

    override fun refresh() {
        view.refresh(inv)
    }

    override fun loadView(): MenuView = CooldownEditorMainView()
}