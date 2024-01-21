package me.danny.shop.inv.shop.items

import me.danny.shop.model.Item

//https://www.baeldung.com/cs/levenshtein-distance-computation
internal fun String.dist(other: String?): Int {
    if (other.isNullOrEmpty()) return length
    if (isEmpty()) return other.length

    val change = if (this[0] != other[0]) 1 else 0
    val del = substring(1).dist(other) + 1
    val ins = other.substring(1).dist(this) + 1
    val sub = substring(1).dist(other.substring(1)) + change

    return del.coerceAtMost(ins).coerceAtMost(sub)
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