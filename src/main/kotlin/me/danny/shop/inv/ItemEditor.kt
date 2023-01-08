package me.danny.shop.inv

import me.danny.shop.DannyShop
import me.danny.shop.data.Item
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ItemEditor(viewer: Player, val item: Item, val returnInfo: ShopMenu.ShopReturnInfo) :
    Menu(4, "- ${ChatColor.BLUE}Item Editor", viewer) {

    init {
        build()
    }

    override fun build() {
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "${ChatColor.BLUE}Back"))
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.slot == inv.size - 1) {
            ShopMenu(DannyShop.SHOP, viewer, returnInfo.itemPage, returnInfo.categoryPage)
            return
        }
    }
}