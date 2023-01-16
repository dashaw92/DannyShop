package me.danny.shop.inv.egg.game

import java.util.*

class Minesweeper(
    val width: Int = 9,
    val height: Int = 6,
    val options: MinesweeperOptions = MinesweeperOptions.default()
) {
    private var lost = false
    private var minesPlaced = false
    private val grid: Array<Cell> = Array(width * height) { Cell.Empty() }
    private val flags: MutableList<Pair<Int, Int>> = mutableListOf()

    private fun placeMines(avoidAt: Pair<Int, Int>) {
        val placedMines = mutableListOf<Pair<Int, Int>>()

        while (placedMines.size != options.numberOfMines) {
            val mineX = (0 until width).random()
            val mineY = (0 until height).random()

            //do not place a mine here, this was the first
            //move
            if ((mineX to mineY) == avoidAt) continue

            val cell = cellAt(mineX to mineY)
            if (cell is Cell.Mine) continue
            grid[mineY * width + mineX] = Cell.Mine
            placedMines += mineX to mineY

            neighborsOf(mineX, mineY)
                .map(::cellAt)
                .filterIsInstance<Cell.Empty>()
                .forEach { it.nearby += 1 }

        }
    }

    fun play(move: Move): MoveResult {
        val (x, y) = move.position()
        if (!inBounds(move.position())) return MoveResult.OutOfBounds(x, y)
        when (move) {
            is Move.FlagCell -> {
                if (flags.contains(move.position())) flags -= move.position()
                else flags += move.position()
                return MoveResult.Success
            }

            is Move.ExposeCell -> {
                when (val cell = cellAt(move.position())) {
                    is Cell.Mine -> {
                        lost = true
                        val mines = grid.indices
                            .filter { grid[it] is Cell.Mine }
                            .map {
                                val mineX = it % width
                                val mineY = it / width
                                mineX to mineY
                            }.toList()
                        return MoveResult.MinesExploded(move.position(), mines)
                    }

                    is Cell.Empty -> {
                        cell.hidden = false

                        //Placing the mines here ensures that the first
                        //move will never expose a mine and end the game
                        if (!minesPlaced) {
                            placeMines(move.position())
                            minesPlaced = true
                        }

                        if (cell.nearby == 0) {
                            val queue = ArrayDeque<Pair<Int, Int>>()
                            val visited: MutableSet<Pair<Int, Int>> = mutableSetOf(x to y)
                            queue.push(move.position())

                            while (!queue.isEmpty()) {
                                val pos = queue.pop()
                                visited += pos

                                val current = cellAt(pos)
                                if (current !is Cell.Empty) continue
                                current.hidden = false
                                if (current.nearby > 0) continue

                                queue.addAll(neighborsOf(pos).filter { !visited.contains(it) })
                            }
                        }
                    }
                }

                return MoveResult.Success
            }
        }
    }

    fun isWin(): Boolean =
        grid.asSequence()
            .filterIsInstance<Cell.Empty>()
            .all { !it.hidden }

    fun isGameOver(): Boolean = lost || isWin()

    fun cellAt(pos: Pair<Int, Int>): Cell = grid[pos.second * width + pos.first]
    fun isFlagged(pos: Pair<Int, Int>): Boolean = flags.contains(pos)
    fun numFlags(): Int = flags.size

    private fun inBounds(move: Pair<Int, Int>): Boolean =
        (0 until width).contains(move.first)
                && (0 until height).contains(move.second)

    private fun neighborsOf(x: Int, y: Int): Sequence<Pair<Int, Int>> =
        sequenceOf(
            Pair(x - 1, y - 1),
            Pair(x - 1, y),
            Pair(x - 1, y + 1),
            Pair(x, y - 1),
            Pair(x, y + 1),
            Pair(x + 1, y - 1),
            Pair(x + 1, y),
            Pair(x + 1, y + 1)
        ).filter(::inBounds)

    private fun neighborsOf(pos: Pair<Int, Int>) = neighborsOf(pos.first, pos.second)
}