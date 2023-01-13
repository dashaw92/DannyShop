package me.danny.shop.me.danny.shop.inv.shop

import me.danny.shop.DannyShop
import me.danny.shop.data.Category
import me.danny.shop.data.Item
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Page
import me.danny.shop.me.danny.shop.data.attachKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemPage(private val viewer: Player, coll: Collection<Item>, buttons: Pair<Int, Int>) :
    Page<Item>(coll, Pair(2, 0), Pair(7, 5), buttons) {
    override fun display(inv: Inventory) {
        var invIdx = start.second * 9 + start.first
        for (item in items.drop(page * size).take(size)) {
            inv.setItem(invIdx, makeMenuItem(item))
            invIdx += 1
            if (invIdx % 9 == 0) invIdx += 2
        }
    }

    fun scrollToItem(item: Item) {
        items = DannyShop.SHOP.items(item.category)
        val newPage = items.indexOfFirst { it.iid.id == item.iid.id } / size
        page = newPage
    }

    fun changeCategory(category: Category) {
        page = 0
        items = DannyShop.SHOP.items(category)
    }

    private fun makeMenuItem(item: Item): ItemStack {
        val tagged =
            ItemBuilder.addAttribute(
                item.item.display().attachKey(ShopMenu.ITEM_KEY, item.iid.id),
                ItemFlag.HIDE_ATTRIBUTES
            )

        val header =
            "&6┌┤&4 DannyShop &6├──"
        val footer =
            "&6└───────────"
        val fields: MutableList<String> = mutableListOf()

        fields.add("&dCost:")
        when (item.cost) {
            is Item.Cost.NotSet -> fields.add("&c  No price set!")
            is Item.Cost.Value -> {
                fields.add("  &eBuy: &7\$%,.2f".format(item.cost.buy))
                fields.add("  &eSell: &7\$%,.2f".format(item.cost.sell))
            }
        }

        when (item.cooldown) {
            is Item.Cooldown.None -> fields.add("&dCooldown: &2None")
            is Item.Cooldown.Infinite -> fields.add("&dCooldown: &4Purchasable only once")
            is Item.Cooldown.Duration -> fields.add(
                "&dCooldown: &7${item.cooldown.time.display()}"
            )
        }

        fields.add("")
        fields.add("&e[Purchase: Click]")
        if (viewer.hasPermission("dannyshop.admin")) {
            fields.add("&3[Edit: Right Click]")
        }

        val display: MutableList<String> = fields.map { field -> "&6│ $field" }.toMutableList()
        display.add(0, header)
        display.add(footer)

        return ItemBuilder.addLore(tagged, *display.toTypedArray())
    }
}