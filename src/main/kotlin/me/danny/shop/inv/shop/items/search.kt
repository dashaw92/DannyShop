package me.danny.shop.inv.shop.items

import me.danny.shop.model.*
import org.apache.commons.lang.*

internal fun String.dist(other: String?): Int {
    return if (other == null) this.length
    else StringUtils.getLevenshteinDistance(other.lowercase(), this.lowercase())
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
    val type1 = o1.item.display().type.name.replace('_', ' ')
    val type2 = o2.item.display().type.name.replace('_', ' ')

    val dist1 = query.dist(type1)
    val dist2 = query.dist(type2)

    dist1.compareTo(dist2)
}