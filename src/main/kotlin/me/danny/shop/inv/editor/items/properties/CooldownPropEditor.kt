package me.danny.shop.inv.editor.items.properties

import me.danny.libinput.providers.*
import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.inv.*
import me.danny.shop.inv.LoreList.toEntry
import me.danny.shop.inv.editor.items.*
import me.danny.shop.inv.view.*
import me.danny.shop.model.Item.Cooldown
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
            Material.CLOCK, "&d$duration &6${unit.plural(duration)}",
            "&eLeft click&7 to add 1",
            "&eRight click&7 to remove 1",
            "&eShift left click&7 to add 10",
            "&eShift right click&7 to remove 10",
            "&eNumber key&7 to add"
        )

        inv.setItem(0, time)

        val modeButton = ItemBuilder.makeItem(
            Material.COMPARATOR, "&eSelect cooldown type", *LoreList.makeList(
                listOf(
                    State.None toEntry listOf("Players do not have to wait", "to purchase this item again"),
                    State.Infinite toEntry listOf("Players can only purchase this", "item one time."),
                    State.Timed toEntry listOf("Players have to wait to purchase", "this item again.")
                ), mode
            )
        )

        val confirmButton = ItemBuilder.makeItem(
            Material.ANVIL, "&5Confirm Cooldown", "&9Type: &7$mode", *when (mode) {
                State.None -> arrayOf("", "&7There will be no cooldown upon purchase.")
                State.Infinite -> arrayOf("", "&7Purchasable one time.")
                State.Timed -> arrayOf("", "&7Purchasable every &e$duration ${unit.plural(duration)}")
            },
            "",
            "&e[Confirm: Click]"
        )

        val inputButton = ItemBuilder.makeItem(Material.SPRUCE_SIGN, "&eSet custom cooldown")

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
                player.closeInventory()
                DannyShop.askInput("&9Set cooldown time", Material.SPRUCE_WALL_SIGN)
                    .getInput(player) { pl, input -> setCooldown(pl, input, inv) }
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
                    State.None -> Cooldown.None
                    State.Infinite -> Cooldown.Infinite
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
                    "&e${it.name}"
                ).attachKey(UNIT_KEY, it.name)

                if (mode != State.Timed) {
                    item = ItemBuilder.addLore(item, "&7Does nothing with current cooldown type.")
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

        fun suffix(): String {
            return when (this) {
                Milliseconds -> "ms"
                Seconds -> "s"
                Minutes -> "m"
                Hours -> "h"
                Days -> "d"
                Weeks -> "w"
                Months -> "mo"
                Years -> "y"
            }
        }

        fun plural(time: Long): String {
            return if (time > 1) name
            else name.substring(0, name.length - 1)
        }

        fun toTimed(time: Long): Cooldown.Duration {
            val ctor: (Long) -> Cooldown.Time = when (this) {
                Milliseconds -> Cooldown.Time::Millis
                Seconds -> Cooldown.Time::Seconds
                Minutes -> Cooldown.Time::Minutes
                Hours -> Cooldown.Time::Hours
                Days -> Cooldown.Time::Days
                Weeks -> Cooldown.Time::Weeks
                Months -> Cooldown.Time::Months
                Years -> Cooldown.Time::Years
            }

            return Cooldown.Duration(ctor(time))
        }
    }

    private var mode: State = when (item.cooldown) {
        is Cooldown.None -> State.None
        is Cooldown.Infinite -> State.Infinite
        is Cooldown.Duration -> State.Timed
    }

    private var duration: Long = when (item.cooldown) {
        is Cooldown.Duration -> item.cooldown.time.time
        else -> 1
    }
    private var unit = when (item.cooldown) {
        is Cooldown.Duration -> when (item.cooldown.time) {
            is Cooldown.Time.Millis -> Time.Milliseconds
            is Cooldown.Time.Seconds -> Time.Seconds
            is Cooldown.Time.Minutes -> Time.Minutes
            is Cooldown.Time.Hours -> Time.Hours
            is Cooldown.Time.Days -> Time.Days
            is Cooldown.Time.Weeks -> Time.Weeks
            is Cooldown.Time.Months -> Time.Months
            is Cooldown.Time.Years -> Time.Years
        }

        else -> Time.Seconds
    }

    private fun setCooldown(player: Player, input: Input, inv: Inventory) {
        val time = when (input) {
            is SingleLine -> input.line
            is MultipleLines -> input.lines.first()
        }

        val inputDuration = time.takeWhile { it.isDigit() || it == '-' }.toLongOrNull()
        if (inputDuration != null) {
            val parsedDuration = inputDuration.toLong()
            if (parsedDuration > 0) {
                duration = parsedDuration
                val inputUnit = time.takeLastWhile { it.isLetter() }
                if (inputUnit.isNotBlank()) {
                    unit = when (inputUnit.lowercase()) {
                        "ms" -> Time.Milliseconds
                        "s" -> Time.Seconds
                        "m" -> Time.Minutes
                        "h" -> Time.Hours
                        "d" -> Time.Days
                        "w" -> Time.Weeks
                        "mo" -> Time.Months
                        "y" -> Time.Years
                        else -> Time.Seconds
                    }
                }
                mode = State.Timed
                player.sendMessage("&6[DannyShop] &7Set cooldown to &9Timed&7: $duration ${unit.plural(duration)}.".color())
            } else {
                player.sendMessage("&6[DannyShop] &7Set cooldown to &4Infinite&7.".color())
                mode = State.Infinite
            }
        } else {
            player.sendMessage("&6[DannyShop] &7Removed cooldown.".color())
            mode = State.None
        }

        build(inv)
        player.openInventory(inv)
    }
}