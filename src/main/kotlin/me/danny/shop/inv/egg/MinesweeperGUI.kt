package me.danny.shop.inv.egg

import me.danny.shop.DannyShop
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.Menu
import me.danny.shop.inv.egg.game.*
import me.danny.shop.me.danny.shop.inv.color
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class MinesweeperGUI(player: Player) : Menu(6, "- &dDannySweeper", player) {

    private val game = Minesweeper()

    init {
        build()
    }

    override fun build() {
        for (x in 0 until game.width) {
            for (y in 0 until game.height) {
                val invIdx = y * game.width + x

                if (game.isFlagged(x to y)) {
                    inv.setItem(invIdx, ItemBuilder.makeItem(Material.RED_BANNER, "&c?"))
                    continue
                }

                val item = when (val cell = game.cellAt(x to y)) {
                    is Cell.Empty -> {
                        if (cell.hidden) ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
                        else nearbyCount(cell.nearby)
                    }

                    is Cell.Mine -> {
                        if (game.isGameOver()) ItemBuilder.makeItem(Material.TNT_MINECART, "&4Mine")
                        else ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
                    }
                }

                inv.setItem(invIdx, item)
            }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        if (game.isGameOver()) return

        val x = event.slot % game.width
        val y = event.slot / game.width

        val move = if (event.isRightClick) Move::FlagCell
        else Move::ExposeCell

        when (val outcome = game.play(move(x, y))) {
            is MoveResult.Success -> {
                if (game.isWin()) {
                    Bukkit.broadcastMessage("&6[DannyShop] &7${viewer.displayName} &2won&e a round of &dDannySweeper&e!".color())
                    viewer.playSound(viewer.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.0f)
                    Bukkit.getScheduler().runTaskLater(DannyShop.instance(),
                        Runnable { MinesweeperHub(viewer) }, 20 * 3L
                    )
                }
                viewer.playSound(viewer.location, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f, 1.0f)
                build()
            }

            is MoveResult.MinesExploded -> {
                viewer.playSound(viewer.location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f)
                inv.setItem(y * game.width + x, ItemBuilder.makeItem(Material.COBWEB, "&4Boom!"))
                for (mine in outcome.mines) {
                    if (mine == outcome.origin) continue

                    val mineX = mine.first
                    val mineY = mine.second
                    val invIdx = mineY * game.width + mineX
                    inv.setItem(invIdx, ItemBuilder.makeItem(Material.TNT_MINECART, "&4Mine"))
                }

                viewer.sendMessage("&6[DannyShop] &cYou lose!".color())
                Bukkit.getScheduler().runTaskLater(DannyShop.instance(),
                    Runnable { MinesweeperHub(viewer) }, 20 * 3L
                )
            }

            is MoveResult.OutOfBounds -> {}
        }
    }

    private fun nearbyCount(nearby: Int): ItemStack {
        val material = when (nearby) {
            0 -> return ItemBuilder.makeItem(Material.GRAY_STAINED_GLASS_PANE, " ")
            1 -> Material.LIGHT_BLUE_STAINED_GLASS_PANE
            2 -> Material.GREEN_STAINED_GLASS_PANE
            3 -> Material.ORANGE_STAINED_GLASS_PANE
            4 -> Material.BLUE_STAINED_GLASS_PANE
            5 -> Material.RED_STAINED_GLASS_PANE
            6 -> Material.CYAN_STAINED_GLASS_PANE
            7 -> Material.PURPLE_STAINED_GLASS_PANE
            else -> Material.BROWN_STAINED_GLASS_PANE
        }

        return ItemBuilder.makeItem(material, nearby, "&4$nearby")
    }
}