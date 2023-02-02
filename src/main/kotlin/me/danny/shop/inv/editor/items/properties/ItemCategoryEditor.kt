package me.danny.shop.inv.editor.items.properties

import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.editor.categories.*
import me.danny.shop.inv.editor.items.*
import me.danny.shop.inv.view.*
import me.danny.shop.model.*
import org.bukkit.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*

class ItemCategoryEditor(private val editor: ItemEditor) : MenuView {

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