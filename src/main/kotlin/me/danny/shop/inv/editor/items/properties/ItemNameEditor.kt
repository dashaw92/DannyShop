package me.danny.shop.inv.editor.items.properties

import me.danny.libinput.providers.*
import me.danny.shop.*
import me.danny.shop.inv.*
import me.danny.shop.inv.editor.items.*
import me.danny.shop.inv.view.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*

internal class ItemNameEditor(private val editor: ItemEditor) : MenuView {
    private val item = DannyShop.SHOP.itemByIid(editor.item)!!

    override fun onOpen(): ViewAction = ViewAction.Resize(3, "&7- &9Edit Name")

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        val removeNameButton = ItemBuilder.makeItem(
            Material.BARRIER, "&cRemove name",
            "&7Item names permit searching",
            "&7for this item by name.",
            "&7If you remove it, players will",
            "&7have a harder time finding it."
        )
        val changeNameButton = ItemBuilder.makeItem(Material.SPRUCE_SIGN, "&eSet name")

        inv.setItem(12, removeNameButton)
        inv.setItem(14, changeNameButton)

        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        val clicked = event.currentItem!!
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(ItemEditorView(editor))

        when (clicked.type) {
            Material.BARRIER -> {
                val nameRemoved = item.copy(name = null)
                DannyShop.SHOP.replaceItem(item.iid, nameRemoved)
                return ViewAction.ChangeView(ItemEditorView(editor))
            }

            Material.SPRUCE_SIGN -> {
                val player = event.whoClicked as Player
                player.closeInventory()
                DannyShop.askInput("&9New name", Material.SPRUCE_WALL_SIGN, item.name)
                    .getInput(player, ::handleInput)
            }

            else -> {}
        }
        return ViewAction.Pass
    }

    private fun handleInput(player: Player, input: Input) {
        val newName = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }

        val renamed = if (newName.isBlank()) {
            player.pluginMsg("Removed item name.")
            item.copy(name = null)
        } else {
            player.pluginMsg("Renamed item to &e$newName&e!")
            item.copy(name = newName)
        }

        DannyShop.SHOP.replaceItem(item.iid, renamed)
        ItemEditor(player, renamed.iid, editor.returnInfo)
    }
}