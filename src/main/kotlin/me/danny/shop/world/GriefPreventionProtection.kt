package me.danny.shop.world

import me.danny.shop.DannyShop
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import org.bukkit.Location
import org.bukkit.entity.Player

class GriefPreventionProtection : WorldProtection {

    init {
        DannyShop.instance().logger.info("GriefPrevention detected: enabling protection module.")
    }

    override fun canUseChestAt(player: Player, location: Location): Boolean {
        val claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, false, null) ?: return true
        return claim.hasExplicitPermission(player, ClaimPermission.Build)
    }
}