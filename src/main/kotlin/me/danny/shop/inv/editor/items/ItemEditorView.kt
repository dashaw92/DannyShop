package me.danny.shop.inv.editor.items

import me.danny.shop.DannyShop
import me.danny.shop.inv.editor.items.properties.*
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.model.Item
import me.danny.shop.pluginMsg
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.fill
import org.bukkit.Material
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

        val footer = arrayOf("", "&e[Edit: Click]")

        val costButton = ItemBuilder.makeItem(Material.EMERALD, "&ePricing", *cost(item), *footer)
        val nameButton = ItemBuilder.makeItem(Material.NAME_TAG, "&eName", *name(item), *footer)
        val categoryButton = ItemBuilder.makeItem(
            Material.CHEST,
            "&eCategory",
            "&7${item.category.name}",
            *footer
        )
        val sellLimitButton = ItemBuilder.makeItem(
            Material.IRON_DOOR,
            "&eSell Limit", *sellLimit(item), *footer
        )

        val deleteButton = ItemBuilder.makeItem(
            Material.BARRIER,
            "&cDelete item",
            "&7&oRemove the item from the shop.",
            "&7&oThis &4&ocannot&7&o be undone!",
            "",
            "&c[Confirm: Shift right click]"
        )

        inv.setItem(11, costButton)
        inv.setItem(12, sellLimitButton)
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

        val newView = when (event.currentItem!!.type) {
            Material.EMERALD -> CostPropEditor(editor)
            Material.CHEST -> ItemCategoryEditor(editor)
            Material.NAME_TAG -> ItemNameEditor(editor)
            Material.IRON_DOOR -> SellLimitEditor(editor)
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
                "&7&oSet a price to fix!"
            )

            is Item.Cost.Value -> arrayOf(
                "&9Each: &7\$%,.2f".format(item.cost.buy),
            )
        }
    }

    private fun name(item: Item): Array<String> {
        return when (item.name) {
            null -> arrayOf(
                "&cNo name set",
                "",
                "&7&oThis item cannot be searched for by name."
            )

            else -> arrayOf(
                "&7${item.name}",
                "",
                "&7&oPlayers can search for this item by that name."
            )
        }
    }

    private fun sellLimit(item: Item): Array<String> {
        return when (item.sellLimit) {
            is Item.SellLimit.None -> arrayOf(
                "&cNo sell limit set.",
                "",
                "&7&oPlayers can sell an unlimited amount of this item."
            )

            is Item.SellLimit.Amount -> arrayOf(
                "&9Sell limit: &7${item.sellLimit.amount}",
                "",
                "&7&oPlayers are limited to selling",
                "&7&othis many units in a refresh window."
            )
        }
    }
}