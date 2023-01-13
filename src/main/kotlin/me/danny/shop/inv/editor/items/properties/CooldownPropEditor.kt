package me.danny.shop.inv.editor.items.properties

import me.danny.shop.DannyShop
import me.danny.shop.data.Item
import me.danny.shop.inv.ItemBuilder
import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.editor.items.ItemEditorView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.me.danny.shop.data.Key
import me.danny.shop.me.danny.shop.data.attachKey
import me.danny.shop.me.danny.shop.data.hasKey
import me.danny.shop.me.danny.shop.data.keyValue
import me.danny.shop.me.danny.shop.inv.fill
import me.danny.shop.me.danny.shop.inv.view.MenuView
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

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
            Material.COMPARATOR, "&6Select cooldown type", *when (mode) {
                State.None -> arrayOf(
                    "&2• &nNone",
                    "&7• Infinite",
                    "&7• Timed",
                    "",
                    "&ePlayers do not have to wait",
                    "&eto purchase this item again."
                )

                State.Infinite -> arrayOf(
                    "&7• None",
                    "&2• &nInfinite",
                    "&7• Timed",
                    "",
                    "&ePlayers can only purchase this",
                    "&eitem one time."
                )

                State.Timed -> arrayOf(
                    "&7• None",
                    "&7• Infinite",
                    "&2• &nTimed",
                    "",
                    "&ePlayers have to wait to purchase",
                    "&ethis item again."
                )
            }
        )

        val confirmButton = ItemBuilder.makeItem(
            Material.ANVIL, "&5Confirm Cooldown", "&eType: &7$mode", *when (mode) {
                State.None -> arrayOf("", "&eThere will be no cooldown upon purchase.")
                State.Infinite -> arrayOf("", "&eThis is a one-time purchasable item.")
                State.Timed -> arrayOf("", "&ePurchasable every &d$duration ${unit.plural(duration)}")
            }
        )

        inv.setItem(inv.size - 9, modeButton)
        inv.setItem(inv.size - 2, confirmButton)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))
    }

    override fun onClick(inv: Inventory, event: InventoryClickEvent): ViewAction {
        val clicked = event.currentItem!!
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(ItemEditorView(editor))

        when (clicked.type) {
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

}