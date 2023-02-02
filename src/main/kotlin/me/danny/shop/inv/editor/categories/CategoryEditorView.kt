package me.danny.shop.inv.editor.categories

import me.danny.libinput.providers.*
import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.view.*
import me.danny.shop.model.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.persistence.*

class CategoryEditorView(val category: Category) : MenuView {

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
                "&7Click an item to change the icon"
            )
        )

        inv.setItem(
            11, ItemBuilder.makeItem(
                Material.WRITABLE_BOOK, "&eSet permission",
                "&7${category.permission ?: "&7No permission set"}"
            ).attachMarker(PERMISSION_BUTTON_KEY)
        )

        inv.setItem(15, ItemBuilder.makeItem(Material.NAME_TAG, "&eChange name").attachMarker(RENAME_BUTTON_KEY))

        inv.setItem(
            inv.size - 9, ItemBuilder.makeItem(
                Material.BARRIER, "&4Delete Category",
                "&7This is irreversible!!!"
            ).attachMarker(DELETE_BUTTON_KEY)
        )

        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(CategoryListingView())
        if (event.clickedInventory == inv) {
            val clicked = event.currentItem!!
            val player = event.whoClicked as Player
            if (clicked.hasMarker(DELETE_BUTTON_KEY)) {
                DannyShop.SHOP.deleteCategory(category.cid)
                event.whoClicked.sendMessage("&6[DannyShop] &7Category &e${category.name}&7 deleted.".color())
                return ViewAction.ChangeView(CategoryListingView())
            }

            if (clicked.hasMarker(RENAME_BUTTON_KEY)) {
                val provider = if (SignInput.isAvailable()) {
                    SignInput()
                        .withLines(arrayOf(category.name, "^^^^^", "DannyShop", "Rename category"))
                        .withMaterial(Material.BIRCH_WALL_SIGN)
                } else {
                    ChatInput()
                        .requestLines(1)
                        .withEscapeWords("cancel")
                        .withPrefix("&6[DannyShop]&7 ".color())
                        .withPrompt("&9Rename category:")
                }

                player.closeInventory()
                provider.getInput(player) { pl, input -> handleRename(pl, input, inv) }
            }

            if (clicked.hasMarker(PERMISSION_BUTTON_KEY)) {
                val provider = if (SignInput.isAvailable()) {
                    SignInput()
                        .withLines(arrayOf(category.permission ?: "", "^^^^^", "DannyShop", "Set permission"))
                        .withMaterial(Material.BIRCH_WALL_SIGN)
                } else {
                    ChatInput()
                        .requestLines(1)
                        .withEscapeWords("cancel")
                        .withPrefix("&6[DannyShop]&7 ".color())
                        .withPrompt("&9Set category permission:")
                }

                player.closeInventory()
                provider.getInput(player) { pl, input -> handlePermission(pl, input, inv) }
            }

            return ViewAction.Pass
        }

        category.changeDisplay(event.currentItem!!.type)
        build(inv)
        return ViewAction.Pass
    }

    private fun handleRename(player: Player, input: Input, inv: Inventory) {
        val newName = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }

        category.changeName(newName)
        player.sendMessage("&6[DannyShop] &7Category name changed to &e$newName&7.".color())
        build(inv)
        player.openInventory(inv)
    }

    private fun handlePermission(player: Player, input: Input, inv: Inventory) {
        val newName = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }.ifBlank { null }

        category.setPermission(newName)
        if (newName == null) {
            player.sendMessage("&6[DannyShop] &7Category permission cleared.".color())
        } else {
            player.sendMessage("&6[DannyShop] &7Category permission set to &e$newName&7.".color())
        }
        build(inv)
        player.openInventory(inv)
    }
}