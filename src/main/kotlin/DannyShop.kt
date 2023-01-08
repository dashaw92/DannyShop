package me.danny.shop

import ShopCommand
import me.danny.shop.data.DannyShopLoadables
import me.danny.shop.data.Shop
import me.danny.shop.inv.ImportListener
import me.danny.shop.inv.Menu
import me.danny.shop.inv.MenuListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

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