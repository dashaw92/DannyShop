package me.danny.shop.inv.shop.purchasing

import me.danny.shop.DannyShop
import me.danny.shop.data.Item
import me.danny.shop.economy.Economy
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Menu
import me.danny.shop.me.danny.shop.data.Key
import me.danny.shop.me.danny.shop.data.hasKey
import me.danny.shop.me.danny.shop.data.keyValue
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

class PurchaseMenu(viewer: Player, item: Item.IID, private val returnInfo: ShopMenu.ShopReturnInfo) :
    Menu(5, "- &2Purchase", viewer) {

    companion object {
        internal val PRICE_KEY = Key("qty_price_buy", PersistentDataType.DOUBLE)
    }

    private val item = DannyShop.SHOP.itemByIid(item)!!
    private val page = QuantityDisplay(this.item)

    init {
        build()
    }

    override fun build() {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        page.render(inv)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(event: InventoryClickEvent) {
        val clicked = event.currentItem!!
        if (event.slot == inv.size - 1) {
            ShopMenu(viewer, returnInfo)
            return
        }

        if (clicked.hasKey(PRICE_KEY)) {
            val price = clicked.keyValue(PRICE_KEY)!!
            Economy.purchase(viewer, item.iid, price, clicked.amount)
            return
        }

        page.onClick(event, ::build)
    }
}