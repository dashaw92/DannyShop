package me.danny.shop.me.inv.shop

import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.me.inv.shop.ShopMenu.FilterType
import me.danny.shop.model.*
import me.danny.shop.model.Item
import me.danny.shop.model.Item.ItemType
import me.danny.shop.model.Item.ItemType.*
import me.danny.shop.model.Item.Quantities.Allowed.Any
import org.apache.commons.lang.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.*

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
                FilterType.Materials -> it.item is Mat
                FilterType.Items -> it.item is ItemType.Item
                FilterType.Commands -> it.item is Command
                FilterType.Experience -> it.item is Exp
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
        val newPage = filteredItems().indexOfFirst { it.iid.id == item.iid.id } / size
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
        fun String.dist(other: String?): Int {
            return if (other == null) this.length
            else StringUtils.getLevenshteinDistance(other.lowercase(), this.lowercase())
        }

        //this function is only called when query is not null
        val qNotNull = query!!.lowercase()

        fun matches(item: Item): Boolean {
            if (item.name == null) {
                val type = item.item.display().type.name.lowercase()
                return type.contains(qNotNull) || type.dist(qNotNull) <= qNotNull.length / 2
            }

            val name = item.name.lowercase()
            return name.contains(qNotNull) || name.dist(qNotNull) <= qNotNull.length / 2
        }

        val nameSearch = Comparator<Item> { o1, o2 ->
            val dist1 = qNotNull.dist(o1.name?.lowercase())
            val dist2 = qNotNull.dist(o2.name?.lowercase())

            dist1.compareTo(dist2)
        }
        val typeSearch = Comparator<Item> { o1, o2 ->
            val type1 = o1.item.display().type.name.lowercase()
            val type2 = o2.item.display().type.name.lowercase()

            val dist1 = qNotNull.dist(type1)
            val dist2 = qNotNull.dist(type2)

            dist1.compareTo(dist2)
        }

        return items.filter(::matches)
            .sortedWith(nameSearch.thenComparing(typeSearch))
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

        val display: MutableList<String> = fields.map { field -> "&6│ $field" }.toMutableList()
        display.add(0, header)
        display.add(footer)

        return ItemBuilder.addLore(tagged, *display.toTypedArray())
    }

    private fun playerCooldown(item: Item): List<String> {
        //Show the expiration time, but strikethrough it to indicate
        //that they are exempt due to permissions
        val modifier = if (viewer.hasPermission("Perm.ADMIN")) "&m"
        else ""

        val expiration = when (val expiration = CooldownHandler.getCooldownTime(viewer, item.iid)) {
            is Expiration.None -> mutableListOf()
            is Expiration.Never -> mutableListOf("&4${modifier}On cooldown forever")
            is Expiration.Future -> {
                mutableListOf("&9${modifier}Expires:&9 &7$modifier${expiration.format().firstOrNull() ?: "<1s"}")
            }
        }

        if (expiration.isNotEmpty() && viewer.hasPermission("Perm.ADMIN")) {
            expiration += "&7&o(Cooldown bypassed)"
        }

        return expiration
    }
}