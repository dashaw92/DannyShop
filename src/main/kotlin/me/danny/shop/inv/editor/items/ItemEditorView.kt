package me.danny.shop.inv.editor.items

import me.danny.shop.DannyShop
import me.danny.shop.data.Item
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.editor.items.properties.QuantitesPropEditor
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.me.danny.shop.inv.editor.items.properties.CostPropEditor
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.me.danny.shop.inv.view.MenuView
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class ItemEditorView(private val editor: ItemEditor) : MenuView {

    override fun onOpen(): ViewAction = ViewAction.Resize(3)

    override fun build(inv: Inventory) {
        val item = DannyShop.SHOP.itemByIid(editor.item)!!

        val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
        inv.fill(filler)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))

        val id = ItemBuilder.makeItem(
            Material.PAPER, "&aIID: &7${item.iid.id}",
            "&3Type: &7${itemType(item)}",
        )

        val footer = arrayOf("", "&e[Edit: Click]")

        val costButton = ItemBuilder.makeItem(Material.EMERALD, "&aPrices", *cost(item), *footer)
        val cooldownButton =
            ItemBuilder.makeItem(Material.CLOCK, "&aCooldown", *cooldown(item), *footer)
        val categoryButton = ItemBuilder.makeItem(
            Material.CHEST,
            "&aCategory",
            "&7${item.category.name}",
            *footer
        )
        val quantitiesButton =
            ItemBuilder.makeItem(Material.WRITABLE_BOOK, "&aQuantities", *quantities(item), *footer)

        inv.setItem(11, costButton)
        inv.setItem(12, cooldownButton)
        inv.setItem(14, categoryButton)
        inv.setItem(15, quantitiesButton)
        inv.setItem(0, id)
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        if (event.slot == inv.size - 1) {
            ShopMenu(DannyShop.SHOP, editor.viewer, editor.returnInfo)
            return ViewAction.Pass
        }

        return when (event.currentItem!!.type) {
            Material.EMERALD -> ViewAction.ChangeView(CostPropEditor(editor))
            Material.WRITABLE_BOOK -> ViewAction.ChangeView(QuantitesPropEditor(editor))
            else -> ViewAction.Pass
        }
    }

    private fun itemType(item: Item): String {
        return when (item.item) {
            is Item.ItemType.Mat -> "Material"
            is Item.ItemType.Item -> "ItemStack"
            is Item.ItemType.Exp -> "Experience"
            is Item.ItemType.Command -> "Command"
        }
    }

    private fun cost(item: Item): Array<String> {
        return when (item.cost) {
            is Item.Cost.NotSet -> arrayOf(
                "&cNot set!",
                "",
                "&c&oPlayers cannot purchase this!",
                "&c&oSet a price to fix!"
            )

            is Item.Cost.Value -> arrayOf(
                "&eBuy: &7\$%,.2f".format(item.cost.buy),
                "&eSell: &7\$%,.2f".format(item.cost.sell),
            )
        }
    }

    private fun cooldown(item: Item): Array<String> {
        return when (item.cooldown) {
            is Item.Cooldown.None -> arrayOf(
                "&7None",
                "",
                "&7&oPlayers may purchase this with no limit"
            )

            is Item.Cooldown.Infinite -> arrayOf(
                "&7Infinite",
                "",
                "&7&oPlayers may purchase this once"
            )

            is Item.Cooldown.Duration -> arrayOf(
                "&eTimed: &7${item.cooldown.time.display()}",
                "",
                "&7&oPlayers must wait before purchasing again"
            )
        }
    }

    private fun quantities(item: Item): Array<String> {
        return arrayOf(
            "&ePredefined: &7${item.quantities.predefined}",
            "&eMode: &7${item.quantities.allowed}",
            "",
            "&7&o%s".format(
                when (item.quantities.allowed) {
                    Item.Quantities.Allowed.Any -> "Players can buy any amount of this at once"
                    Item.Quantities.Allowed.Predefined -> "Players may only buy a predefined amount"
                }
            )
        )
    }

}