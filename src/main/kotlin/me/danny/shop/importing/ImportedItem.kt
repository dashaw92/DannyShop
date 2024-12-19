package me.danny.shop.importing

import me.danny.shop.model.Category
import me.danny.shop.model.ID
import me.danny.shop.model.Item
import me.danny.shop.model.Item.*

internal data class ImportedItem(
    val iid: ID,
    val item: ItemType,
    var name: String?,
    var cooldown: Cooldown,
    var cost: Cost,
    var quantities: Quantities
) {
    fun build(category: Category): Item = Item(
        iid,
        name,
        item,
        cost,
        cooldown,
        quantities,
        category
    )
}