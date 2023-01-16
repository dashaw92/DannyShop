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

class MinesweeperGUI(
    player: Player,
    options: MinesweeperOptions = MinesweeperOptions.default(),
    val debug: Boolean = false
) : Menu(6, "&dDannySweeper", player) {

    private val game = Minesweeper(options = options)

    init {
        if (debug) viewer.sendMessage("&6[DannyShop] &7Playing in debug mode. Game will not count!".color())
        build()
    }

    override fun build() {
        inv = Bukkit.createInventory(
            this,
            6 * 9,
            "&dDannySweeper &c${game.numFlags()}⚑ &4${game.options.numberOfMines}Ⓑ".color()
        )

        if (debug) renderDebug()
        else renderGame()

        viewer.openInventory(inv)
    }

    private fun renderGame() {
        for (x in 0 until game.width) {
            for (y in 0 until game.height) {
                val invIdx = y * game.width + x

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


                if (game.isFlagged(x to y) && !game.isGameOver()) {
                    inv.setItem(invIdx, ItemBuilder.makeItem(Material.RED_BANNER, "&c⚑"))
                    continue
                }

                inv.setItem(invIdx, item)
            }
        }
    }

    private fun renderDebug() {
        for (x in 0 until game.width) {
            for (y in 0 until game.height) {
                val invIdx = y * game.width + x
                val item = when (val cell = game.cellAt(x to y)) {
                    is Cell.Empty -> nearbyCount(cell.nearby)
                    is Cell.Mine -> ItemBuilder.makeItem(Material.TNT_MINECART, "&4Mine")
                }

                inv.setItem(invIdx, item)
            }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        if (game.isGameOver()) return

        val x = event.slot % game.width
        val y = event.slot / game.width

        val cell = game.cellAt(x to y)
        if (cell is Cell.Empty && !cell.hidden) return

        val move = if (event.isRightClick) Move::FlagCell
        else Move::ExposeCell

        when (val outcome = game.play(move(x, y))) {
            is MoveResult.Success -> {
                if (game.isWin() && !debug) {
                    Bukkit.broadcastMessage("&6[DannyShop] &7${viewer.displayName} &2won&e a round of &dDannySweeper&e!".color())
                    viewer.playSound(viewer.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.0f)
                    Bukkit.getScheduler().runTaskLater(
                        DannyShop.instance(),
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
        val (prefix, material) = when (nearby) {
            0 -> return ItemBuilder.makeItem(Material.GLASS_PANE, " ")
            1 -> "&9" to Material.LIGHT_BLUE_STAINED_GLASS_PANE
            2 -> "&2" to Material.GREEN_STAINED_GLASS_PANE
            3 -> "&6" to Material.ORANGE_STAINED_GLASS_PANE
            4 -> "&1" to Material.BLUE_STAINED_GLASS_PANE
            5 -> "&4" to Material.RED_STAINED_GLASS_PANE
            6 -> "&3" to Material.CYAN_STAINED_GLASS_PANE
            7 -> "&5" to Material.PURPLE_STAINED_GLASS_PANE
            else -> "&8" to Material.BROWN_STAINED_GLASS_PANE
        }

        return ItemBuilder.makeItem(material, nearby, "$prefix$nearby")
    }
}