package me.danny.shop.inv.editor.categories

import me.danny.libinput.providers.*
import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.view.*
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.inv.view.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.persistence.*

class CategoryEditorView(val category: Category) : MenuView {

    companion object {
        private val DELETE_BUTTON_KEY = Key("delete_category", PersistentDataType.BYTE)
        private val RENAME_BUTTON_KEY = Key("rename_category", PersistentDataType.BYTE)
    }

    override fun onOpen(): ViewAction = ViewAction.Resize(3)

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        inv.setItem(
            12, ItemBuilder.makeItem(
                category.display, "&9${category.name}",
                "&eClick an item to change the icon"
            )
        )

        inv.setItem(14, ItemBuilder.makeItem(Material.NAME_TAG, "&6Change name").attachMarker(RENAME_BUTTON_KEY))

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
            if (clicked.hasMarker(DELETE_BUTTON_KEY)) {
                DannyShop.SHOP.deleteCategory(category.cid)
                event.whoClicked.sendMessage("&6[DannyShop] &eCategory &6${category.name}&e deleted.".color())
                return ViewAction.ChangeView(CategoryListingView())
            }

            if (clicked.hasMarker(RENAME_BUTTON_KEY)) {
                val player = event.whoClicked as Player
                val provider = if (SignInput.isAvailable()) {
                    SignInput()
                        .withLines(arrayOf(category.name, "^^^^^", "DannyShop", "Rename category"))
                        .withMaterial(Material.BIRCH_WALL_SIGN)
                } else {
                    ChatInput()
                        .requestLines(1)
                        .withEscapeWords("cancel")
                        .withPrefix("&6[DannyShop] &e".color())
                        .withPrompt("Rename category:")
                }

                player.closeInventory()
                provider.getInput(player) { pl, input -> handleInput(pl, input, inv) }
            }

            return ViewAction.Pass
        }

        category.changeDisplay(event.currentItem!!.type)
        build(inv)
        return ViewAction.Pass
    }

    private fun handleInput(player: Player, input: Input, inv: Inventory) {
        val newName = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }

        category.changeName(newName)
        player.sendMessage("&6[DannyShop] &7Category name changed to &e$newName".color())
        build(inv)
        player.openInventory(inv)
    }
}