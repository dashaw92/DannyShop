package me.danny.shop.inv.egg.game

import me.danny.shop.inv.ChordBuilder
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Menu
import me.danny.shop.inv.StateResult
import me.danny.shop.inv.egg.MinesweeperGUI
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.shop.ShopMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class MinesweeperHub(player: Player) : Menu(3, "- &dDannySweeper", player) {

    private var mines: Int = 5

    init {
        build()
    }

    private val debugAction = ChordBuilder { mutableListOf<Int>() }
        .loopStep(4) { event, state ->
            if (event.slot == 0) {
                if (event.click != ClickType.NUMBER_KEY) return@loopStep StateResult.ResetSteps
                state += event.hotbarButton + 1

                StateResult.Accepted
            } else StateResult.Rejected
        }.withTerminal { state ->
            if (state == listOf(1, 9, 9, 9)) {
                MinesweeperGUI(viewer, MinesweeperOptions(mines), debug = true)
            }
        }.build()

    override fun build() {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)
        inv.setItem(13, ItemBuilder.makeItem(Material.TNT_MINECART, "&6Play DannySweeper"))
        inv.setItem(
            12, ItemBuilder.makeItem(
                Material.PUFFERFISH, "&6Number of mines: &d$mines",
                "",
                "&eRight click to remove 1",
                "&eLeft click to add 1"
            )
        )
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.slot == inv.size - 1) {
            ShopMenu(viewer)
            return
        }

        debugAction.next(event)

        val item = event.currentItem!!
        when (item.type) {
            Material.TNT_MINECART -> MinesweeperGUI(viewer, MinesweeperOptions(mines))
            Material.PUFFERFISH -> {
                val amount = when (event.click) {
                    ClickType.RIGHT -> mines - 1
                    ClickType.LEFT -> mines + 1
                    else -> mines
                }

                mines = amount.coerceIn(1..50)
                build()
            }

            else -> {}
        }
    }

}