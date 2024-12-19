package me.danny.shop.inv.editor.items.properties

import me.danny.shop.DannyShop
import me.danny.shop.data.hasKey
import me.danny.shop.data.keyValue
import me.danny.shop.inv.editor.categories.CategoryEditor
import me.danny.shop.inv.editor.categories.CategoryPages
import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.editor.items.ItemEditorView
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.model.ID
import me.danny.shop.model.Shop
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.fill
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

internal class ItemCategoryEditor(private val editor: ItemEditor) : MenuView {

    private val item = DannyShop.SHOP.itemByIid(editor.item)!!
    private val page = CategoryPages(Pair(36 - 5, 36 - 4), item.category)

    override fun onOpen(): ViewAction = ViewAction.Resize(5, "&7- &9Change Category")

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.ORANGE_STAINED_GLASS_PANE, " ")
        inv.fill(filler)
        page.render(inv)

        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        val clicked = event.currentItem!!
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(ItemEditorView(editor))

        if (clicked.hasKey(CategoryEditor.CATEGORY_KEY)) {
            val category = Shop.getCategory(ID(clicked.keyValue(CategoryEditor.CATEGORY_KEY)!!))!!
            val clone = item.copy(category = category)
            DannyShop.SHOP.replaceItem(item.iid, clone)
            page.selected = category

            editor.returnInfo.categoryPage.changeCategory(category)
            editor.returnInfo.itemPage.scrollToItem(clone)
            return ViewAction.ChangeView(ItemEditorView(editor))
        }

        page.onClick(event) { build(inv) }
        return ViewAction.Pass
    }
}