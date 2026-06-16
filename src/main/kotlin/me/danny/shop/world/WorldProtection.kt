package me.danny.shop.world

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

interface WorldProtection {
    fun canUseChestAt(player: Player, location: Location): Boolean
}

object ProtectionProviders : WorldProtection {
    private fun pluginEnabled(name: String): Boolean = Bukkit.getPluginManager().getPlugin(name) != null
    private val providers: List<WorldProtection>

    init {
        val ps = mutableListOf<WorldProtection>()
        if (pluginEnabled("WorldGuard")) ps += WorldGuardProtection()
        if (pluginEnabled("GriefPrevention")) ps += GriefPreventionProtection()

        providers = ps
    }

    override fun canUseChestAt(player: Player, location: Location): Boolean =
        player.isOp || providers.all { it.canUseChestAt(player, location) }

}