package me.danny.shop.data

import me.danny.shop.*
import me.danny.shop.data.Item.Cooldown
import me.danny.shop.data.Item.Cooldown.Time
import me.danny.shop.data.Item.Cooldown.Time.Companion.Units
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.*
import org.bukkit.event.player.*
import org.bukkit.persistence.*
import java.time.*
import java.util.*

private val cd_key = Key("cooldowns", IDContainerType)
private val cache: MutableMap<UUID, IDContainer> = mutableMapOf()

internal object CooldownHandler {
    /**
     * Called from onDisable
     */
    fun saveAll() {
        cache.forEach { (uuid, container) ->
            val pl = Bukkit.getPlayer(uuid) ?: return@forEach
            pl.updateCooldowns(container)
        }
        cache.clear()
    }

    fun putOnCooldown(player: Player, id: ID, cooldown: Cooldown) {
        val container = player.cooldowns()
        container.setCooldown(id, cooldown)
        player.updateCooldowns(container)
    }

    fun getCooldownTime(player: Player, id: ID): Expiration {
        val container = player.cooldowns()

        if (!container.isOnCooldown(id)) return Expiration.None

        val timestamp = container[id]!!
        if (timestamp == Long.MIN_VALUE) return Expiration.Never
        return Expiration.Future(id, timestamp)
    }

    fun isOnCooldown(player: Player, id: ID): Boolean =
        player.cooldowns().isOnCooldown(id) && !player.hasPermission("dannyshop.admin")
}

internal sealed interface Expiration {
    object Never : Expiration {
        override fun toString(): String = "Never"
    }

    object None : Expiration {
        override fun toString(): String = "No cooldown"
    }

    data class Future(private val id: ID, private val time: Long) : Expiration {
        /**
         * Build a user-facing expiration date for the cooldown
         */
        fun format(): List<String> {
            val now = Instant.now().toEpochMilli()

            val cooldown = DannyShop.SHOP.itemByIid(id)!!.cooldown as Cooldown.Duration
            val timeSpan = cooldown.time.time * cooldown.time.multiplier()

            var rem = now - (timeSpan + time)

            //Descending entries of time units
            //Each Pair#second is that unit in milliseconds
            val units = listOf(
                "y" to Time.multiplier(Units.Y),
                "mo" to Time.multiplier(Units.Mo),
                "w" to Time.multiplier(Units.W),
                "d" to Time.multiplier(Units.D),
                "h" to Time.multiplier(Units.H),
                "m" to Time.multiplier(Units.M),
                "s" to Time.multiplier(Units.S),
            )

            //Work from greatest units down to milliseconds,
            //collecting whole units and carrying over the remainder
            //for the next unit.
            //The last entry is milliseconds, which will always
            //take whatever is left (1L).
            val remainders = units.map { (suffix, unit) ->
                val remUnit = rem / unit
                rem -= remUnit * unit
                remUnit to suffix
            }

            //Finally, transform the remainders map
            return remainders
                //Since it's entirely possible a unit is too big for the input,
                //skip until we reach the first non-zero remainder
                .dropWhile { (unit, _) -> unit == 0L }
                //output: [1y 0mo 0w 10d 9h 0m 59s 935ms]
                //users can then do whatever they see fit with this
                .map { (unit, suffix) -> "$unit$suffix" }
        }
    }
}

private object IDContainerType : PersistentDataType<ByteArray, IDContainer> {
    override fun getPrimitiveType() = ByteArray::class.java
    override fun getComplexType() = IDContainer::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): IDContainer {
        val str = String(primitive)

        val map = str.split(':').associate {
            val split = it.split('=')
            val id = ID(split[0])
            val then = split[1].toLongOrNull() ?: 0
            id to then
        }.toMutableMap()

        return IDContainer(map)
    }

    override fun toPrimitive(complex: IDContainer, context: PersistentDataAdapterContext): ByteArray {
        val entries: MutableList<String> = mutableListOf()

        complex.entries.forEach { (id, then) ->
            entries += "${id.id}=$then"
        }

        val joined = entries.joinToString(":")
        return joined.toByteArray()
    }

}

internal data class IDContainer(val ids: MutableMap<ID, Long>) : Map<ID, Long> by ids {
    companion object {
        fun new(): IDContainer = IDContainer(mutableMapOf())
    }

    fun resetCooldown(id: ID) {
        ids[id] = 0
    }

    fun resetAll() {
        ids.clear()
    }

    fun setCooldown(id: ID, cooldown: Cooldown) {
        when (cooldown) {
            is Cooldown.None -> return
            is Cooldown.Infinite -> {
                ids[id] = Long.MIN_VALUE
                return
            }

            //stored timestamps are "last purchased at"
            //The cooldowns are done this way so if an item's
            //cooldown is lowered/increased down the line,
            //this will gracefully update the expiration,
            //as that's not calculated until the cooldown
            //is checked on purchase
            is Cooldown.Duration -> {
                ids[id] = Instant.now().toEpochMilli()
            }
        }
    }

    fun isOnCooldown(id: ID): Boolean {

        val item = DannyShop.SHOP.itemByIid(id) ?: return false
        return when (item.cooldown) {
            is Cooldown.None -> false
            is Cooldown.Infinite -> ids[id] == Long.MIN_VALUE
            is Cooldown.Duration -> {
                val then = ids[id] ?: return false
                val amount = item.cooldown.time.time
                val output = item.cooldown.time.multiplier() * amount
                then + output > Instant.now().toEpochMilli()
            }
        }
    }
}

internal fun Player.cooldowns(): IDContainer {
    if (cache[uniqueId] != null) return cache[uniqueId]!!
    val container = persistentDataContainer.getOrDefault(cd_key.key, cd_key.type, IDContainer.new())
    cache[uniqueId] = container
    return container
}

private fun Player.updateCooldowns(container: IDContainer) {
    persistentDataContainer.set(cd_key.key, cd_key.type, container)
    cache[uniqueId] = container
}

object CooldownListener : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        val container = cache[uuid] ?: return
        event.player.updateCooldowns(container)
        cache -= uuid
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId
        val container = event.player.cooldowns()
        cache[uuid] = container
    }
}