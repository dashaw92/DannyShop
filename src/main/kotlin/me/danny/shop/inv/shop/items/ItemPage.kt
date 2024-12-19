package me.danny.shop.inv.shop.items

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.data.CooldownHandler
import me.danny.shop.data.Expiration
import me.danny.shop.data.attachKey
import me.danny.shop.inv.LoreField
import me.danny.shop.inv.Page
import me.danny.shop.me.inv.shop.ShopMenu
import me.danny.shop.model.Category
import me.danny.shop.model.Item
import me.danny.shop.model.Item.ItemType
import me.danny.shop.model.Item.Quantities.Allowed.Any
import me.danny.shop.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

internal class ItemPage(
    private val viewer: Player,
    coll: Collection<Item>,
    var filterType: FilterType,
    buttons: Pair<Int, Int>
) :
    Page<Item>(coll, Pair(2, 0), Pair(7, 5), buttons) {

    var query: String? = null

    private fun filteredItems(): List<Item> {
        val filtered = items.filter {
            when (filterType) {
                FilterType.All -> true
                FilterType.Materials -> it.item is ItemType.Mat
                FilterType.Items -> it.item is ItemType.Item
                FilterType.Commands -> it.item is ItemType.Command
                FilterType.Experience -> it.item is ItemType.Exp
            }
        }

        val searched = if (query != null) {
            filterAndSearch(filtered)
        } else filtered

        return searched
    }

    override fun numPages(): Int = 1 + filteredItems().size / size

    override fun display(inv: Inventory) {
        if (filteredItems().isEmpty()) {
            val filler = ItemBuilder.makeItem(Material.RED_STAINED_GLASS_PANE, " ")
            val info = when {
                items.isEmpty() -> ItemBuilder.makeItem(
                    Material.BARRIER, "&cNo items",
                    "&7This category has no items."
                )

                query != null && filteredItems().isEmpty() -> ItemBuilder.makeItem(
                    Material.BARRIER, "&cNo items",
                    "&7No items match your search criteria."
                )

                else -> ItemBuilder.makeItem(
                    Material.BARRIER, "&cNo items",
                    "&7This category has no items that",
                    "&7match your filtering mode."
                )
            }

            renderRect(inv, filler)
            val midX = start.first + dim.first / 2
            val midY = start.second + dim.second / 2
            val invIdx = midY * 9 + midX
            inv.setItem(invIdx, info)
            return
        }

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
        val newPage = filteredItems().indexOfFirst { it.iid == item.iid } / size
        page = newPage
    }

    fun changeCategory(category: Category) {
        page = 0
        items = DannyShop.SHOP.items(category.cid)
    }

    fun searchFor(query: String?) {
        this.query = query
    }

    /*
    * Operation:
    * 1) Filter down to close matches
    * 2) Sort by match distance descending
    *
    * Filtering:
    *   Both the query and <comparing string> are lowercased.
    *   Uses Levenshtein distance on the item's name (if present),
    *   against the search query. If the distance is less than half
    *   than the length of the query, or the name contains the query,
    *   it's considered a match.
    *   If the item has no name, then the Material name of the item's
    *   display is used. Repeat above steps with material.
    *
    * Sorting:
    *   Both the query and <comparing string> are lowercased.
    *   Uses levenshtein distance against the name or material type,
    *   sorting the items by closest to farthest distance.
    */
    private fun filterAndSearch(items: List<Item>): List<Item> {

        //this function is only called when query is not null
        val qNotNull = query?.lowercase() ?: return items

        return items.filter { matches(qNotNull, it) }
            .sortedWith(
                nameSearch(qNotNull)
                    .thenComparing(typeSearch(qNotNull))
            )
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

        val fields = LoreField()

        var addPurchaseOption = viewer.hasPermission(Perm.ADMIN)
        fields.add("&9Cost:")
        when (item.cost) {
            is Item.Cost.NotSet -> fields.add("&c  No price set!")
            is Item.Cost.Value -> {
                addPurchaseOption = addPurchaseOption || !CooldownHandler.isOnCooldown(viewer, item.iid)
                fields.add("  &3Each: &7\$%,.2f".format(item.cost.buy))
            }
        }

        when (item.cooldown) {
            is Item.Cooldown.None -> { /*fields.add("&9Cooldown: &2None")*/
            }

            is Item.Cooldown.Infinite -> fields.add("&9Cooldown: &4Forever")
            is Item.Cooldown.Duration -> fields.add(
                "&9Cooldown: &7${item.cooldown.time.display()}"
            )
        }
        fields.addAll(playerCooldown(item))

        if (addPurchaseOption) {
            fields.add("")
            fields.add("&e[Purchase: Click]")
            if (item.quantities.allowed == Any || item.quantities.predefined.size > 1) {
                fields.add("&e[Bulk: Right click]")
            }
        }
        if (viewer.hasPermission("Perm.ADMIN")) {
            //Without this, there will be an ugly
            //empty line for viewers without perms
            //on items with no prices set
            if (!addPurchaseOption) fields.add("")
            fields.add("&3[Edit: Shift click]")
        }

        return ItemBuilder.addLore(tagged, *fields.build())
    }

    private fun playerCooldown(item: Item): List<String> {
        //Show the expiration time, but strikethrough it to indicate
        //that they are exempt due to permissions
        val modifier = if (viewer.hasPermission(Perm.ADMIN)) "&m"
        else ""

        val expiration = when (val expiration = CooldownHandler.getCooldownTime(viewer, item.iid)) {
            is Expiration.None -> mutableListOf()
            is Expiration.Never -> mutableListOf("&4${modifier}On cooldown forever")
            is Expiration.Future -> {
                mutableListOf("&9${modifier}Expires:&9 &7$modifier${expiration.format().firstOrNull() ?: "<1s"}")
            }
        }

        if (expiration.isNotEmpty() && viewer.hasPermission(Perm.ADMIN)) {
            expiration += "&7&o(Cooldown bypassed)"
        }

        return expiration
    }
}
