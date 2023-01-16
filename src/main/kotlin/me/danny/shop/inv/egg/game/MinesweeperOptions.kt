package me.danny.shop.inv.egg.game

data class MinesweeperOptions(val numberOfMines: Int) {
    companion object {
        fun default(): MinesweeperOptions = MinesweeperOptions(5)
    }

    init {
        if (numberOfMines < 1) throw IllegalStateException("Cannot have less than 1 mine!")
    }
}
