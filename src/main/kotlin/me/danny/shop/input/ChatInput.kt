package me.danny.shop.input

import me.danny.shop.DannyShop
import me.danny.shop.utils.color
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.UUID

class ChatInput : InputProvider {

    companion object {
        private val sessions: MutableMap<UUID, ChatInput> = mutableMapOf()
    }

    private var promptText = "Please provide input:"
    private var prefix = "&6[DannyShop] &e"
    private var numberOfLines = 1
    private val escapeWords: MutableList<String> = mutableListOf()
    private var infoMessage: List<String> = listOf(
        "&7Your next message will be captured.",
        "&7Please type your input in chat."
    )

    private lateinit var callback: OutputCallback
    private val readInput: MutableList<String> = mutableListOf()

    fun requestLines(numberOfLines: Int): ChatInput {
        if (numberOfLines > 0) this.numberOfLines = numberOfLines
        return this
    }

    fun withPrompt(prompt: String) = apply { promptText = prompt }
    fun withPrefix(prefix: String) = apply { this.prefix = prefix }
    fun withEscapeWords(vararg words: String) = apply { words.forEach(escapeWords::add) }
    fun withInfoMessage(message: List<String>) = apply { infoMessage = message }

    override fun getInput(player: Player, callback: OutputCallback) {
//        ConversationStarter.getForPlayer(
//            player,
//            promptText,
//            prefix,
//            callback,
//            numberOfLines,
//            infoMessage,
//            *escapeWords.toTypedArray()
//        )
//            .begin()
        this.callback = callback
        sessions[player.uniqueId] = this

        player.sendMessage("$prefix $promptText".color())
        infoMessage.forEach { player.sendMessage(it.color()) }
    }

    object ChatInputListener : Listener {
        @EventHandler
        fun onChat(event: AsyncPlayerChatEvent) {
            val session = sessions[event.player.uniqueId] ?: return
            event.isCancelled = true

            if (session.escapeWords.any { it.equals(event.message, ignoreCase = true)}) {
                sessions -= event.player.uniqueId
                Bukkit.getScheduler().runTaskLater(DannyShop.instance(), Runnable {
                    session.callback.accept(event.player, SingleLine("N/A"))
                }, 0L)
                return
            }

            session.readInput += event.message

            if (session.readInput.size == session.numberOfLines) {
                sessions -= event.player.uniqueId

                val input = when (session.numberOfLines) {
                    1 -> SingleLine(session.readInput.first())
                    else -> MultipleLines(session.readInput)
                }
                Bukkit.getScheduler().runTaskLater(DannyShop.instance(), Runnable {
                    session.callback.accept(event.player, input)
                }, 0L)
            }
        }
    }
}