package me.danny.shop.importing

import me.danny.shop.*
import me.danny.shop.inv.*
import me.danny.shop.model.*
import me.danny.shop.model.Item.*
import me.danny.shop.model.Item.Quantities.Allowed.Any
import me.danny.shop.model.Item.Quantities.Allowed.Predefined
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.ClickEvent.Action.CHANGE_PAGE
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.hover.content.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.*
import java.util.*
import net.md_5.bungee.api.ChatColor as Color

internal class ImportSession(
    private val who: Player,
    private val category: Category,
    private val items: MutableList<ImportedItem>
) {
    companion object {
        private val sessions: MutableMap<UUID, ImportSession> = mutableMapOf()
        fun isInSession(uuid: UUID): Boolean = sessions.containsKey(uuid)
        fun getSession(uuid: UUID): ImportSession? = sessions[uuid]
        fun delete(uuid: UUID) {
            sessions -= uuid
        }
    }

    init {
        sessions[who.uniqueId] = this
    }

    private val iidMap: Map<Int, ID> = items.zip(0..53).associate { (item, page) -> page to item.iid }
    private val book = generateBook()
    private var lastPage: Int? = null

    fun updateItem(id: String, prop: String, value: String) {
        val item = items.find { it.iid.id == id } ?: return
        when (prop.lowercase()) {
            "name" -> item.name = value
            "cooldown" -> item.cooldown = Cooldown.parse(value.lowercase())
            "cost" -> item.cost = when (value.trim()) {
                "" -> Cost.NotSet
                else -> {
                    val price = value.toDoubleOrNull()
                    if (price == null) Cost.NotSet
                    else Cost.Value(price)
                }
            }

            "qty" -> item.quantities =
                Quantities(value.split(' ').mapNotNull { it.toIntOrNull() }, item.quantities.allowed)

            "qtymode" -> item.quantities = Quantities(
                item.quantities.predefined, when (value.lowercase()) {
                    "any" -> Any
                    "predefined" -> Predefined
                    else -> Any
                }
            )

            else -> return
        }
        updatePage(iidMap.entries.find { (_, otherId) -> id == otherId.id }?.key ?: return)
    }

    private fun updatePage(page: Int) {
        val meta = book.itemMeta!! as BookMeta
        if (meta.pageCount <= page) return
        lastPage = page
        meta.spigot().setPage(1, infoPage())
        meta.spigot().setPage(page + 2, buildPage(iidMap[page]!!))
        book.itemMeta = meta
        openEditor()
    }

    fun openEditor() {
        who.openBook(book)
    }

    fun importAll() {
        items.asSequence()
            .map { it.build(category) }
            .forEach { item ->
                who.pluginMsg("Imported &e${item.iid.id}&7.")
                DannyShop.SHOP.addItem(item)
            }
    }

    private fun generateBook(): ItemStack {
        val book = ItemBuilder.makeItem(Material.WRITTEN_BOOK, "DannyShop")
        val meta = book.itemMeta!! as BookMeta
        meta.spigot().addPage(arrayOf(infoPage()))
        iidMap.forEach { (_, iid) ->
            meta.spigot().addPage(arrayOf(buildPage(iid)))
        }
        meta.author = "Danny"
        meta.title = "DannyShop"
        book.itemMeta = meta
        return book
    }

    private fun infoPage(): BaseComponent {
        fun msg(msg: String): TextComponent = TextComponent(msg).apply { color = Color.BLACK }

        val base = TextComponent("DannyShop Import\n").apply { color = Color.DARK_RED }
        base.addExtra(
            msg(
                """
            
            Each page represents on item to be imported.
            
            Click the values to change them.
            
            When you're done, click this:
            
            
        """.trimIndent()
            )
        )
        base.addExtra(TextComponent("[Done]").apply {
            color = Color.DARK_BLUE
            isBold = true
            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to finish editing"))
            clickEvent = ClickEvent(RUN_COMMAND, "/dannyshop:shop import finish")
        })
        if (lastPage != null) {
            base.addExtra("   ")
            base.addExtra(TextComponent("[Return]").apply {
                color = Color.DARK_GREEN
                clickEvent = ClickEvent(CHANGE_PAGE, "${lastPage!! + 2}")
                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to return to last edited item"))
                isBold = true
            })
        }
        return base
    }

    private fun buildPage(id: ID): BaseComponent {
        fun addProp(prop: String): TextComponent = TextComponent("$prop\n").apply { color = Color.BLUE }
        fun TextComponent.newLine() {
            addExtra("\n\n")
        }

        fun TextComponent.addPropEvent(prop: String): TextComponent {
            clickEvent = ClickEvent(RUN_COMMAND, "/dannyshop:shop import set ${id.id} $prop")
            return this
        }

        val item = items.find { it.iid == id } ?: return TextComponent("Invalid IID $id")
        val base = TextComponent()
        base.addExtra(TextComponent("${item.iid.id}\n").apply {
            color = Color.GOLD
            hoverEvent = HoverEvent(SHOW_TEXT, Text("${item.item}"))
        })
        base.addExtra(addProp("Name:"))
        base.addExtra(when (item.name) {
            null -> TextComponent("Not set").apply { color = Color.RED }
            else -> TextComponent(item.name).apply { color = Color.BLACK }
        }.apply {
            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to set name"))
        }.addPropEvent("name")
        )
        base.newLine()

        base.addExtra(addProp("Cooldown:"))
        base.addExtra(when (item.cooldown) {
            is Cooldown.None -> TextComponent("None").apply { color = Color.DARK_GREEN }
            is Cooldown.Infinite -> TextComponent("Infinite").apply { color = Color.DARK_RED }
            is Cooldown.Duration -> TextComponent((item.cooldown as Cooldown.Duration).time.display()).apply {
                color = Color.BLACK
            }
        }.apply {
            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to change cooldown"))
        }.addPropEvent("cooldown")
        )
        base.newLine()

        base.addExtra(addProp("Cost:"))
        base.addExtra(when (item.cost) {
            is Cost.NotSet -> TextComponent("Not set").apply { color = Color.RED }
            is Cost.Value -> TextComponent("$%,.2f".format((item.cost as Cost.Value).buy)).apply {
                color = Color.BLACK
            }
        }.apply {
            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to adjust price"))
        }.addPropEvent("cost")
        )
        base.newLine()

        base.addExtra(addProp("Quantities:"))
        base.addExtra(TextComponent(item.quantities.predefined.joinToString(separator = ", ", limit = 6)).apply {
            color = Color.BLACK
            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to adjust quantities"))
        }.addPropEvent("qty"))
        base.addExtra("\n")
        base.addExtra(when (item.quantities.allowed) {
            Any -> TextComponent("Any")
            Predefined -> TextComponent("Predefined")
        }.apply {
            color = Color.BLACK
            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to set allowed mode"))
        }.addPropEvent("qtymode")
        )
        base.addExtra("\n")

        base.addExtra(TextComponent("[Return to Home]").apply {
            color = Color.DARK_PURPLE
            isBold = true
            hoverEvent = HoverEvent(SHOW_TEXT, Text("Return to page 1"))
            clickEvent = ClickEvent(CHANGE_PAGE, "0")
        })
        return base
    }

    internal fun categoryID(): ID {
        return category.cid
    }
}