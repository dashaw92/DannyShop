package me.danny.shop

import me.danny.shop.backend.BackendManager
import me.danny.shop.backend.LoadResult
import me.danny.shop.commands.ShopCommand
import me.danny.shop.economy.Economy
import me.danny.shop.importing.ImportListener
import me.danny.shop.input.ChatInput
import me.danny.shop.inv.Menu
import me.danny.shop.inv.listeners.MenuListener
import me.danny.shop.model.Shop
import me.danny.shop.utils.color
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

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

    internal var backend = BackendManager.loadDefaultProvider(this)

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

        Menu.scheduleRefreshTask()

        Bukkit.getPluginManager().registerEvents(MenuListener, this)
        Bukkit.getPluginManager().registerEvents(ImportListener, this)
        Bukkit.getPluginManager().registerEvents(ChatInput.ChatInputListener, this);
        getCommand("dannyshop")!!.setExecutor(ShopCommand)
    }

    override fun onDisable() {
        try {
            backend.saveShop(this, SHOP)
            Menu.closeOpenInvs()
        } catch (ignored: NoClassDefFoundError) {
        }
    }
}

internal fun CommandSender.pluginMsg(msg: String) {
    sendMessage("&6[DannyShop]&7 $msg".color())
}