package me.danny.shop.inv.egg.game

sealed interface MoveResult {
    object Success : MoveResult
    data class OutOfBounds(val x: Int, val y: Int) : MoveResult
    data class MinesExploded(val origin: Pair<Int, Int>, val mines: List<Pair<Int, Int>>) : MoveResult
}