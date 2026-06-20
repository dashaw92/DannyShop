package me.danny.shop.inv.editor.categories

import me.danny.shop.DannyShop
import me.danny.shop.input.askInput
import me.danny.shop.input.collapse
import me.danny.shop.data.Key
import me.danny.shop.data.attachMarker
import me.danny.shop.data.hasMarker
import me.danny.shop.input.ChatInput
import me.danny.shop.input.Input
import me.danny.shop.input.MultipleLines
import me.danny.shop.input.SingleLine
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.model.Category
import me.danny.shop.pluginMsg
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.color
import me.danny.shop.utils.fill
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.persistence.PersistentDataType

internal class CategoryEditorView(val category: Category, private val returnToViewSupplier: () -> MenuView) : MenuView {

    companion object {
        private val DELETE_BUTTON_KEY = Key("delete_category", PersistentDataType.BYTE)
        private val RENAME_BUTTON_KEY = Key("rename_category", PersistentDataType.BYTE)
        private val PERMISSION_BUTTON_KEY = Key("set_permission", PersistentDataType.BYTE)
    }

    override fun onOpen(): ViewAction = ViewAction.Resize(3)

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        inv.setItem(
            13, ItemBuilder.makeItem(
                category.display, "&e${category.name}",
                "&6Click &7any item in &6your inventory",
                "&7to change the icon of this category"
            ).let { ItemBuilder.addAttribute(it, *ItemFlag.entries.toTypedArray()) }
        )

        inv.setItem(
            11, ItemBuilder.makeItem(
                Material.WRITABLE_BOOK, "&eSet permission",
                *if (category.permission != null) {
                    listOf(
                        "&9Permission: &7&o${category.permission}",
                        "",
                        "&e[Set: Click]",
                        "&c[Remove: Right click]"
                    )
                } else {
                    listOf(
                        "&9No permission set",
                        "",
                        "&e[Set: Click]",
                    )
                }.toTypedArray(),
            ).attachMarker(PERMISSION_BUTTON_KEY)
        )

        inv.setItem(
            15,
            ItemBuilder.makeItem(
                Material.NAME_TAG, "&eChange name",
                "&9Name: &7${category.name}",
                "",
                "&e[Set: Click]"
            )
                .attachMarker(RENAME_BUTTON_KEY)
        )

        inv.setItem(
            inv.size - 9, ItemBuilder.makeItem(
                Material.BARRIER,
                "&cDelete Category",
                "&7&oRemove the category from the shop.",
                "&7&oThis &4&ocannot&7&o be undone!",
                "&4&oWarning: &7&oAll items in this category",
                "&7&owill &c&oalso be deleted!",
                "",
                "&c[Confirm: Shift right click]"
            ).attachMarker(DELETE_BUTTON_KEY)
        )

        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(returnToViewSupplier())
        if (event.clickedInventory == inv) {
            val clicked = event.currentItem!!
            val player = event.whoClicked as Player
            if (event.isRightClick && event.isShiftClick && clicked.hasMarker(DELETE_BUTTON_KEY)) {
                DannyShop.SHOP.deleteCategory(category.cid)
                event.whoClicked.pluginMsg("Category &e${category.name}&7 deleted.".color())
                return ViewAction.ChangeView(returnToViewSupplier())
            }

            if (clicked.hasMarker(RENAME_BUTTON_KEY)) {
                val provider = ChatInput()
                    .requestLines(1)
                    .withEscapeWords("cancel")
                    .withPrefix("&6[DannyShop]&7".color())
                    .withPrompt("&9Rename category:")

                player.closeInventory()
                provider.getInput(player) { pl, input -> handleRename(pl, input, inv) }
            }

            if (clicked.hasMarker(PERMISSION_BUTTON_KEY)) {
                if (event.isRightClick) {
                    category.setPermission(null)
                    player.pluginMsg("&eCategory permission removed.")
                    build(inv)
                    player.openInventory(inv)
                    return ViewAction.Pass
                }

                player.closeInventory()
                askInput("&9Set permission (${category.permission})")
                    .getInput(player) { pl, input -> handlePermission(pl, input, inv) }
            }

            return ViewAction.Pass
        }

        category.changeDisplay(event.currentItem!!.type)
        build(inv)
        return ViewAction.Pass
    }

    private fun handleRename(player: Player, input: Input, inv: Inventory) {
        val newName = input.collapse()

        category.changeName(newName)
        player.pluginMsg("&eCategory name changed to &7$newName&e.")
        build(inv)
        player.openInventory(inv)
    }

    private fun handlePermission(player: Player, input: Input, inv: Inventory) {
        val newName = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }.ifBlank { null }

        category.setPermission(newName)
        player.pluginMsg("&eCategory permission set to &7&o$newName&e.")
        build(inv)
        player.openInventory(inv)
    }
}