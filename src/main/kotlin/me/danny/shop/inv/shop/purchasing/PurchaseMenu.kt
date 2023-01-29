package me.danny.shop.inv.shop.purchasing

import me.danny.libinput.providers.*
import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.data.Item.Cost
import me.danny.shop.data.Item.Quantities.Allowed.Any
import me.danny.shop.economy.*
import me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.inv.shop.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.persistence.*

class PurchaseMenu(viewer: Player, item: ID, private val returnInfo: ShopMenu.ShopReturnInfo) :
    Menu(5, "- &2Purchase", viewer) {

    companion object {
        internal val PRICE_KEY = Key("qty_price_buy", PersistentDataType.BYTE)
    }

    private val item = DannyShop.SHOP.itemByIid(item)!!
    private val page = QuantityDisplay(this.item)
    private var customAmount = this.item.quantities.predefined.first()

    init {
        build()
    }

    override fun build() {
        val filler = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        page.render(inv)
        if (item.quantities.allowed == Any) {
            val customQuantity = ItemBuilder.makeItem(
                Material.SPRUCE_SIGN, "&6Buy custom amount",
                "&2x$customAmount &7($%,.2f)".format((item.cost as Cost.Value).buy * customAmount),
                "",
                "&e[Edit: Right click]"
            )
            inv.setItem(inv.size - 9, customQuantity)
        }
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(event: InventoryClickEvent) {
        val clicked = event.currentItem!!
        if (event.slot == inv.size - 1) {
            ShopMenu(viewer, returnInfo)
            return
        }

        if (clicked.hasKey(PRICE_KEY)) {
            Economy.purchase(viewer, item.iid, clicked.amount)
            return
        }

        if (clicked.type == Material.SPRUCE_SIGN) {
            if (event.click == ClickType.RIGHT) {
                val provider = if (SignInput.isAvailable()) {
                    SignInput()
                        .withMaterial(Material.SPRUCE_WALL_SIGN)
                        .withLines(arrayOf("", "^^^^^", "DannyShop", "Buy custom amount"))
                } else {
                    ChatInput()
                        .withEscapeWords("cancel")
                        .withPrefix("&6[DannyShop] &e".color())
                        .withPrompt("Buy custom amount:".color())
                        .requestLines(1)
                }
                viewer.closeInventory()
                provider.getInput(viewer, ::handleCustomQuantity)
            } else {
                Economy.purchase(viewer, item.iid, customAmount)
            }
        }
        page.onClick(event, ::build)
    }

    private fun handleCustomQuantity(player: Player, input: Input) {
        val quantityStr = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }

        val quantity = quantityStr.toIntOrNull()

        if (quantity != null && quantity > 0) {
            customAmount = quantity
            player.sendMessage("&6[DannyShop] &7Custom amount set to &e$customAmount".color())
        } else {
            player.sendMessage("&6[DannyShop] &cUnable to read quantity from \"&d$quantityStr&c\"...".color())
        }

        build()
        player.openInventory(inv)
    }
}