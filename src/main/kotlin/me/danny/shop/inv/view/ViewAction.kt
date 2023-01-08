package me.danny.shop.inv.view

import me.danny.shop.me.danny.shop.inv.view.MenuView

sealed interface ViewAction {
    object Pass : ViewAction
    data class ChangeView(val newView: MenuView) : ViewAction
}