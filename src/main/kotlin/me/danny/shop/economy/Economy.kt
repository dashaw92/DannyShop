package me.danny.shop.economy

import com.earth2me.essentials.*
import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.data.Item
import me.danny.shop.data.Item.Cost
import me.danny.shop.data.Item.ItemType
import me.danny.shop.data.Item.ItemType.Mat
import me.danny.shop.me.danny.shop.inv.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.*
import java.util.*
import kotlin.math.*
import net.milkbowl.vault.economy.Economy as VaultEco

object Economy {
    private val econ by lazy(::getEconomy)

    fun hasEconomy(): Boolean = Bukkit.getPluginManager().getPlugin("Vault") != null
    private fun getEconomy(): VaultEco {
        if (!hasEconomy()) throw Exception("Attempted to get Vault instance with no Vault plugin active...")
        val rsp = Bukkit.getServicesManager().getRegistration(VaultEco::class.java)
            ?: throw Exception("Failed to load Economy from Vault...")
        return rsp.provider
    }

    private fun buy(id: UUID, amount: Double): Result {
        val player = Bukkit.getPlayer(id)

        val balance = econ.getBalance(player)
        val needed = amount - balance

        if (needed > 0.0) return Result.NotEnoughFunds(needed)

        val resp = econ.withdrawPlayer(player, amount)
        if (resp.transactionSuccess()) return Result.Success
        return Result.UnknownFailure
    }

    private fun sell(id: UUID, amount: Double) {
        if (amount <= 0.0) return

        val player = Bukkit.getPlayer(id)
        econ.depositPlayer(player, amount)
    }

    private fun hasSpace(player: Player, item: Item, amount: Int): Boolean {
        val similarCheck = when (item.item) {
            is Mat -> ItemStack(item.item.material, amount)
            is ItemType.Item -> {
                val stack = item.item.display().clone()
                stack.amount = amount
                stack
            }

            else -> return true
        }

        val slotsNeeded = ceil(amount / 64.0).toInt()
        val empty = player.inventory.storageContents.count { it == null || it.type.isAir }
        if (empty < slotsNeeded) {
            if (amount < 64) {
                val first = player.inventory.find { similarCheck.isSimilar(it) }
                if (first != null && 64 - first.amount >= amount) {
                    return true
                }
            }

            player.sendMessage("&6[DannyShop] &7You need $slotsNeeded empty slot(s) to buy this.".color())
            return false
        }
        return true
    }

    fun purchase(player: Player, id: ID, amount: Int = 1) {
        val item = DannyShop.SHOP.itemByIid(id)!!
        if (item.cost !is Cost.Value) return
        val price = item.cost.buy * amount

        if (!hasSpace(player, item, amount)) return

        when (val resp = buy(player.uniqueId, price)) {
            is Result.Success -> {
                player.sendMessage("&6[DannyShop] &7$%,.2f&a taken from your balance.".format(price).color())
                when (item.item) {
                    is Mat -> player.inventory.addItem(ItemStack(item.item.material, amount))
                    is ItemType.Item -> {
                        val stack = item.item.display().clone()
                        stack.amount = amount
                        player.inventory.addItem(stack)
                    }

                    is ItemType.Exp -> {
                        val exp = (item.item.exp * amount).roundToInt()
                        player.giveExp(exp)
                        player.sendMessage("&6[DannyShop] &7%,d&e experience given to you.".format(exp).color())
                    }

                    is ItemType.Command -> {
                        var cmd = item.item.command
                            .replace("\$PLAYER", player.name)
                            .replace("\$UUID", player.uniqueId.toString())
                            .replace("\$UUID_NO_DASHES", player.uniqueId.toString().replace("-", ""))
                        if (cmd.startsWith('/')) cmd = cmd.substring(1)

                        (0 until amount).forEach { _ ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
                        }
                    }
                }
            }

            is Result.NotEnoughFunds -> {
                player.sendMessage(
                    "&6[DannyShop] &eYou need &7$%,.2f &emore to purchase this.".format(resp.needed).color()
                )
            }

            is Result.UnknownFailure -> {
                player.sendMessage("&6[DannyShop] &cAn error occurred checking your balance. No money was taken from your account.".color())
            }
        }
    }

    internal fun getWorth(item: ItemStack): Cost {
        val plug = Bukkit.getPluginManager().getPlugin("Essentials") ?: return Cost.NotSet
        val ess = plug as Essentials

        val sell = ess.worth.getPrice(ess, item)?.toDouble() ?: return Cost.NotSet
        val buy = 1.25 * sell
        return Cost.Value(buy, sell)
    }

    private sealed interface Result {
        object Success : Result
        object UnknownFailure : Result
        data class NotEnoughFunds(val needed: Double) : Result
    }
}