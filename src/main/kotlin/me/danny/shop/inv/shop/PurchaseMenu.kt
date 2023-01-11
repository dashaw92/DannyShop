package me.danny.shop.me.danny.shop.inv.shop

import me.danny.shop.DannyShop
import me.danny.shop.data.Item
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Menu
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class PurchaseMenu(viewer: Player, val item: Item, private val returnInfo: ShopMenu.ShopReturnInfo) :
    Menu(3, "- ${ChatColor.DARK_GREEN}Purchase", viewer) {

    override fun build() {
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "${ChatColor.BLUE}Back"))
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.slot == inv.size - 1) {
            ShopMenu(DannyShop.SHOP, viewer, returnInfo)
            return
        }
    }

}