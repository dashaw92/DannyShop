package me.danny.shop.inv.shop.purchasing

import me.danny.shop.DannyShop
import me.danny.shop.input.askInput
import me.danny.shop.input.collapse
import me.danny.shop.data.Key
import me.danny.shop.data.attachMarker
import me.danny.shop.data.hasKey
import me.danny.shop.economy.Economy
import me.danny.shop.input.Input
import me.danny.shop.inv.Menu
import me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.model.ID
import me.danny.shop.model.Item.Cost
import me.danny.shop.pluginMsg
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.ItemBuilder.withAmount
import me.danny.shop.utils.fill
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

internal class SellMenu(viewer: Player, item: ID, private val returnInfo: ShopMenu.ShopReturnInfo) :
    Menu(2, "- &6Bulk Sell", viewer) {

    companion object {
        internal val PRICE_KEY = Key("qty_price_sell", PersistentDataType.BYTE)
    }

    private val item = DannyShop.SHOP.itemByIid(item)!!
    private var customAmount = 1

    init {
        build()
    }

    override fun build() {
        val filler = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        for (i in 0..15) {
            inv.setItem(i,
                ItemBuilder.makeItem(Material.OAK_BUTTON, "&6Sell ${(i + 1) * 4}").withAmount((i + 1) * 4)
                    .attachMarker(PRICE_KEY)
            )
        }

        val customQuantity = ItemBuilder.makeItem(
            Material.SPRUCE_SIGN, "&eSell custom amount",
            "&7x$customAmount &9($%,.2f)".format((item.cost as Cost.Value).buy * customAmount),
            "",
            "&e[Edit: Right click]"
        )
        inv.setItem(inv.size - 2, customQuantity)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(event: InventoryClickEvent) {
        val clicked = event.currentItem!!
        if (event.slot == inv.size - 1) {
            ShopMenu(viewer, returnInfo)
            return
        }

        if (clicked.hasKey(PRICE_KEY)) {
            Economy.sell(viewer, viewer.inventory, item.iid, clicked.amount)
            return
        }

        if (clicked.type == Material.SPRUCE_SIGN) {
            if (event.click == ClickType.RIGHT) {
                viewer.closeInventory()
                askInput("&9Sell custom amount")
                    .getInput(viewer, ::handleCustomQuantity)
            } else {
                Economy.sell(viewer, viewer.inventory, item.iid, customAmount)
            }
        }
    }

    private fun handleCustomQuantity(player: Player, input: Input) {
        val quantityStr = input.collapse()

        val quantity = quantityStr.toIntOrNull()

        if (quantity != null && quantity > 0) {
            customAmount = quantity
            player.pluginMsg("Custom amount set to &e$customAmount")
        } else {
            player.pluginMsg("&cUnable to read quantity from \"&d$quantityStr&c\"...")
        }

        build()
        player.openInventory(inv)
    }
}