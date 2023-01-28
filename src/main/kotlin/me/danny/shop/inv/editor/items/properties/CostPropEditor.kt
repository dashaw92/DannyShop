package me.danny.shop.me.danny.shop.inv.editor.items.properties

import me.danny.libinput.providers.*
import me.danny.shop.*
import me.danny.shop.data.Item
import me.danny.shop.data.Item.ItemType
import me.danny.shop.economy.*
import me.danny.shop.inv.*
import me.danny.shop.inv.editor.items.*
import me.danny.shop.inv.view.*
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.inv.view.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.persistence.*

class CostPropEditor(private val editor: ItemEditor) : MenuView {

    private var buy: Double = 0.0
    private var sell: Double = 0.0

    init {
        val item = DannyShop.SHOP.itemByIid(editor.item)!!
        when (item.cost) {
            is Item.Cost.Value -> {
                buy = item.cost.buy
                sell = item.cost.sell
            }

            //Add a shortcut for when the worth.yml has changed since import
            is Item.Cost.NotSet -> {
                if (item.item is ItemType.Mat) {
                    val essCost = Economy.getWorth(ItemStack(item.item.material))
                    if (essCost is Item.Cost.Value) {
                        buy = essCost.buy
                        sell = essCost.sell
                    }
                }
            }
        }
    }

    override fun onOpen(): ViewAction = ViewAction.Resize(5, "&7- &9Adjust Pricing")

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        val amounts = listOf(1.0, 10.0, 100.0, 250.0, 1000.0, 10000.0, 100_000.0, 1_000_000.0)
        placeButtons(amounts, inv)

        inv.setItem(
            inv.size - 9, ItemBuilder.makeItem(
                Material.ANVIL,
                "&5Confirm Price",
                "&eBuy: &7\$%,.2f".format(buy),
                "&eSell: &7\$%,.2f".format(sell),
                "",
                "&cShift right click to &4unset price",
                "&c&oUnsetting the price will make this",
                "&c&oitem unavailable for purchasing!"
            )
        )
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        val item = DannyShop.SHOP.itemByIid(editor.item)!!
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(ItemEditorView(editor))

        val player = event.whoClicked as Player

        when (event.currentItem!!.type) {
            Material.ANVIL -> {
                val cost: Item.Cost = if (event.isShiftClick && event.isRightClick) Item.Cost.NotSet
                else Item.Cost.Value(buy, sell)

                DannyShop.SHOP.replaceItem(item.iid, item.copy(cost = cost))
                return ViewAction.ChangeView(ItemEditorView(editor))
            }

            Material.EMERALD_BLOCK, Material.RED_TERRACOTTA -> {
                handleButton(event); return ViewAction.Refresh
            }

            Material.SPRUCE_SIGN -> {
                val provider = if (SignInput.isAvailable()) {
                    SignInput()
                        .withLines(arrayOf("", "^^^^^", "DannyShop", "Set buy price"))
                        .withMaterial(Material.SPRUCE_WALL_SIGN)

                } else {
                    ChatInput()
                        .requestLines(1)
                        .withEscapeWords("cancel")
                        .withPrefix("&6[DannyShop] ".color())
                        .withPrompt("&eSet buy price".color())
                }

                provider.getInput(player) { pl, input -> setPrice(pl, input, { buy = it }, event.inventory) }
            }

            Material.BIRCH_SIGN -> {
                val provider = if (SignInput.isAvailable()) {
                    SignInput()
                        .withLines(arrayOf("", "^^^^^", "DannyShop", "Set sell price"))
                        .withMaterial(Material.BIRCH_WALL_SIGN)

                } else {
                    ChatInput()
                        .requestLines(1)
                        .withEscapeWords("cancel")
                        .withPrefix("&6[DannyShop] ".color())
                        .withPrompt("&eSet sell price".color())
                }

                provider.getInput(player) { pl, input -> setPrice(pl, input, { sell = it }, event.inventory) }
            }

            else -> {}
        }

        return ViewAction.Pass
    }

    private fun handleButton(event: InventoryClickEvent) {
        var amount = event.currentItem!!.keyValue(AMOUNT_KEY)!!
        if (event.isShiftClick) amount *= 10.0
        else if (event.isRightClick) amount /= 2.0
        else if (event.click == ClickType.NUMBER_KEY) {
            amount *= (event.hotbarButton + 1)
        }

        val msg: String
        if (event.currentItem!!.keyValue(TARGET_KEY)!! == "buy") {
            buy = (buy + amount).coerceAtLeast(0.0)
            msg = if (amount < 0) "&eBuy price &cdecreased by &7$%,.2f".format(amount)
            else "&eBuy price &aincreased by &7$%,.2f".format(amount)
        } else {
            sell = (sell + amount).coerceAtLeast(0.0)
            msg = if (amount < 0) "&eSell price &cdecreased by &7$%,.2f".format(amount)
            else "&eSell price &aincreased by &7$%,.2f".format(amount)
        }

        event.whoClicked.sendMessage("&6[DannyShop] $msg".color())
    }

    private fun sellPriceButton(amount: Double): ItemStack = makeButton("Sell", amount)
    private fun buyPriceButton(amount: Double): ItemStack = makeButton("Buy", amount)
    private fun placeButtons(amounts: List<Double>, inv: Inventory) {
        amounts.map(::buyPriceButton).zip(0..8)
            .forEach { pair -> inv.setItem(pair.second, pair.first) }
        amounts.map { -it }
            .map(::buyPriceButton).zip(9..17)
            .forEach { pair -> inv.setItem(pair.second, pair.first) }


        amounts.map(::sellPriceButton).zip(18..26)
            .forEach { pair -> inv.setItem(pair.second, pair.first) }
        amounts.map { -it }
            .map(::sellPriceButton).zip(27..35)
            .forEach { pair -> inv.setItem(pair.second, pair.first) }

        inv.setItem(8, ItemBuilder.makeItem(Material.SPRUCE_SIGN, "&6Set custom buy price"))
        inv.setItem(26, ItemBuilder.makeItem(Material.BIRCH_SIGN, "&6Set custom sell price"))
    }

    private fun makeButton(target: String, amount: Double): ItemStack {
        val type: Material
        val prefix: String
        if (amount < 0.0) {
            type = Material.RED_TERRACOTTA
            prefix = "&cDecrease by"
        } else {
            type = Material.EMERALD_BLOCK
            prefix = "&aIncrease by"
        }

        return ItemBuilder.makeItem(
            type,
            "&2$target Price",
            "$prefix \$%,.2f".format(amount),
            "&8&m                        ",
            "&6Shift click&e for 10x",
            "&6Right click&e for 0.5x",
            "&6Number key&e for multiplier"
        ).attachKey(AMOUNT_KEY, amount).attachKey(TARGET_KEY, target.lowercase())
    }

    private fun setPrice(player: Player, input: Input, setter: (Double) -> Unit, inv: Inventory) {
        val line = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }

        val price = line.toDoubleOrNull()
        if (price != null && price.isFinite() && price >= 0.0) {
            setter(price)
            player.sendMessage("&6[DannyShop] &7Price set to &2$%,.2f".format(price).color())
        } else {
            player.sendMessage("&6[DannyShop] &cUnable to read price from \"&d$line&c\"...".color())
        }

        build(inv)
        player.openInventory(inv)
    }

    companion object {
        private val AMOUNT_KEY = Key("costprop_amount", PersistentDataType.DOUBLE)
        private val TARGET_KEY = Key("costprop_target", PersistentDataType.STRING)
    }
}