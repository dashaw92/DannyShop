package me.danny.shop

import me.danny.shop.backend.*
import me.danny.shop.commands.*
import me.danny.shop.data.*
import me.danny.shop.economy.*
import me.danny.shop.inv.*
import me.danny.shop.inv.listeners.*
import me.danny.shop.listeners.*
import me.danny.shop.model.*
import org.bukkit.*
import org.bukkit.plugin.java.*

/**
 * The one-stop shop plugin
 * ( pun intended ;) )
 */
class DannyShop : JavaPlugin() {
    companion object {
        /**
         * The main shop of the plugin
         */
        internal lateinit var SHOP: Shop
            private set

        /**
         * Retrieve the active instance of the plugin
         */
        fun instance(): DannyShop = Bukkit.getPluginManager().getPlugin("DannyShop") as DannyShop
    }

    private val backend = BackendManager.loadDefaultProvider(this)

    override fun onEnable() {
        if (!Economy.hasEconomy()) {
            logger.warning("Vault not found!")
            logger.warning("The shop may be edited, but no items may be purchased!")
        }

        SHOP = when (val result = backend.loadShop(this)) {
            is LoadResult.Failure -> {
                logger.severe("Failed to load shop from backend (${backend.name()})")
                logger.severe("Stack trace:")
                result.reason.printStackTrace()
                logger.severe("Disabling plugin. If you believe this to be a bug, please file an issue.")
                isEnabled = false
                return
            }

            is LoadResult.Success -> result.shop
        }

        Bukkit.getPluginManager().registerEvents(MenuListener, this)
        Bukkit.getPluginManager().registerEvents(ImportListener, this)
        Bukkit.getPluginManager().registerEvents(CooldownListener, this)
        getCommand("dannyshop")!!.setExecutor(ShopCommand)
        getCommand("migrate")!!.setExecutor(MigrateCommand)
//        getCommand("dannytest")!!.setExecutor(this)
    }

    override fun onDisable() {
        CooldownHandler.saveAll()
        backend.saveShop(this, SHOP)
        Menu.closeOpenInvs()
    }

//    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
//        if (label != "dannytest") return true
//
//        val pl = sender as Player
//        CooldownHandler.wipeCooldowns(pl)
//        return true
//    }
}