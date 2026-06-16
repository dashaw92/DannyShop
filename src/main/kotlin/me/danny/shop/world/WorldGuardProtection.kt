package me.danny.shop.world

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import me.danny.shop.DannyShop
import org.bukkit.Location
import org.bukkit.entity.Player

class WorldGuardProtection : WorldProtection {

    init {
        DannyShop.instance().logger.info("WorldGuard detected: enabling protection module.")
    }

    override fun canUseChestAt(player: Player, location: Location): Boolean {
        val wgLoc = BukkitAdapter.adapt(location)
        val wgPl = WorldGuardPlugin.inst().wrapPlayer(player)

        val container = WorldGuard.getInstance().platform.regionContainer
        val query = container.createQuery()
        val regions = query.getApplicableRegions(wgLoc)
        val use = regions.testState(wgPl, Flags.CHEST_ACCESS)
        return regions.size() == 0 || use
    }
}