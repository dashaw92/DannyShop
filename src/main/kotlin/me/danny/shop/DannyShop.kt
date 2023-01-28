package me.danny.shop

import me.danny.shop.commands.*
import me.danny.shop.data.*
import me.danny.shop.economy.*
import me.danny.shop.inv.*
import me.danny.shop.inv.listeners.*
import me.danny.shop.listeners.*
import org.bukkit.*
import org.bukkit.plugin.java.*

/**
 * The one-stop shop plugin
 * ( pun intended ;) )
 */
class DannyShop : JavaPlugin() {
    companion object {
        lateinit var SHOP: Shop
            private set

        fun instance(): DannyShop = Bukkit.getPluginManager().getPlugin("DannyShop") as DannyShop
        fun hasInputAPI(): Boolean = Bukkit.getPluginManager().isPluginEnabled("InputAPI")
    }

    override fun onEnable() {
        if (!Economy.hasEconomy()) {
            logger.warning("Vault not found!")
            logger.warning("The shop may be edited, but no items may be purchased!")
        }

        SHOP = DannyShopLoadables.loadShop(this)
        Bukkit.getPluginManager().registerEvents(MenuListener, this)
        Bukkit.getPluginManager().registerEvents(ImportListener, this)
        getCommand("dannyshop")!!.setExecutor(ShopCommand)
    }

    override fun onDisable() {
        DannyShopLoadables.saveShop(SHOP)
        Menu.closeOpenInvs()
    }
}