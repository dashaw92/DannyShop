package me.danny.shop.inv.editor.items.properties

import me.danny.libinput.providers.*
import me.danny.shop.*
import me.danny.shop.data.Item
import me.danny.shop.inv.*
import me.danny.shop.inv.LoreList.toEntry
import me.danny.shop.inv.editor.items.*
import me.danny.shop.inv.view.*
import me.danny.shop.me.danny.shop.data.*
import me.danny.shop.me.danny.shop.inv.*
import me.danny.shop.me.danny.shop.inv.view.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.persistence.*

class CooldownPropEditor(private val editor: ItemEditor) : MenuView {

    private val item = DannyShop.SHOP.itemByIid(editor.item)!!

    companion object {
        private val UNIT_KEY = Key("cdprop_unit", PersistentDataType.STRING)
    }

    override fun onOpen(): ViewAction = ViewAction.Resize(2, "&7- &9Adjust Cooldown")

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)

        placeUnitButtons(inv, 1..8)

        val time = ItemBuilder.makeItem(
            Material.CLOCK, "&d$duration &e${unit.plural(duration)}",
            "&6Left click&e to add 1",
            "&6Right click&e to remove 1",
            "&6Shift left click&e to add 10",
            "&6Shift right click&e to remove 10",
            "&6Number key&e to add"
        )

        inv.setItem(0, time)

        val modeButton = ItemBuilder.makeItem(
            Material.COMPARATOR, "&6Select cooldown type", *LoreList.makeList(
                listOf(
                    State.None toEntry listOf("Players do not have to wait", "to purchase this item again"),
                    State.Infinite toEntry listOf("Players can only purchase this", "item one time."),
                    State.Timed toEntry listOf("Players have to wait to purchase", "this item again.")
                ), mode
            )
        )

        val confirmButton = ItemBuilder.makeItem(
            Material.ANVIL, "&5Confirm Cooldown", "&eType: &7$mode", *when (mode) {
                State.None -> arrayOf("", "&eThere will be no cooldown upon purchase.")
                State.Infinite -> arrayOf("", "&eThis is a one-time purchasable item.")
                State.Timed -> arrayOf("", "&ePurchasable every &d$duration ${unit.plural(duration)}")
            }
        )

        val inputButton = ItemBuilder.makeItem(Material.SPRUCE_SIGN, "&6Set custom cooldown")

        inv.setItem(inv.size - 9, inputButton)
        inv.setItem(inv.size - 8, modeButton)
        inv.setItem(inv.size - 2, confirmButton)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        val clicked = event.currentItem!!
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(ItemEditorView(editor))

        when (clicked.type) {
            Material.SPRUCE_SIGN -> {
                val player = event.whoClicked as Player
                val provider = if (SignInput.isAvailable()) {
                    SignInput()
                        .withLines(arrayOf("", "^^^^^", "DannyShop", "Set cooldown time"))
                        .withMaterial(Material.SPRUCE_WALL_SIGN)
                } else {
                    ChatInput()
                        .withEscapeWords("cancel")
                        .withPrefix("&6[DannyShop] ".color())
                        .withPrompt("&eSet cooldown time:".color())
                        .requestLines(1)
                }

                player.closeInventory()
                provider.getInput(player) { pl, input -> setCooldown(pl, input, inv) }
            }

            Material.CLOCK -> {
                when (event.click) {
                    ClickType.LEFT -> duration += 1
                    ClickType.RIGHT -> duration -= 1
                    ClickType.SHIFT_LEFT -> duration += 10
                    ClickType.SHIFT_RIGHT -> duration -= 10
                    ClickType.NUMBER_KEY -> duration += (event.hotbarButton + 1)
                    else -> {}
                }

                build(inv)
                return ViewAction.Pass
            }

            Material.COMPARATOR -> {
                mode = when (mode) {
                    State.None -> State.Infinite
                    State.Infinite -> State.Timed
                    State.Timed -> State.None
                }

                build(inv)
                return ViewAction.Pass
            }

            Material.ANVIL -> {
                val cooldown = when (mode) {
                    State.None -> Item.Cooldown.None
                    State.Infinite -> Item.Cooldown.Infinite
                    State.Timed -> unit.toTimed(duration)
                }

                val item = item.copy(cooldown = cooldown)
                DannyShop.SHOP.replaceItem(item.iid, item)
                return ViewAction.ChangeView(ItemEditorView(editor))
            }

            else -> {}
        }

        if (clicked.hasKey(UNIT_KEY)) {
            val unitButton = Time.valueOf(clicked.keyValue(UNIT_KEY)!!)
            unit = unitButton
            build(inv)
            return ViewAction.Pass
        }

        return ViewAction.Pass
    }

    private fun placeUnitButtons(inv: Inventory, range: IntRange) {
        Time.values()
            .map {
                val material = when (mode) {
                    State.Timed -> if (it == unit) Material.GREEN_TERRACOTTA else Material.RED_TERRACOTTA
                    else -> if (it == unit) Material.GLASS else Material.COAL_BLOCK
                }
                var item = ItemBuilder.makeItem(
                    material,
                    "&6${it.name}"
                ).attachKey(UNIT_KEY, it.name)

                item = if (mode != State.Timed) {
                    ItemBuilder.addLore(item, "&4Does nothing with current cooldown type.")
                } else {
                    ItemBuilder.addLore(item, "&eSwitch cooldown time to this unit")
                }

                item
            }.zip(range)
            .map { pair -> inv.setItem(pair.second, pair.first) }
    }

    private enum class State {
        None,
        Infinite,
        Timed
    }

    private enum class Time {
        Milliseconds,
        Seconds,
        Minutes,
        Hours,
        Days,
        Weeks,
        Months,
        Years;

        fun plural(time: Long): String {
            return if (time > 1) name
            else name.substring(0, name.length - 1)
        }

        fun toTimed(time: Long): Item.Cooldown.Duration {
            val ctor: (Long) -> Item.Cooldown.Time = when (this) {
                Milliseconds -> Item.Cooldown.Time::Millis
                Seconds -> Item.Cooldown.Time::Seconds
                Minutes -> Item.Cooldown.Time::Minutes
                Hours -> Item.Cooldown.Time::Hours
                Days -> Item.Cooldown.Time::Days
                Weeks -> Item.Cooldown.Time::Weeks
                Months -> Item.Cooldown.Time::Months
                Years -> Item.Cooldown.Time::Years
            }

            return Item.Cooldown.Duration(ctor(time))
        }
    }

    private var mode: State = when (item.cooldown) {
        is Item.Cooldown.None -> State.None
        is Item.Cooldown.Infinite -> State.Infinite
        is Item.Cooldown.Duration -> State.Timed
    }
    private var duration: Long = when (item.cooldown) {
        is Item.Cooldown.Duration -> item.cooldown.time.toExternal()
        else -> 1
    }
    private var unit: Time = when (item.cooldown) {
        is Item.Cooldown.Duration -> when (item.cooldown.time) {
            is Item.Cooldown.Time.Millis -> Time.Milliseconds
            is Item.Cooldown.Time.Seconds -> Time.Seconds
            is Item.Cooldown.Time.Minutes -> Time.Minutes
            is Item.Cooldown.Time.Hours -> Time.Hours
            is Item.Cooldown.Time.Days -> Time.Days
            is Item.Cooldown.Time.Weeks -> Time.Weeks
            is Item.Cooldown.Time.Months -> Time.Months
            is Item.Cooldown.Time.Years -> Time.Years
        }

        else -> Time.Seconds
    }

    private fun setCooldown(player: Player, input: Input, inv: Inventory) {
        val time = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }

        val inputDuration = time.takeWhile { it.isDigit() }
        if (inputDuration.isNotBlank()) {
            val parsedDuration = inputDuration.toLong()
            if (parsedDuration > 0) {
                duration = parsedDuration
            }

            val inputUnit = time.takeLastWhile { it.isLetter() }
            if (inputUnit.isNotBlank()) {
                when (inputUnit.lowercase()) {
                    "ms" -> unit = Time.Milliseconds
                    "s" -> unit = Time.Seconds
                    "m" -> unit = Time.Minutes
                    "h" -> unit = Time.Hours
                    "d" -> unit = Time.Days
                    "w" -> unit = Time.Weeks
                    "mo" -> unit = Time.Months
                    "y" -> unit = Time.Years
                }
            }
        }

        build(inv)
        player.openInventory(inv)
    }
}