package me.danny.shop.inv

import me.danny.shop.data.Category
import me.danny.shop.data.Shop
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ShopMenu(private val shop: Shop, viewer: Player) : Menu(6, "${ChatColor.GOLD}DannyShop", viewer) {

    private var categories: List<Category> = shop.categories().take(6)
    private var selected: Category

    init {
        selected = categories.firstOrNull() ?: Category("All", Material.CHEST)
        build()
    }

    override fun build() {
        inventory.clear()

        categories
            .map {
                var item = Item.makeItem(it.display, "${ChatColor.BLUE}${it.name}")
                if(it == selected) item = Item.addEnchantGlow(item)
                item
            }
            .forEachIndexed { index, item -> inventory.setItem(index * 9, item) }

        val catBorder = Item.makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
        listOf(1, 10, 19, 28, 37, 46)
            .forEach { inv.setItem(it, catBorder) }
        val ctrlBorder = Item.makeItem(Material.BLUE_STAINED_GLASS_PANE, " ")
        (38 until 45)
            .forEach { inv.setItem(it, ctrlBorder) }

        val items = shop.items(selected)
        var index = 2
        for (item in items) {
            inventory.setItem(index, item.item.display())
            index += 1
            if(index % 9 == 0) {
                index += 2
            }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        if(event.slot % 9 == 0) {
            val row = event.slot / 9
            selected = categories[row]
            build()
        }
    }

}