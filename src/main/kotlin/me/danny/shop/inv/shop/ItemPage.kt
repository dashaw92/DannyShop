package me.danny.shop.me.danny.shop.inv.shop

import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.data.Item
import me.danny.shop.data.Item.ItemType
import me.danny.shop.data.Item.ItemType.*
import me.danny.shop.data.Item.Quantities.Allowed.Any
import me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu.FilterType
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu.FilterType.*
import org.bukkit.entity.*
import org.bukkit.inventory.*

class ItemPage(
    private val viewer: Player,
    coll: Collection<Item>,
    var filterType: FilterType,
    buttons: Pair<Int, Int>
) :
    Page<Item>(coll, Pair(2, 0), Pair(7, 5), buttons) {

    private fun filteredItems(): List<Item> = items.filter {
        when (filterType) {
            All -> true
            Materials -> it.item is Mat
            Items -> it.item is ItemType.Item
            Commands -> it.item is Command
            Experience -> it.item is Exp
        }
    }

    override fun numPages(): Int = 1 + filteredItems().size / size

    override fun display(inv: Inventory) {
        var invIdx = start.second * 9 + start.first
        val displayed = filteredItems().drop(page * size).take(size)
        for (item in displayed) {
            inv.setItem(invIdx, makeMenuItem(item))
            invIdx += 1
            if (invIdx % 9 == 0) invIdx += 2
        }
    }

    fun setFilter(filter: FilterType) {
        this.filterType = filter
        if (numPages() < page + 1) page = numPages() - 1
    }

    fun scrollToItem(item: Item) {
        items = DannyShop.SHOP.items(item.category.cid)
        val newPage = filteredItems().indexOfFirst { it.iid.id == item.iid.id } / size
        page = newPage
    }

    fun changeCategory(category: Category) {
        page = 0
        items = DannyShop.SHOP.items(category.cid)
    }

    private fun makeMenuItem(item: Item): ItemStack {
        var tagged =
            ItemBuilder.addAttribute(
                item.item.display().attachKey(ShopMenu.ITEM_KEY, item.iid.id),
                ItemFlag.HIDE_ATTRIBUTES
            )
        if (item.name != null) {
            tagged = ItemBuilder.setName(tagged, item.name)
        }

        val header =
            "&6┌┤&4 DannyShop &6├──"
        val footer =
            "&6└───────────"
        val fields: MutableList<String> = mutableListOf()

        var addPurchaseOption = false
        fields.add("&dCost:")
        when (item.cost) {
            is Item.Cost.NotSet -> fields.add("&c  No price set!")
            is Item.Cost.Value -> {
                addPurchaseOption = true
                fields.add("  &eEach: &7\$%,.2f".format(item.cost.buy))
            }
        }

        when (item.cooldown) {
            is Item.Cooldown.None -> fields.add("&dCooldown: &2None")
            is Item.Cooldown.Infinite -> fields.add("&dCooldown: &4Purchasable only once")
            is Item.Cooldown.Duration -> fields.add(
                "&dCooldown: &7${item.cooldown.time.display()}"
            )
        }

        if (addPurchaseOption) {
            fields.add("")
            fields.add("&e[Purchase: Click]")
            if (item.quantities.allowed == Any || item.quantities.predefined.size > 1) {
                fields.add("&e[Bulk: Right click]")
            }
        }
        if (viewer.hasPermission("dannyshop.admin")) {
            //Without this, there will be an ugly
            //empty line for viewers without perms
            //on items with no prices set
            if (!addPurchaseOption) fields.add("")
            fields.add("&3[Edit: Shift click]")
        }

        val display: MutableList<String> = fields.map { field -> "&6│ $field" }.toMutableList()
        display.add(0, header)
        display.add(footer)

        return ItemBuilder.addLore(tagged, *display.toTypedArray())
    }
}