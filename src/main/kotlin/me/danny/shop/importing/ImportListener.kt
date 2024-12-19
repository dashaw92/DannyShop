package me.danny.shop.importing

import me.danny.shop.Perm
import me.danny.shop.commands.ImportCommand
import me.danny.shop.economy.Economy.getWorth
import me.danny.shop.model.Category
import me.danny.shop.model.ID
import me.danny.shop.model.Item
import me.danny.shop.model.Shop
import me.danny.shop.pluginMsg
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Listens for players punching chests with the import wand
 * When this occurs, attempt to import the items in the chest
 * into the shop
 */
internal object ImportListener : Listener {

    private val chests = object : Tag<Material> {
        val materials = mutableSetOf(Material.CHEST, Material.TRAPPED_CHEST)

        @Suppress("DEPRECATION")
        override fun getKey(): NamespacedKey = NamespacedKey("dannyshop", "chest_tag")
        override fun getValues(): MutableSet<Material> = materials
        override fun isTagged(item: Material): Boolean = materials.contains(item)
    }

    @EventHandler
    fun onPlayerPunch(event: PlayerInteractEvent) {
        val player = event.player

        if (!event.hasItem() || !ImportCommand.isWand(event.item!!) || !player.hasPermission(Perm.ADMIN)) return
        if (event.action == Action.RIGHT_CLICK_BLOCK && chests.isTagged(event.clickedBlock!!.type)) return
        event.isCancelled = true
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        if (!chests.isTagged(event.clickedBlock!!.type)) return

        val state = event.clickedBlock!!.state as Chest
        if (state.customName == null || state.inventory.isEmpty) return
        val name = state.customName!!
        if (Shop.findCategoryByName(name) == null) {
            Shop.addCategory(Category(name, Material.CHEST))
            player.pluginMsg("Created category &e$name&7.")
        }

        val category = Shop.findCategoryByName(name)!!

        player.pluginMsg("Importing items from chest into category &e${category.name}&7.")

        if (ImportSession.isInSession(player.uniqueId)) {
            val session = ImportSession.getSession(player.uniqueId)!!
            if (session.categoryID() == category.cid) {
                player.pluginMsg("You are already importing into this category. Re-opening editor.")
                session.openEditor()
                return
            }

            player.pluginMsg("Cleaning up previous session. Items from previous session will not be imported.")
//            session.importAll()
            ImportSession.delete(player.uniqueId)
        }
        importItems(player, category, state.inventory)
    }

    private fun importItems(player: Player, category: Category, inv: Inventory) {
        val importItems = inv.asSequence()
            .filterNotNull()
            .filterNot { it.type.isAir }
            .map { item ->
                val iid = ID.generate()
                val type = itemType(item)

                val cost = when (type) {
                    is Item.ItemType.Mat, is Item.ItemType.Item -> getWorth(item)
                    else -> Item.Cost.NotSet
                }

                val quantities = when (type) {
                    is Item.ItemType.Mat -> Item.Quantities(listOf(1, 32, 64), Item.Quantities.Allowed.Any)
                    else -> Item.Quantities(listOf(1), Item.Quantities.Allowed.Predefined)
                }

                val cooldown = Item.Cooldown.None

                ImportedItem(iid, type, null, cooldown, cost, quantities)
            }.toMutableList()
        //inv.clear()

        val session = ImportSession(player, category, importItems)
        session.openEditor()
    }

    @EventHandler
    fun onWandDrop(event: PlayerDropItemEvent) {
        if (ImportCommand.isWand(event.itemDrop.itemStack)) {
            event.player.pluginMsg("Removed import wand!")
            event.itemDrop.remove()
        }
    }

    private fun itemType(item: ItemStack): Item.ItemType {
        //In general, items with ItemMeta attached are custom items (ItemType.Item)
        //wew, that's a lot of "item" for one sentence 😅
        if (item.hasItemMeta()) {
            //We need to handle the custom types (experience and commands) first
            //All custom items are defined by their name, so..:
            if (item.itemMeta!!.hasDisplayName()) {
                val name = item.itemMeta!!.displayName
                when (item.type) {
                    //These checks are intentionally fallthrough

                    //Exp items are experience bottles with the name "Exp <amount>"
                    Material.EXPERIENCE_BOTTLE -> {
                        if (name.startsWith("Exp ")) {
                            val amount = name.takeLastWhile { it.isDigit() }.toIntOrNull()
                            if (amount != null) {
                                return Item.ItemType.Exp(amount)
                            }
                        }
                    }

                    //Command items are command blocks with the name "/<command>"
                    Material.COMMAND_BLOCK -> {
                        if (name.startsWith('/')) {
                            return Item.ItemType.Command(name)
                        }
                    }

                    else -> {}
                }
            }
            //It's none of the special item types, so now it's a custom item
            return Item.ItemType.Item(item)
        } else {
            //Raw materials
            return Item.ItemType.Mat(item.type)
        }
    }
}