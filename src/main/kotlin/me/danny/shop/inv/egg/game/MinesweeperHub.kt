package me.danny.shop.inv.egg.game

import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Menu
import me.danny.shop.inv.egg.MinesweeperGUI
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class MinesweeperHub(player: Player) : Menu(3, "- &dDannySweeper", player) {

    init {
        build()
    }

    override fun build() {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)
        inv.setItem(13, ItemBuilder.makeItem(Material.TNT_MINECART, "&6Play DannySweeper"))
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.slot == inv.size - 1) {
            ShopMenu(viewer)
            return
        }

        if (event.currentItem!!.type == Material.TNT_MINECART) {
            MinesweeperGUI(viewer)
            return
        }
    }

}