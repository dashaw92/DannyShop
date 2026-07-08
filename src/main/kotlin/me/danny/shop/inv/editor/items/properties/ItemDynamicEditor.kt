package me.danny.shop.inv.editor.items.properties

import me.danny.shop.DannyShop
import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.editor.items.ItemEditorView
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
import me.danny.shop.model.Item
import me.danny.shop.utils.ItemBuilder
import me.danny.shop.utils.fill
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

internal class ItemDynamicEditor(private val editor: ItemEditor) : MenuView {
    override fun onOpen(): ViewAction = ViewAction.Resize(3, "&7- &dDynamic Pricing (WIP)")

    override fun build(inv: Inventory) {
        val filler = ItemBuilder.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ")
        inv.fill(filler)
        inv.setItem(inv.size - 1, ItemBuilder.makeItem(Material.ARROW, "&9Back"))

        val item = DannyShop.SHOP.itemByIid(editor.item)!!

        val toggleEnabledButton = ItemBuilder.makeItem(Material.LEVER, "&eToggle enabled", *toggleEnabled(item))
        val demandButton = ItemBuilder.makeItem(Material.LAVA_BUCKET, "&eServer Demand", *serverDemand(item))
        val replenishTicksButton =
            ItemBuilder.makeItem(Material.CLOCK, "&eReplenish Interval Ticks", *replenishTicks(item))
        val replenishVolumeButton = ItemBuilder.makeItem(Material.HOPPER, "&eReplenish Volume", *replenishVolume(item))
        val minimumPriceButton = ItemBuilder.makeItem(Material.RED_CARPET, "&eMinimum Value", *minimumPrice(item))
        val playerImmunityVolumeButton =
            ItemBuilder.makeItem(Material.PLAYER_HEAD, "&ePlayer Immunity Volume", *playerImmunity(item))
        val functionEditorButton =
            ItemBuilder.makeItem(Material.WRITABLE_BOOK, "&eValuation Functions", *functionEditor(item))

        inv.setItem(10, demandButton)
        inv.setItem(11, replenishTicksButton)
        inv.setItem(12, replenishVolumeButton)

        if (item.hasDynamicPricing()) {
            inv.setItem(13, toggleEnabledButton)
        }

        inv.setItem(14, minimumPriceButton)
        inv.setItem(15, playerImmunityVolumeButton)
        inv.setItem(16, functionEditorButton)
    }

    override fun onClick(
        inv: Inventory,
        event: InventoryClickEvent
    ): ViewAction {
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(ItemEditorView(editor))
        return ViewAction.Pass
    }

    private fun toggleEnabled(item: Item): Array<String> {
        return if (item.dynamic != null && item.dynamic.enabled) {
            arrayOf(
                "&9Enabled: &7true",
                "",
                "&7Dynamic pricing is currently enabled for this item.",
                "",
                "&e[Toggle: Click]"
            )
        } else {
            arrayOf(
                "&9Enabled: &7false",
                "",
                "&7Dynamic pricing is currently disabled for this item.",
                "",
                "&e[Toggle: Click]"
            )
        }
    }

    private fun serverDemand(item: Item): Array<String> {
        val alwaysShown = arrayOf(
            "",
            "&7This is the amount the server 'demands' be filled",
            "&7before dynamic pricing kicks in. Until the sold volume",
            "&7exceeds this, the item will sell for the full value.",
            "",
            "&e[Edit: Click]"
        )

        return if (item.dynamic != null && item.dynamic.serverDemand > 0L) {
            arrayOf(
                "&9Server demand: &7${item.dynamic.serverDemand}",
                *alwaysShown,
            )
        } else {
            arrayOf(
                "&cNo server demand set.",
                *alwaysShown,
            )
        }
    }

    private fun replenishTicks(item: Item): Array<String> {
        val alwaysShown = arrayOf(
            "",
            "&7This is the interval (in ticks) for the volume",
            "&7sold to decay over time.",
            "",
            "&e[Edit: Click]"
        )

        return if (item.dynamic != null && item.dynamic.replenishIntervalTicks > 0L) {
            arrayOf(
                "&9Replenish ticks: &7${item.dynamic.replenishIntervalTicks}",
                *alwaysShown,
            )
        } else {
            arrayOf(
                "&cNo replenish interval set.",
                *alwaysShown,
            )
        }
    }

    private fun replenishVolume(item: Item): Array<String> {
        val alwaysShown = arrayOf(
            "",
            "&7Every replenish interval, the sold volume",
            "&7decays by this many units.",
            "",
            "&e[Edit: Click]"
        )

        return if (item.dynamic != null && item.dynamic.replenishIntervalTicks > 0L) {
            arrayOf(
                "&9Replenish volume: &7${item.dynamic.replenishVolume}",
                *alwaysShown,
            )
        } else {
            arrayOf(
                "&cNo replenish volume set.",
                *alwaysShown,
            )
        }
    }

    private fun minimumPrice(item: Item): Array<String> {
        val alwaysShown = arrayOf(
            "",
            "&7This is the minimum price that the item can be worth.",
            "&7Regardless of what the functions evaluate to, this price",
            "&7takes priority.",
            "",
            "&e[Edit: Click]"
        )

        return if (item.dynamic != null && item.dynamic.replenishIntervalTicks > 0L) {
            arrayOf(
                "&9Minimum price: &7$%,.2f".format(item.dynamic.minimumPrice),
                *alwaysShown,
            )
        } else {
            arrayOf(
                "&cNo minimum price set.",
                *alwaysShown,
            )
        }
    }

    private fun playerImmunity(item: Item): Array<String> {
        val alwaysShown = arrayOf(
            "",
            "&7Player immunity volume provides a way for players to",
            "&7bypass dynamic pricing. While their individual sold volume",
            "&7is below this threshold, they will receive the full value",
            "&7of the item regardless of the current effective price.",
            "&7As soon as they surpass this threshold, dynamic pricing",
            "&7will begin for them. This volume decays at the same rate as",
            "&7the server wide volume (via the replenish interval).",
            "",
            "&e[Edit: Click]"
        )

        return if (item.dynamic != null && item.dynamic.replenishIntervalTicks > 0L) {
            arrayOf(
                "&9Player immunity volume: &7${item.dynamic.playerImmunityVolume}",
                *alwaysShown,
            )
        } else {
            arrayOf(
                "&cNo minimum price set.",
                *alwaysShown,
            )
        }
    }

    private fun functionEditor(item: Item): Array<String> {
        val alwaysShown = arrayOf(
            "",
            "&7Functions are evaluated to determine the current price",
            "&7of the item. The following variables are available:",
            "&e • &3price&7, &3P&7: &oBase price of the item",
            "&e • &3serverdemand&7, &3D&7: &oServer demand volume",
            "&e • &3volume&7, &3V&7: &oCurrent server-wide volume",
            "&e • &3minprice&7, &3M&7: &oMinimum price of the item",
            "",
            "&7Attached to each function is a volume range.",
            "&7The volume range specifies the volumes the function",
            "&7will be applicable for. The order of the functions",
            "&7matters. The volume range represents a running sum that",
            "&7starts at server demand up to the end of the first function's.",
            "&7range, then continuing to the next function from the previous",
            "&7range end + 1.",
            "",
            "&e[Edit: Click]"
        )

        return if (item.dynamic != null && item.dynamic.functions.isNotEmpty()) {
            arrayOf(
                "&9Functions: &7${item.dynamic.functions.size} functions",
                *alwaysShown,
            )
        } else {
            arrayOf(
                "&cNo functions defined.",
                *alwaysShown,
            )
        }
    }
}
