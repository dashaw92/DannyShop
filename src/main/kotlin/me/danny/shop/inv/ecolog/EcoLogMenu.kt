package me.danny.shop.inv.ecolog

import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.StateMenu
import org.bukkit.entity.Player

internal class EcoLogMenu(player: Player) :
    StateMenu(6, "- &2Logs", player) {

    init {
        build()
    }

    override fun loadView(): MenuView = EcoLogDisplayView()

}