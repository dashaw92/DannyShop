package me.danny.shop.inv.egg.game

data class MinesweeperOptions(val numberOfMines: Int) {
    companion object {
        fun default(): MinesweeperOptions = MinesweeperOptions(5)
    }
}
