package me.danny.shop

import me.danny.libinput.providers.*
import me.danny.libinput.providers.Input
import me.danny.shop.utils.*
import org.bukkit.*

internal fun askInput(
    prompt: String,
    signMaterial: Material,
    originalValue: String? = null,
    needsExtraLength: Boolean = false
): InputProvider {
    val provider = if (SignInput.isAvailable() && !needsExtraLength) {
        SignInput()
            .withLines(arrayOf(originalValue ?: "", "^^^^^", "DannyShop", prompt.color()))
            .withMaterial(signMaterial)

    } else {
        ChatInput()
            .requestLines(1)
            .withEscapeWords("cancel")
            .withPrefix("&6[DannyShop] ".color())
            .withPrompt("$prompt:".color())
    }
    return provider
}

internal fun Input.collapse(): String = when (this) {
    is SingleLine -> line
    is MultipleLines -> lines.first()
}