package me.danny.shop.inv.editor.items.properties

import me.danny.shop.DannyShop
import me.danny.shop.data.Item
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Page
import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.editor.items.ItemEditorView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.me.danny.shop.data.Key
import me.danny.shop.me.danny.shop.data.attachKey
import me.danny.shop.me.danny.shop.data.hasKey
import me.danny.shop.me.danny.shop.data.keyValue
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.view.MenuView
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class QuantitesPropEditor(private val editor: ItemEditor) : MenuView {

    companion object {
        private val QUANTITY_KEY = Key("quantityprop_amount", PersistentDataType.INTEGER)
    }

    private val item = DannyShop.SHOP.itemByIid(editor.item)!!
    private var quantities = item.quantities.predefined.toMutableList()
    private var allowed = item.quantities.allowed

    private val page = QuantityPage(quantities)

    override fun onOpen(): ViewAction = ViewAction.Resize(4, "&7- &9Adjust Quantities")

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        val allowedButton = ItemBuilder.makeItem(
            Material.COMPARATOR, "&3Allowed mode", *when (allowed) {
                Item.Quantities.Allowed.Any -> arrayOf(
                    "&2• &nAny",
                    "&7• Predefined",
                    "",
                    "&ePlayers can enter a custom quantity",
                    "&ewhen purchasing this item.",
                )

                Item.Quantities.Allowed.Predefined -> arrayOf(
                    "&7• Any",
                    "&2• &nPredefined",
                    "",
                    "&ePlayers may only purchase this item",
                    "&ein predefined quantities."
                )
            }
        )

        inv.setItem(inv.size - 6, ItemBuilder.makeItem(Material.HOPPER, "&eSort"))
        inv.setItem(inv.size - 7, ItemBuilder.makeItem(Material.ANVIL, "&5Confirm Quantities"))
        inv.setItem(inv.size - 8, allowedButton)
        inv.setItem(inv.size - 9, ItemBuilder.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&2Add a quantity"))

        page.render(inv)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        val it = event.currentItem ?: return ViewAction.ChangeView(ItemEditorView(editor))
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(ItemEditorView(editor))

        when (it.type) {
            Material.HOPPER -> {
                page.sort()
                build(inv)
                return ViewAction.Pass
            }

            Material.GREEN_STAINED_GLASS_PANE -> {
                quantities += 1
                page.update(quantities)
                build(inv)
                return ViewAction.Pass
            }

            Material.COMPARATOR -> {
                allowed = when (allowed) {
                    Item.Quantities.Allowed.Any -> Item.Quantities.Allowed.Predefined
                    Item.Quantities.Allowed.Predefined -> Item.Quantities.Allowed.Any
                }
                build(inv)
                return ViewAction.Pass
            }

            Material.ANVIL -> {
                val quantities = Item.Quantities(quantities.sorted().toSet().toList(), allowed)
                DannyShop.SHOP.replaceItem(item.iid, item.copy(quantities = quantities))
                return ViewAction.ChangeView(ItemEditorView(editor))
            }

            else -> {}
        }

        if (it.hasKey(QUANTITY_KEY)) {
            var amount = it.keyValue(QUANTITY_KEY)!!

            when (event.click) {
                ClickType.LEFT -> amount += 1
                ClickType.RIGHT -> amount -= 1
                ClickType.SHIFT_LEFT -> amount += 32
                ClickType.SHIFT_RIGHT -> amount -= 32
                ClickType.NUMBER_KEY -> amount += (event.hotbarButton + 1)
                ClickType.DROP -> {
                    quantities.remove(amount)
                    page.update(quantities)
                    build(inv)
                    return ViewAction.Pass
                }

                else -> {}
            }

            quantities[page.indexOf(event.slot)] = amount.coerceIn(1, 64)
            page.update(quantities)
            build(inv)
            return ViewAction.Pass
        }

        page.onClick(event) { build(inv) }
        return ViewAction.Pass
    }

    private class QuantityPage(allowed: List<Int>) : Page<Int>(allowed, Pair(0, 0), Pair(9, 3), Pair(36 - 5, 36 - 4)) {

        init {
            sort()
        }

        fun indexOf(clickedSlot: Int): Int = (page * size) + clickedSlot

        fun sort() {
            items = items.sorted()
        }

        fun update(allowed: List<Int>) {
            items = allowed
        }

        override fun display(inv: Inventory) {
            val items = items.drop(page * size)
                .take(size)
                .map(::makeButton)

            items.onEachIndexed(inv::setItem)
        }

        private fun makeButton(value: Int): ItemStack {
            return ItemBuilder.makeItem(
                Material.POLISHED_ANDESITE, "&2Quantity: &a$value",
                "",
                "&6Left click&e to add 1",
                "&6Right click&e to remove 1",
                "&6Shift left click&e to add 32",
                "&6Shift right click&e to remove 32",
                "&6Number key&e to add",
                "&4Drop&c to remove this quantity",
            ).attachKey(QUANTITY_KEY, value)
        }
    }

}