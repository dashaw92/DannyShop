package me.danny.shop.input

import org.bukkit.entity.Player
import java.util.function.BiConsumer

typealias OutputCallback = BiConsumer<Player, Input>
sealed interface Input

data class SingleLine(val line: String) : Input {
    fun toMultiple(): MultipleLines = MultipleLines(listOf(line))
}

data class MultipleLines(val lines: List<String>) : Input {
    fun toSingle(joiner: (List<String>) -> String): SingleLine = SingleLine(joiner(lines))
}

sealed interface InputProvider {
    fun getInput(player: Player, callback: OutputCallback)
}

