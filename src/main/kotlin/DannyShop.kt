package me.danny.shop

import me.danny.shop.commands.ShopCommand
import me.danny.shop.data.DannyShopLoadables
import me.danny.shop.data.Shop
import me.danny.shop.inv.Menu
import me.danny.shop.inv.listeners.MenuListener
import me.danny.shop.listeners.ImportListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * The one-stop shop plugin
 * ( pun intended ;) )
 */
class DannyShop : JavaPlugin() {
    companion object {
        lateinit var SHOP: Shop
            private set
    }

    override fun onEnable() {
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