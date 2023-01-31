package me.danny.shop.inv.view

/**
 * Used to determine what a base menu should do on click
 */
sealed interface ViewAction {
    /**
     * Do nothing, this was a normal button click with no meta-action
     */
    object Pass : ViewAction

    /**
     * Request the parent re-build the inventory
     */
    object Refresh : ViewAction

    /**
     * Request the base menu change to the provided view
     */
    data class ChangeView(val newView: MenuView) : ViewAction

    /**
     * Request an inventory resize
     */
    data class Resize(val rows: Int, val title: String? = null) : ViewAction
}