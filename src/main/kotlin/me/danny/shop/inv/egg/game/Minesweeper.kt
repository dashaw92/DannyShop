package me.danny.shop.inv.egg.game

import java.util.*

class Minesweeper(
    val width: Int = 9,
    val height: Int = 6,
    private val options: MinesweeperOptions = MinesweeperOptions.default()
) {
    private var lost = false
    private val grid: Array<Cell> = generateBoard()
    private val flags: MutableList<Pair<Int, Int>> = mutableListOf()

    private fun generateBoard(): Array<Cell> {
        val board: Array<Cell> = Array(width * height) { Cell.Empty() }
        val placedMines = mutableListOf<Pair<Int, Int>>()

        while (placedMines.size != options.numberOfMines) {
            val x = (0 until width).random()
            val y = (0 until height).random()

            if (board[y * width + x] is Cell.Mine) continue
            board[y * width + x] = Cell.Mine
            placedMines += Pair(x, y)

            neighborsOf(x, y)
                .map { board[it.second * width + it.first] }
                .filterIsInstance<Cell.Empty>()
                .forEach { it.nearby += 1 }

        }

        return board
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
                        return MoveResult.Success
                    }
                }
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