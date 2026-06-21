package me.danny.shop.economy

import me.danny.shop.DannyShop
import me.danny.shop.model.ID
import me.danny.shop.model.Item
import org.bukkit.entity.Player
import java.time.Instant
import java.util.*

class ResetTask(val period: Long) : Runnable {
    companion object {
        internal var nextReset: Long = Instant.now().toEpochMilli()
    }

    override fun run() {
        if (Instant.now().toEpochMilli() > nextReset) {
            nextReset = Instant.now().toEpochMilli() + (period * 50)
            LimitTracking.resetAll()
        }
    }
}

internal object LimitTracking {
    private val M = mutableMapOf<UUID, MutableList<ItemCount>>()

    private fun getCount(pl: Player, id: ID): ItemCount? {
        val item = DannyShop.SHOP.itemByIid(id) ?: return null
        if (item.sellLimit !is Item.SellLimit.Amount) return null
        M.putIfAbsent(pl.uniqueId, mutableListOf())

        val counts = M[pl.uniqueId]!!
        val maybeCount = counts.find { itemCount -> itemCount.id == id }
        if (maybeCount == null) {
            val newCount = ItemCount(id, item.sellLimit.amount.toInt())
            counts.add(newCount)
            return newCount
        }

        return maybeCount
    }

    internal fun remaining(pl: Player, id: ID): Int? {
        return getCount(pl, id)?.count
    }

    internal fun add(pl: Player, id: ID, amount: Int): Int? {
        val canSell = remaining(pl, id) ?: return null
        var willSell = amount
        if (canSell < amount) {
            willSell = canSell
        }

        getCount(pl, id)!!.count = canSell - willSell
        return willSell
    }

    internal fun resetAll() = M.clear()
}

private data class ItemCount(val id: ID, var count: Int)