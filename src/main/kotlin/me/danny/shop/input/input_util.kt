package me.danny.shop.input

import me.danny.shop.utils.color

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