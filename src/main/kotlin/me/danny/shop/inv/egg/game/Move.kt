package me.danny.shop.inv.egg.game

sealed interface Move {
    data class FlagCell(val x: Int, val y: Int) : Move
    data class ExposeCell(val x: Int, val y: Int) : Move

    fun position(): Pair<Int, Int> =
        when (this) {
            is FlagCell -> Pair(this.x, this.y)
            is ExposeCell -> Pair(this.x, this.y)
        }
}