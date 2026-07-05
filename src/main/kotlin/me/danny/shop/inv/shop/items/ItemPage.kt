package me.danny.shop.inv.shop.items

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.data.attachKey
import me.danny.shop.economy.LimitTracking
import me.danny.shop.economy.ResetTask
import me.danny.shop.inv.LoreField
import me.danny.shop.inv.Page
import me.danny.shop.inv.shop.ShopMenu
import me.danny.shop.model.Category
import me.danny.shop.model.Item
import me.danny.shop.model.Item.ItemType
import me.danny.shop.tracking.Graph
import me.danny.shop.tracking.TOTAL_DAYS
import me.danny.shop.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.concurrent.TimeUnit

internal class ItemPage(
    private val viewer: Player,
    coll: Collection<Item>,
    var filterType: FilterType,
    buttons: Pair<Int, Int>
) :
    Page<Item>(coll, Pair(2, 0), Pair(7, 5), buttons) {

    var query: String? = null
    var showGraph: Boolean = false
    var graphMode: GraphMode = GraphMode.All
    var graphSize: GraphSize = GraphSize.Compact

    private fun filteredItems(): List<Item> {
        val filtered = items.filter {
            when (filterType) {
                FilterType.All -> true
                FilterType.Materials -> it.item is ItemType.Mat
                FilterType.Items -> it.item is ItemType.Item
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

        var addPurchaseOption = false
        fields.add("&9Worth:")
        when (item.cost) {
            is Item.Cost.NotSet -> fields.add("&c  No value set!")
            is Item.Cost.Value -> {
                addPurchaseOption = viewer.hasPermission(Perm.SELL)
                fields.add("  &3Each: &7$%,.2f".format(item.cost.buy))
            }
        }

        if (item.sellLimit is Item.SellLimit.Amount) {
            val limit = item.sellLimit.amount.toInt()
            val current = limit - (LimitTracking.remaining(viewer, item.iid) ?: 0)

            var resetNext = ResetTask.nextReset - Instant.now().toEpochMilli()
            var resetFormatted = ""
            val days = TimeUnit.DAYS.convert(resetNext, TimeUnit.MILLISECONDS)
            if (days >= 1) {
                resetNext -= TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS)
                resetFormatted += "${days}d "
            }
            val hours = TimeUnit.HOURS.convert(resetNext, TimeUnit.MILLISECONDS)
            if (hours >= 1) {
                resetNext -= TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS)
                resetFormatted += "${hours}h "
            }
            val minutes = TimeUnit.MINUTES.convert(resetNext, TimeUnit.MILLISECONDS)
            if (minutes >= 1) {
                resetNext -= TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES)
                resetFormatted += "${minutes}m "
            }
            val seconds = TimeUnit.SECONDS.convert(resetNext, TimeUnit.MILLISECONDS)
            if (seconds >= 1) {
                resetFormatted += "${seconds}s"
            }

            if (resetFormatted.isBlank()) resetFormatted = "0s"

            fields.add("")
            fields.add("&5Sell Limit:")
            fields.add("   &7$current/$limit")
            fields.add("   &7Reset in ${resetFormatted.trim()}")
        }

        if (viewer.hasPermission(Perm.ECOLOG_VIEW_VOLUME) && DannyShop.instance().config.ecoAnalyticsEnabled) {
            val hist = DannyShop.instance().analytics.getHistory(item.iid)
            if (hist != null) {
                val day = hist.getFirstNDays(1)
                val month = hist.getFirstNDays(30)
                val all = hist.getFirstNDays(TOTAL_DAYS)

                fields.add("")
                fields.add("&2Volume:")
                fields.add("   &9Today: &7%,d".format(day.sumOf(List<Long>::sum)))
                fields.add("   &9Month: &7%,d".format(month.sumOf(List<Long>::sum)))
                fields.add("   &9${TOTAL_DAYS}d: &7%,d".format(all.sumOf(List<Long>::sum)))
                if (showGraph) {
                    fields.add("")
                    fields.add("   $graphMode &7&o($graphSize)")

                    val dims /* width, height */ = when (graphSize) {
                        GraphSize.Full -> 90 to 14
                        GraphSize.Compact -> 54 to 7
                    }

                    val graph = when (graphMode) {
                        GraphMode.All -> Graph.create(
                            dataByDay = all,
                            timescale = TimeUnit.DAYS,
                            width = dims.first,
                            height = dims.second
                        )

                        GraphMode.Month -> Graph.create(
                            dataByDay = month,
                            timescale = TimeUnit.DAYS,
                            width = dims.first,
                            height = dims.second
                        )

                        GraphMode.Day -> Graph.create(
                            dataByDay = day,
                            timescale = TimeUnit.MINUTES,
                            width = dims.first,
                            height = dims.second
                        )
                    }
                    fields.addAll(graph.map { "   $it" })
                    fields.add("   &e[Change size: Control drop]")
                    fields.add("   &e[Change span: Offhand]")
                }
                fields.add("   &e[Toggle graph: Drop]")
                fields.add("")
            }
        }

        if (addPurchaseOption) {
            fields.add("")
            fields.add("&6Sell:")
            fields.add("   &e[1: Click]")
            fields.add("   &e[X: Right click]")
            fields.add("   &e[All: Shift click]")
        }
        if (viewer.hasPermission("Perm.ADMIN")) {
            //Without this, there will be an ugly
            //empty line for viewers without perms
            //on items with no prices set
            if (!addPurchaseOption) fields.add("")
            fields.add("&3[Edit: Shift right click]")
        }

        return ItemBuilder.addLore(tagged, *fields.build())
    }
}

internal enum class GraphMode {
    All,
    Month,
    Day,
}

internal enum class GraphSize {
    Compact,
    Full,
}