package me.danny.shop.me.danny.shop.inv.shop

import me.danny.shop.DannyShop
import me.danny.shop.data.Category
import me.danny.shop.data.Item
import me.danny.shop.economy.Economy
import me.danny.shop.inv.*
import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.egg.game.MinesweeperHub
import me.danny.shop.inv.shop.CategoryPage
import me.danny.shop.inv.shop.purchasing.PurchaseMenu
import me.danny.shop.me.danny.shop.data.Key
import me.danny.shop.me.danny.shop.data.hasKey
import me.danny.shop.me.danny.shop.data.keyValue
import me.danny.shop.me.danny.shop.inv.HotbarSlotListener
import me.danny.shop.me.danny.shop.inv.color
import me.danny.shop.me.danny.shop.inv.fill
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class ShopMenu(viewer: Player, shopReturnInfo: ShopReturnInfo? = null) : Menu(6, "", viewer), HotbarSlotListener {

    companion object {
        private val cheaters = mutableListOf<UUID>()

        private fun random(): Int = (1..9).random()

        @JvmStatic
        val expected = listOf(random(), random(), random(), random())
        internal val ITEM_KEY = Key("item_iid", PersistentDataType.STRING)
    }

    private val cheatCode: Chord<GameState> = ChordBuilder { GameState(expected, mutableListOf()) }
        .loopStep(expected.size) { event, state ->
            val player = event.whoClicked as Player
            if (event.click != ClickType.NUMBER_KEY) {
                state.current.clear()
                return@loopStep StateResult.Rejected
            }

            state.current += event.hotbarButton + 1

            if (state.current.last() != state.expected[state.current.size - 1]) {
                player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1.0f, (2..4).random() * 0.2f)
                val codes = state.current.map { "&2$it" }
                    .dropLast(1)
                    .toMutableList()
                codes += "&d?"
                player.sendMessage("&6[DannyShop] &7${codes.joinToString()}".color())
                return@loopStep StateResult.ResetSteps
            }

            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, (state.current.size + 1) * 0.2f)
            StateResult.Accepted
        }.withTerminal { state ->
            if (state.current == expected) {
                cheaters += viewer.uniqueId
                viewer.sendMessage("&6[DannyShop] &6☺ &2${expected.joinToString()}".color())
                viewer.playSound(viewer.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f)
                MinesweeperHub(viewer)
            }
        }.build()

    private val shop = DannyShop.SHOP
    private var categories: List<Category> = shop.categories().take(6)
    private val selected = categories.firstOrNull() ?: Category("All", Material.CHEST)
    private var itemPage: ItemPage =
        shopReturnInfo?.itemPage ?: ItemPage(viewer, shop.items(selected), Pair(inv.size - 2, inv.size - 1))
    private var categoryPage: CategoryPage =
        shopReturnInfo?.categoryPage ?: CategoryPage(shop.categories(), selected, Pair(1, 46))

    init {
        build()
    }

    override fun build() {
        if (shop.isEmpty()) {
            showEmptyShop()
            return
        }

        rebuildInv()

        val catBorder = ItemBuilder.makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
        listOf(1, 10, 19, 28, 37, 46)
            .forEach { inv.setItem(it, catBorder) }
        val ctrlBorder = ItemBuilder.makeItem(Material.BLUE_STAINED_GLASS_PANE, " ")
        (47 until inv.size)
            .forEach { inv.setItem(it, ctrlBorder) }

        categoryPage.render(inv)
        itemPage.render(inv)

        if (cheaters.contains(viewer.uniqueId)) {
            inv.setItem(
                inv.size - 7, ItemBuilder.makeItem(
                    Material.TNT_MINECART, "&dPlay DannySweeper",
                    "&2${expected.joinToString(prefix = "[", postfix = "]")}"
                )
            )
        }

        viewer.openInventory(inv)
    }

    private fun rebuildInv() {
        val page = itemPage.page() + 1
        val maxPages = itemPage.numPages()
        inv = Bukkit.createInventory(
            this, 6 * 9,
            "$prefix- ${ChatColor.BLUE}${categoryPage.selected().name} ${ChatColor.DARK_GRAY}(%d/%d)".format(
                page,
                maxPages
            )
        )
    }

    private fun showEmptyShop() {
        inv = Bukkit.createInventory(this, 27, "$prefix- &8Uh oh!".color())
        val filler = ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
        val notice = ItemBuilder.makeItem(
            Material.REDSTONE_TORCH, "&6The shop is empty!",
            "&eBut don't worry! Creating a shop is simple!",
            "&eCheck out the command &d/dannyshop import&e.",
        )
        inv.fill(filler)
        inv.setItem(13, notice)
        viewer.openInventory(inv)
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.currentItem!!.hasKey(ITEM_KEY)) {
            val iid = event.currentItem?.keyValue(ITEM_KEY) ?: ""
            if (iid.trim().isBlank()) return

            val item = shop.itemByIid(iid) ?: return

            val returnInfo = ShopReturnInfo(itemPage, categoryPage)
            if (event.click == ClickType.SHIFT_LEFT && viewer.hasPermission("dannyshop.admin")) {
                ItemEditor(viewer, item.iid, returnInfo)
            } else {
                if (!Economy.hasEconomy()) {
                    viewer.sendMessage("&6[DannyShop] &cCannot purchase this! No economy is active!".color())
                    return
                }

                when (item.cost) {
                    is Item.Cost.Value -> {
                        if (event.click == ClickType.RIGHT) PurchaseMenu(viewer, item.iid, returnInfo)
                        else Economy.purchase(viewer, item.iid, item.cost.buy)
                    }

                    else -> viewer.sendMessage("&6[DannyShop] &cCannot purchase this! No price is set.".color())
                }
            }
            return
        }

        when {
            event.slot % 9 == 0 -> {
                val row = event.slot / 9
                val selected = categoryPage.displayedCategories()[row]
                itemPage.changeCategory(selected)
                categoryPage.changeCategory(selected)
                build()
                return
            }
        }

        itemPage.onClick(event, ::build)
        categoryPage.onClick(event, ::build)

        if (event.slot == inv.size - 7) {
            if (cheaters.contains(viewer.uniqueId)) {
                MinesweeperHub(viewer)
                return
            }

            cheatCode.next(event)
        }
    }

    data class ShopReturnInfo(val itemPage: ItemPage, val categoryPage: CategoryPage)
    data class GameState(val expected: List<Int>, val current: MutableList<Int>)
}