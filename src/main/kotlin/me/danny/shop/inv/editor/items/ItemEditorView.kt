package me.danny.shop.inv.editor.items

import me.danny.shop.*
import me.danny.shop.inv.*
import me.danny.shop.inv.editor.items.properties.*
import me.danny.shop.inv.view.*
import me.danny.shop.me.inv.shop.*
import me.danny.shop.model.*
import org.bukkit.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*

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
        val cooldownButton =
            ItemBuilder.makeItem(Material.CLOCK, "&eCooldown", *cooldown(item), *footer)
        val nameButton = ItemBuilder.makeItem(Material.NAME_TAG, "&eName", *name(item), *footer)
        val categoryButton = ItemBuilder.makeItem(
            Material.CHEST,
            "&eCategory",
            "&7${item.category.name}",
            *footer
        )
        val quantitiesButton =
            ItemBuilder.makeItem(Material.WRITABLE_BOOK, "&eQuantities", *quantities(item), *footer)

        inv.setItem(11, costButton)
        inv.setItem(12, cooldownButton)
        inv.setItem(13, nameButton)
        inv.setItem(14, categoryButton)
        inv.setItem(15, quantitiesButton)
        inv.setItem(0, id)
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        if (event.slot == inv.size - 1) {
            ShopMenu(editor.viewer, editor.returnInfo)
            return ViewAction.Pass
        }

        val newView = when (event.currentItem!!.type) {
            Material.EMERALD -> CostPropEditor(editor)
            Material.WRITABLE_BOOK -> QuantitesPropEditor(editor)
            Material.CLOCK -> CooldownPropEditor(editor)
            Material.CHEST -> ItemCategoryEditor(editor)
            Material.NAME_TAG -> ItemNameEditor(editor)
            else -> null
        } ?: return ViewAction.Pass
        return ViewAction.ChangeView(newView)
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
                "&7&oPlayers cannot purchase this!",
                "&7&oSet a price to fix!"
            )

            is Item.Cost.Value -> arrayOf(
                "&9Each: &7\$%,.2f".format(item.cost.buy),
            )
        }
    }

    private fun cooldown(item: Item): Array<String> {
        return when (item.cooldown) {
            is Item.Cooldown.None -> arrayOf(
                "&2None",
                "",
                "&7&oPlayers may purchase this with no limit"
            )

            is Item.Cooldown.Infinite -> arrayOf(
                "&4Infinite",
                "",
                "&7&oPlayers may purchase this once"
            )

            is Item.Cooldown.Duration -> arrayOf(
                "&9Timed: &7${item.cooldown.time.display()}",
                "",
                "&7&oPlayers must wait before purchasing again"
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

    private fun quantities(item: Item): Array<String> {
        return arrayOf(
            "&9Predefined: &7${item.quantities.predefined}",
            "&9Mode: &7${item.quantities.allowed}",
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