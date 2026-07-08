package me.danny.shop.inv.editor.items

import me.danny.shop.DannyShop
import me.danny.shop.input.Input
import me.danny.shop.input.askInput
import me.danny.shop.input.collapse
import me.danny.shop.inv.editor.items.properties.ItemCategoryEditor
import me.danny.shop.inv.editor.items.properties.ItemDynamicEditor
import me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.model.Item
import me.danny.shop.pluginMsg
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.fill
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

internal class ItemEditorView(private val editor: ItemEditor) : MenuView {

    override fun onOpen(): ViewAction = ViewAction.Resize(3)

    override fun build(inv: Inventory) {
        val item = DannyShop.SHOP.itemByIid(editor.item)!!

        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))

        val id = ItemBuilder.makeItem(
            Material.PAPER, "&e${item.iid.id}",
            "&9Type: &7${itemType(item)}",
            "&9Category:",
            "  &3Name: &7${item.category.name}",
            "  &3CID: &7${item.category.cid.id}",
        )

        val costButton = ItemBuilder.makeItem(Material.EMERALD, "&ePricing", *cost(item))
        val nameButton = ItemBuilder.makeItem(Material.NAME_TAG, "&eName", *name(item))
        val categoryButton = ItemBuilder.makeItem(
            Material.CHEST,
            "&eCategory",
            "&7${item.category.name}"
        )
        val sellLimitButton = ItemBuilder.makeItem(
            Material.IRON_DOOR,
            "&eSell Limit", *sellLimit(item)
        )

        val deleteButton = ItemBuilder.makeItem(
            Material.BARRIER,
            "&cDelete item",
            "&7&oRemove the item from the shop.",
            "&7&oThis &4&ocannot&7&o be undone!",
            "",
            "&c[Confirm: Shift right click]"
        )

        val dynamicButton = ItemBuilder.makeItem(
            Material.CHEST_MINECART,
            "&eDynamic Pricing", *dynamicPricing(item)
        )

        inv.setItem(11, costButton)
        inv.setItem(12, sellLimitButton)
        inv.setItem(13, dynamicButton)
        inv.setItem(14, nameButton)
        inv.setItem(15, categoryButton)
        inv.setItem(0, id)
        inv.setItem(inv.size - 9, deleteButton)
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        if (event.slot == inv.size - 1) {
            ShopMenu(editor.viewer, editor.returnInfo)
            return ViewAction.Pass
        }

        if (event.currentItem!!.type == Material.BARRIER && event.isShiftClick && event.isRightClick) {
            event.whoClicked.pluginMsg("&cItem deleted.")
            DannyShop.SHOP.deleteItem(editor.item)
            ShopMenu(editor.viewer, editor.returnInfo)
            return ViewAction.Pass
        }

        val player = event.whoClicked as Player
        val item = DannyShop.SHOP.itemByIid(editor.item) ?: return ViewAction.Pass
        val newView = when (event.currentItem!!.type) {
            Material.CHEST_MINECART -> ItemDynamicEditor(editor)
            Material.CHEST -> ItemCategoryEditor(editor)
            Material.EMERALD -> {
                fun updateCost(cost: Double?) {
                    val cost =
                        if (cost != null && cost > 0) Item.Cost.Value(cost)
                        else Item.Cost.NotSet
                    DannyShop.SHOP.replaceItem(item.iid, item.copy(cost = cost))
                    build(inv)
                }

                if (event.click.isRightClick) {
                    updateCost(null)
                    return ViewAction.Pass
                }

                player.closeInventory()
                askInput("&eSet sell price")
                    .getInput(player) { pl, input -> setPrice(pl, input, ::updateCost, event.inventory) }
                return ViewAction.Pass
            }

            Material.NAME_TAG -> {
                fun updateName(name: String?) {
                    DannyShop.SHOP.replaceItem(item.iid, item.copy(name = name))
                    build(inv)
                }

                if (event.click.isRightClick) {
                    updateName(null)
                    return ViewAction.Pass
                }

                player.closeInventory()
                askInput("&9New name")
                    .getInput(player) { pl, input ->
                        updateName(input.collapse())
                        pl.openInventory(event.inventory)
                    }
                return ViewAction.Pass
            }

            Material.IRON_DOOR -> {
                fun updateLimit(limit: ULong) {
                    val limit: Item.SellLimit = if (limit == 0UL) Item.SellLimit.None
                    else Item.SellLimit.Amount(limit)

                    DannyShop.SHOP.replaceItem(item.iid, item.copy(sellLimit = limit))
                    build(inv)
                }

                if (event.click.isRightClick) {
                    updateLimit(0UL)
                    return ViewAction.Pass
                }

                player.closeInventory()
                askInput("&eSet sell limit")
                    .getInput(player) { pl, input ->
                        setLimit(pl, input, ::updateLimit, event.inventory)
                    }
                return ViewAction.Pass
            }

            else -> null
        } ?: return ViewAction.Pass
        return ViewAction.ChangeView(newView)
    }

    private fun itemType(item: Item): String {
        return when (item.item) {
            is Item.ItemType.Mat -> "Material"
            is Item.ItemType.Item -> "ItemStack"
        }
    }

    private fun cost(item: Item): Array<String> {
        return when (item.cost) {
            is Item.Cost.NotSet -> arrayOf(
                "&cNot set!",
                "",
                "&c&oPlayers cannot sell this!",
                "&7&oSet a price to fix!",
                "",
                "&e[Edit: Click]"
            )

            is Item.Cost.Value -> arrayOf(
                "&9Each: &7$%,.2f".format(item.cost.buy),
                "",
                "&e[Remove: Right click]",
                "&e[Edit: Click]"
            )
        }
    }

    private fun name(item: Item): Array<String> {
        return when (item.name) {
            null -> arrayOf(
                "&cNo name set",
                "",
                "&7&oThis item cannot be searched for by name.",
                "",
                "&e[Edit: Click]"
            )

            else -> arrayOf(
                "&7${item.name}",
                "",
                "&7&oPlayers can search for this item by that name.",
                "",
                "&e[Remove: Right click]",
                "&e[Edit: Click]"
            )
        }
    }

    private fun sellLimit(item: Item): Array<String> {
        return when (item.sellLimit) {
            is Item.SellLimit.None -> arrayOf(
                "&cNo sell limit set.",
                "",
                "&7&oPlayers can sell an unlimited amount of this item.",
                "",
                "&e[Edit: Click]"
            )

            is Item.SellLimit.Amount -> arrayOf(
                "&9Sell limit: &7${item.sellLimit.amount}",
                "",
                "&7&oPlayers are limited to selling",
                "&7&othis many units in a refresh window.",
                "",
                "&e[Remove: Right click]",
                "&e[Edit: Click]"
            )
        }
    }

    private fun dynamicPricing(item: Item): Array<String> {
        return if (item.dynamic != null) {
            arrayOf(
                "&9Dynamic pricing active: &7${item.usesDynamicPricing}",
                "",
                "&9Server demand: &7${item.dynamic.serverDemand}",
                "&9Replenish interval ticks: &7${item.dynamic.replenishIntervalTicks}",
                "&9Replenish volume: &7${item.dynamic.replenishVolume}",
                "&9Minimum price: &7$%,.2f".format(item.dynamic.minimumPrice),
                "&9Player immunity volume: &7${item.dynamic.playerImmunityVolume}",
                "",
                "&e[Remove: Right click]",
                "&e[Edit: Click]"
            )
        } else {
            arrayOf(
                "&7Item has no dynamic pricing information set up.",
                "",
                "&7Item price will remain at a fixed price regardless",
                "&7of volume.",
                "",
                "&e[Edit: Click]"
            )
        }
    }

    private fun setLimit(player: Player, input: Input, setter: (ULong) -> Unit, inv: Inventory) {
        val line = input.collapse()

        val limit = line.toIntOrNull()
        if (limit != null && limit > 0) {
            setter(limit.toULong())
            player.pluginMsg("Sell limit set to $limit.")
        } else {
            setter(0u)
            player.pluginMsg("Sell limit removed.")
        }

        build(inv)
        player.openInventory(inv)
    }

    private fun setPrice(player: Player, input: Input, setter: (Double) -> Unit, inv: Inventory) {
        val line = input.collapse()

        val price = line.toDoubleOrNull()
        if (price != null && price.isFinite() && price >= 0.0) {
            setter(price)
            player.pluginMsg("Price set to &2$%,.2f&7.".format(price))
        } else {
            player.pluginMsg("&cUnable to read price from \"&d$line&c\"...")
        }

        build(inv)
        player.openInventory(inv)
    }
}