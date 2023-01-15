package me.danny.shop.inv.egg.game

sealed interface Cell {
    object Mine : Cell
    class Empty(var hidden: Boolean = true, var nearby: Int = 0) : Cell
}