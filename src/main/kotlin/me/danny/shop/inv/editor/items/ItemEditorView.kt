package me.danny.shop.inv.editor.items

import me.danny.shop.DannyShop
import me.danny.shop.data.Item
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.MenuView
import me.danny.shop.inv.MenuView.ViewAction
import me.danny.shop.inv.fill
import me.danny.shop.inv.root.ShopMenu
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class ItemEditorView(private val editor: ItemEditor) : MenuView {

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
        inv.fill(filler)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "${ChatColor.BLUE}Back"))

        val id = ItemBuilder.makeItem(
            Material.PAPER, "${ChatColor.GREEN}IID: ${ChatColor.GRAY}${editor.item.iid.id}",
            "${ChatColor.DARK_AQUA}Type: ${ChatColor.GRAY}${itemType()}",
        )

        val footer = arrayOf("", "${ChatColor.YELLOW}[Edit: Click]")

        val costButton = ItemBuilder.makeItem(Material.EMERALD, "${ChatColor.GREEN}Prices", *cost(), *footer)
        val cooldownButton = ItemBuilder.makeItem(Material.CLOCK, "${ChatColor.GREEN}Cooldown", *cooldown(), *footer)
        val categoryButton = ItemBuilder.makeItem(
            Material.CHEST,
            "${ChatColor.GREEN}Category",
            "${ChatColor.GRAY}${editor.item.category.name}",
            *footer
        )
        val quantitiesButton =
            ItemBuilder.makeItem(Material.WRITABLE_BOOK, "${ChatColor.GREEN}Quantities", *quantities(), *footer)

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

        return ViewAction.Pass
    }

    private fun itemType(): String {
        return when (editor.item.item) {
            is Item.ItemType.Mat -> "Material"
            is Item.ItemType.Item -> "ItemStack"
            is Item.ItemType.Exp -> "Experience"
            is Item.ItemType.Command -> "Command"
        }
    }

    private fun cost(): Array<String> {
        return when (editor.item.cost) {
            is Item.Cost.NotSet -> arrayOf(
                "${ChatColor.RED}Not set!",
                "",
                "${ChatColor.RED}${ChatColor.ITALIC}Players cannot purchase this!",
                "${ChatColor.RED}${ChatColor.ITALIC}Set a price to fix!"
            )

            is Item.Cost.Value -> arrayOf(
                "${ChatColor.YELLOW}Buy: ${ChatColor.GRAY}\$%.2f".format(editor.item.cost.buy),
                "${ChatColor.YELLOW}Sell: ${ChatColor.GRAY}\$%.2f".format(editor.item.cost.sell),
            )
        }
    }

    private fun cooldown(): Array<String> {
        return when (editor.item.cooldown) {
            is Item.Cooldown.None -> arrayOf(
                "${ChatColor.GRAY}None",
                "",
                "${ChatColor.GRAY}${ChatColor.ITALIC}Players may purchase this with no limit"
            )

            is Item.Cooldown.Infinite -> arrayOf(
                "${ChatColor.GRAY}Infinite",
                "",
                "${ChatColor.GRAY}${ChatColor.ITALIC}Players may purchase this once"
            )

            is Item.Cooldown.Duration -> arrayOf(
                "${ChatColor.YELLOW}Timed: ${ChatColor.GRAY}${editor.item.cooldown.time.time}${editor.item.cooldown.time.suffix}",
                "",
                "${ChatColor.GRAY}${ChatColor.ITALIC}Players must wait before purchasing again"
            )
        }
    }

    private fun quantities(): Array<String> {
        return arrayOf(
            "${ChatColor.YELLOW}Predefined: ${ChatColor.GRAY}${editor.item.quantities.predefined}",
            "${ChatColor.YELLOW}Mode: ${ChatColor.GRAY}${editor.item.quantities.allowed}",
            "",
            "${ChatColor.GRAY}${ChatColor.ITALIC}%s".format(
                when (editor.item.quantities.allowed) {
                    Item.Quantities.Allowed.Any -> "Players can buy any amount of this at once"
                    Item.Quantities.Allowed.Predefined -> "Players may only buy a predefined amount"
                }
            )
        )
    }

}