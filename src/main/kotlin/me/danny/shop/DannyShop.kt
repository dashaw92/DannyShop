package me.danny.shop

import me.danny.shop.backend.BackendManager
import me.danny.shop.backend.LoadResult
import me.danny.shop.commands.SellCommand
import me.danny.shop.commands.SellWandListener
import me.danny.shop.commands.ShopCommand
import me.danny.shop.config.Config
import me.danny.shop.economy.Economy
import me.danny.shop.economy.ResetTask
import me.danny.shop.importing.ImportListener
import me.danny.shop.input.ChatInput
import me.danny.shop.inv.Menu
import me.danny.shop.inv.listeners.MenuListener
import me.danny.shop.model.Shop
import me.danny.shop.tracking.Analytics
import me.danny.shop.tracking.Logging
import me.danny.shop.tracking.getAnalytics
import me.danny.shop.tracking.getLogging
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
    internal lateinit var config: Config
    internal lateinit var ecolog: Logging
    internal lateinit var analytics: Analytics
    private var sellLimitResetTaskHandle: Int = -1

    override fun onEnable() {
        if (!Economy.hasEconomy()) {
            logger.warning("Vault not found!")
            logger.warning("The shop may be edited, but no items may be sold!")
        }

        config = BackendManager.getConfig(this)
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

        analytics = getAnalytics(config)
        analytics.load()

        ecolog = getLogging(config)
        ecolog.load()

        Menu.scheduleRefreshTask()

        Bukkit.getPluginManager().registerEvents(MenuListener, this)
        Bukkit.getPluginManager().registerEvents(ImportListener, this)
        Bukkit.getPluginManager().registerEvents(SellWandListener, this)
        Bukkit.getPluginManager().registerEvents(ChatInput.ChatInputListener, this)
        getCommand("dannyshop")!!.setExecutor(ShopCommand)
        getCommand("sell")!!.setExecutor(SellCommand)
        startSellLimitResetTask()
        startAutosaveTask()
    }

    override fun onDisable() {
        saveAll()
    }

    internal fun saveAll() {
        analytics.save()
        ecolog.save()
        try {
            backend.saveShop(this, SHOP)
            Menu.closeOpenInvs()
        } catch (_: NoClassDefFoundError) {
        }
    }

    internal fun startAutosaveTask() {
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(this, {
                saveAll()
                Bukkit.getConsoleSender().pluginMsg("&3[AutoSave] &7All data saved.")
            }, 20L * 60L, 20L * 60L * 5L)
    }

    internal fun startSellLimitResetTask() {
        if (sellLimitResetTaskHandle != -1) {
            Bukkit.getScheduler().cancelTask(sellLimitResetTaskHandle)
        }

        sellLimitResetTaskHandle = Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(this, ResetTask(config.sellLimitRefreshTicks), 0L, 20L)
    }
}

internal fun CommandSender.pluginMsg(msg: String) {
    sendMessage("&6[DannyShop]&7 $msg".color())
}