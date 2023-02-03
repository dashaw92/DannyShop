package me.danny.shop.economy

import com.earth2me.essentials.*
import me.danny.shop.*
import me.danny.shop.data.*
import me.danny.shop.model.*
import me.danny.shop.model.Item
import me.danny.shop.model.Item.Cost
import me.danny.shop.model.Item.ItemType
import me.danny.shop.model.Item.ItemType.Mat
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.*
import java.util.*
import kotlin.math.*
import net.milkbowl.vault.economy.Economy as VaultEco

internal object Economy {
    private val econ by lazy(::getEconomy)

    fun hasEconomy(): Boolean = Bukkit.getPluginManager().getPlugin("Vault") != null
    private fun getEconomy(): VaultEco {
        if (!hasEconomy()) throw Exception("Attempted to get Vault instance with no Vault plugin active...")
        val rsp = Bukkit.getServicesManager().getRegistration(VaultEco::class.java)
            ?: throw Exception("Failed to load Economy from Vault...")
        return rsp.provider
    }

    private fun buy(id: UUID, amount: Double): Result {
        val player = Bukkit.getPlayer(id)!!
        if (player.hasPermission(Perm.ADMIN)) return Result.BypassesCheck

        val balance = econ.getBalance(player)
        val needed = amount - balance

        if (needed > 0.0) return Result.NotEnoughFunds(needed)

        val resp = econ.withdrawPlayer(player, amount)
        if (resp.transactionSuccess()) return Result.Success
        return Result.UnknownFailure
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
                val candidates = player.inventory.filter { similarCheck.isSimilar(it) }
                if (candidates.isNotEmpty() && candidates.any { 64 - it.amount >= amount }) {
                    return true
                }
            }

            player.pluginMsg("You need $slotsNeeded empty slot(s) to buy this.")
            return false
        }
        return true
    }

    fun purchase(player: Player, id: ID, amount: Int = 1) {
        val item = DannyShop.SHOP.itemByIid(id)!!

        if (CooldownHandler.isOnCooldown(player, id)) {
            when (val expiration = CooldownHandler.getCooldownTime(player, id)) {
                is Expiration.Never -> player.pluginMsg("&cYou cannot purchase this anymore!")
                is Expiration.Future -> {
                    val fullExpiration = expiration.format().take(2).joinToString(" ").trim().ifBlank {
                        "<1s"
                    }
                    player.pluginMsg("You can purchase this again in &o$fullExpiration")
                }

                else -> {}
            }
            return
        }

        val price = when (item.cost) {
            is Cost.Value -> item.cost.buy * amount
            else -> {
                //admin bypass for no price set
                if (player.hasPermission(Perm.ADMIN)) 0.0
                else return
            }
        }

        if (!hasSpace(player, item, amount)) return

        when (val resp = buy(player.uniqueId, price)) {
            is Result.Success, Result.BypassesCheck -> {
                if (resp is Result.Success) {
                    player.pluginMsg("$%,.2f&a taken from your balance.".format(price))
                } else {
                    player.pluginMsg("Price bypassed. No money was taken. &2:)")
                }

                when (item.item) {
                    is Mat -> player.inventory.addItem(ItemStack(item.item.material, amount))
                    is ItemType.Item -> {
                        val stack = item.item.display().clone()
                        stack.amount = amount
                        player.inventory.addItem(stack)
                    }

                    is ItemType.Exp -> {
                        val exp = item.item.exp * amount
                        player.giveExp(exp)
                        player.pluginMsg("%,d&e experience given to you.".format(exp))
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

                CooldownHandler.putOnCooldown(player, id)
            }

            is Result.NotEnoughFunds -> {
                player.pluginMsg("You need &c$%,.2f &7more to purchase this.".format(resp.needed))
            }

            is Result.UnknownFailure -> {
                player.pluginMsg("&cAn error occurred checking your balance. No money was taken from your account.")
            }
        }
    }

    internal fun getWorth(item: ItemStack): Cost {
        val plug = Bukkit.getPluginManager().getPlugin("Essentials") ?: return Cost.NotSet
        val ess = plug as Essentials

        val sell = ess.worth.getPrice(ess, item)?.toDouble() ?: return Cost.NotSet
        val buy = 1.25 * sell
        return Cost.Value(buy)
    }

    private sealed interface Result {
        object BypassesCheck : Result
        object Success : Result
        object UnknownFailure : Result
        data class NotEnoughFunds(val needed: Double) : Result
    }
}