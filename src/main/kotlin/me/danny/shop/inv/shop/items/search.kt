package me.danny.shop.inv.shop.items

import me.danny.shop.model.Item
import kotlin.math.min

//https://gist.github.com/ademar111190/34d3de41308389a0d0d8
internal fun String.dist(rhs: String?) : Int {
    if(this == rhs) { return 0 }
    if(this.isEmpty()) { return rhs?.length ?: 0}
    if(rhs?.isEmpty() != false) { return this.length }

    val lhsLength = this.length + 1
    val rhsLength = rhs.length + 1

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1..<rhsLength) {
        newCost[0] = i

        for (j in 1..<lhsLength) {
            val match = if(this[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength - 1]
}

internal fun matches(query: String, item: Item): Boolean {
    val maxDist = query.length / 2
    val type = item.item.display().type.name.replace('_', ' ').lowercase()
    //If the material name contains the query, or the distance is within maxDist
    var filter: Boolean = type.contains(query) || type.dist(query) <= maxDist
    if (item.name == null) {
        //can't filter on non-null names
        return filter
    }

    val name = item.name.lowercase()
    //Or if the item's name contains the query
    filter = filter || name.contains(query)
    //Or if the distance of the item's name is within maxDist
    filter = filter || name.dist(query) <= maxDist
    return filter
}

internal fun nameSearch(query: String) = Comparator<Item> { o1, o2 ->
    val dist1 = query.dist(o1.name)
    val dist2 = query.dist(o2.name)

    dist1.compareTo(dist2)
}

internal fun typeSearch(query: String) = Comparator<Item> { o1, o2 ->
    val type1 = o1.item.display().type.name
    val type2 = o2.item.display().type.name

    val dist1 = query.dist(type1)
    val dist2 = query.dist(type2)

    dist1.compareTo(dist2)
}