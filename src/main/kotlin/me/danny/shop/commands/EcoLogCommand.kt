package me.danny.shop.commands

import me.danny.shop.DannyShop
import me.danny.shop.Perm
import me.danny.shop.pluginMsg
import me.danny.shop.tracking.EcoLogMgr
import me.danny.shop.tracking.SaleRecord
import me.danny.shop.utils.getElapsed
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentStyle
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

object EcoLogCommand {
    fun onCommand(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission(Perm.ECOLOG)) {
            sender.pluginMsg("&cYou lack permission for this command.")
            return
        }

        if (!DannyShop.instance().config.loggingEnabled) {
            sender.pluginMsg("&cEconomy logging is not enabled.")
            return
        }

        var pageArgIdx = 0
        val logs = if (args.isEmpty()) {
            DannyShop.instance().ecolog.getLogs()
        } else {
            val maybeLog = EcoLogMgr.loadHistorical(args[0])
            if (maybeLog == null) {
                DannyShop.instance().ecolog.getLogs()
            } else {
                pageArgIdx += 1
                maybeLog.records
            }
        }.sortedBy { it.time }.asReversed()

        if (logs.isEmpty()) {
            sender.pluginMsg("&cThere are no sale logs to view.")
            return
        }

        val linesPerPage = 15
        val pages = logs.size / linesPerPage
        val page = args.getOrNull(pageArgIdx)?.toIntOrNull()?.minus(1)?.coerceIn(0, pages) ?: 0

        sender.sendMessage("Showing sale logs (${page + 1}/${pages + 1})")
        (page * linesPerPage until (page + 1) * linesPerPage)
            .filter { idx -> idx < logs.size }
            .map(logs::get)
            .map(::recordToString)
            .forEach(sender.spigot()::sendMessage)

        return
    }

    fun onTabComplete(
        sender: CommandSender,
        arg: String
    ): List<String>? {
        if (!sender.hasPermission(Perm.ECOLOG)) return null
        val available = EcoLogMgr.availableLogs().sorted()
        return available.filter { it.lowercase().startsWith(arg.lowercase()) }
    }
}

private fun recordToString(sale: SaleRecord): BaseComponent {
    val time = calcElapsed(sale.time)
    val info = sellerInfo(sale.seller)
    val item = (DannyShop.SHOP.itemByIid(sale.item)?.itemName() ?: sale.item.id).comp().color(ChatColor.GRAY).italic()
    item.hoverEvent = HoverEvent(
        Action.SHOW_TEXT,
        Text(
            listOf(
                "ID: ".comp().color(ChatColor.BLUE),
                sale.item.id.comp().color(ChatColor.WHITE)
            ).toTypedArray()
        )
    )

    val base = TextComponent()
    base.addExtra(time)
    base.addExtra(" ".comp())
    base.addExtra(info)
    base.addExtra(": ".comp().color(ChatColor.YELLOW))
    base.addExtra(TextComponent("${sale.amount} ").color(ChatColor.LIGHT_PURPLE))
    base.addExtra(item)
    base.addExtra(" ")
    val ext = sale.ext.fmt().comp()
    ext.hoverEvent = HoverEvent(
        Action.SHOW_TEXT,
        Text(
            listOf(
                "Each: ".comp().color(ChatColor.GREEN),
                sale.price.fmt().comp().color(ChatColor.DARK_GREEN)
            ).toTypedArray()
        )
    )
    base.addExtra(ext)
    return base
}

private fun calcElapsed(time: ZonedDateTime): BaseComponent {
    val comp = time.getElapsed().comp().color(ChatColor.DARK_GRAY)
    comp.hoverEvent = HoverEvent(
        Action.SHOW_TEXT,
        Text(
            listOf(
                time.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)).comp().color(ChatColor.DARK_GRAY)
            ).toTypedArray()
        )
    )
    return comp
}

private fun sellerInfo(uuid: UUID): BaseComponent {
    val player = Bukkit.getOfflinePlayer(uuid).name ?: "<???>"

    val comp = "".comp().color(ChatColor.WHITE)
    comp.addExtra(player.comp().color(ChatColor.BLUE))
    return comp
}

internal fun format(bd: BigDecimal): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    return fmt.format(bd)
}

internal fun BigDecimal.fmt(): String = format(this)

internal fun String.comp(): BaseComponent = TextComponent(this)
internal fun BaseComponent.color(c: ChatColor): BaseComponent {
    return this.also { it.applyStyle(ComponentStyle.builder().color(c).build()) }
}

internal fun BaseComponent.italic(): BaseComponent {
    return this.also { it.applyStyle(ComponentStyle.builder().italic(true).build()) }
}