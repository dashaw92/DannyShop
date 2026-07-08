package me.danny.shop.inv.editor.items.properties

import me.danny.shop.inv.editor.items.ItemEditor
import me.danny.shop.inv.editor.items.ItemEditorView
import me.danny.shop.inv.view.MenuView
import me.danny.shop.inv.view.ViewAction
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

        val demandButton = ItemBuilder.makeItem(Material.LAVA_BUCKET, "&eServer Demand")
        val replenishTicksButton = ItemBuilder.makeItem(Material.CLOCK, "&eReplenish Interval Ticks")
        val replenishVolumeButton = ItemBuilder.makeItem(Material.HOPPER, "&eReplenish Volume")
        val minimumPriceButton = ItemBuilder.makeItem(Material.RED_CARPET, "&eMinimum Value")
        val playerImmunityVolumeButton = ItemBuilder.makeItem(Material.PLAYER_HEAD, "&ePlayer Immunity Volume")

        inv.setItem(11, demandButton)
        inv.setItem(12, replenishTicksButton)
        inv.setItem(13, replenishVolumeButton)
        inv.setItem(14, minimumPriceButton)
        inv.setItem(15, playerImmunityVolumeButton)
    }

    override fun onClick(
        inv: Inventory,
        event: InventoryClickEvent
    ): ViewAction {
        if (event.slot == inv.size - 1) return ViewAction.ChangeView(ItemEditorView(editor))
        return ViewAction.Pass
    }

}
