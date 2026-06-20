package me.danny.shop.inv.editor.items.properties

import me.danny.shop.DannyShop
import me.danny.shop.input.askInput
import me.danny.shop.input.collapse
import me.danny.shop.data.Key
import me.danny.shop.data.attachKey
import me.danny.shop.data.keyValue
import me.danny.shop.input.Input
import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.editor.items.ItemEditorView
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.model.Item
import me.danny.shop.pluginMsg
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.color
import me.danny.shop.utils.fill
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.math.absoluteValue

internal class SellLimitEditor(private val editor: ItemEditor) : MenuView {

    private var limit: UInt = 0u

    init {
        val item = DannyShop.SHOP.itemByIid(editor.item)!!
        when (item.sellLimit) {
            is Item.SellLimit.None -> limit = 0u

            //Add a shortcut for when the worth.yml has changed since import
            is Item.SellLimit.Amount -> limit = item.sellLimit.amount
        }
    }

    override fun onOpen(): ViewAction = ViewAction.Resize(3, "&7- &9Adjust Sell Limit")

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        val amounts = listOf(16, 32, 64, 128, 256, 512, 1024, 2048)
        placeButtons(amounts, inv)

        inv.setItem(
            inv.size - 2, ItemBuilder.makeItem(
                Material.ANVIL,
                "&5Confirm Sell Limit",
                "&9Sell limit: &7${
                    when (limit) {
                        0u -> "&cNo limit"
                        else -> "$limit"
                    }
                }",
                "&7Shift right click to &4remove sell limit&7.",
                "",
                "&e[Confirm: Click]",
                "&e[Unset: Shift right click]"
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
                val limit: Item.SellLimit = if (limit == 0u || (event.isShiftClick && event.isRightClick)) Item.SellLimit.None
                else Item.SellLimit.Amount(limit)

                DannyShop.SHOP.replaceItem(item.iid, item.copy(sellLimit = limit))
                return ViewAction.ChangeView(ItemEditorView(editor))
            }

            Material.EMERALD_BLOCK, Material.RED_TERRACOTTA -> {
                handleButton(event); return ViewAction.Refresh
            }

            Material.SPRUCE_SIGN -> {
                player.closeInventory()
                askInput("&eSet sell limit")
                    .getInput(player) { pl, input -> setLimit(pl, input, { limit = it }, event.inventory) }
            }

            else -> {}
        }

        return ViewAction.Pass
    }

    private fun handleButton(event: InventoryClickEvent) {
        var amount = event.currentItem!!.keyValue(AMOUNT_KEY)!!
        if (event.isShiftClick) amount *= 10
        else if (event.isRightClick) amount /= 2
        else if (event.click == ClickType.NUMBER_KEY) {
            amount *= (event.hotbarButton + 1)
        }

        if (amount < 0 && limit.toInt() - amount.absoluteValue <= 0) {
            limit = 0u
            event.whoClicked.pluginMsg("&7Sell limit &cremoved&7.".color())
        } else {
            limit = (limit + amount.toUInt()).coerceAtLeast(0u)
            val msg = if (amount < 0) "&7Sell limit &cdecreased by &e$amount"
            else "&7Sell price &aincreased by &e$amount"
            event.whoClicked.pluginMsg("$msg".color())
        }
    }

    private fun placeButtons(amounts: List<Int>, inv: Inventory) {
        fun buyPriceButton(amount: Int): ItemStack = makeButton(amount)

        amounts.map(::buyPriceButton).zip(0..8)
            .forEach { pair -> inv.setItem(pair.second, pair.first) }
        amounts.map { -it }
            .map(::buyPriceButton).zip(9..17)
            .forEach { pair -> inv.setItem(pair.second, pair.first) }
        inv.setItem(8, ItemBuilder.makeItem(Material.SPRUCE_SIGN, "&eSet custom Sell Limit"))
    }

    private fun makeButton(amount: Int): ItemStack {
        val type: Material
        val prefix: String
        if (amount < 0) {
            type = Material.RED_TERRACOTTA
            prefix = "&9Decrease by"
        } else {
            type = Material.EMERALD_BLOCK
            prefix = "&9Increase by"
        }

        return ItemBuilder.makeItem(
            type,
            "&eChange Sell Limit",
            "$prefix &7$amount".format(amount),
            "&8&m                        ",
            "&eShift click&7 for 10x",
            "&eRight click&7 for 0.5x",
            "&eNumber key&7 for multiplier"
        ).attachKey(AMOUNT_KEY, amount)
    }

    private fun setLimit(player: Player, input: Input, setter: (UInt) -> Unit, inv: Inventory) {
        val line = input.collapse()

        val limit = line.toIntOrNull()
        if (limit != null && limit > 0) {
            setter(limit.toUInt())
            player.pluginMsg("Sell limit set to $limit.")
        } else {
            setter(0u)
            player.pluginMsg("Sell limit removed.")
        }

        build(inv)
        player.openInventory(inv)
    }

    companion object {
        private val AMOUNT_KEY = Key("sell_limit", PersistentDataType.INTEGER)
    }
}