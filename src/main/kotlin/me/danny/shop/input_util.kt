package me.danny.shop

import me.danny.shop.input.ChatInput
import me.danny.shop.input.Input
import me.danny.shop.input.InputProvider
import me.danny.shop.input.MultipleLines
import me.danny.shop.input.SingleLine
import me.danny.shop.utils.color
import org.bukkit.Material

internal fun askInput(
    prompt: String,
): InputProvider = ChatInput()
            .requestLines(1)
            .withEscapeWords("cancel")
            .withPrefix("&6[DannyShop]".color())
            .withPrompt("$prompt:".color())

internal fun Input.collapse(): String = when (this) {
    is SingleLine -> line
    is MultipleLines -> lines.first()
}