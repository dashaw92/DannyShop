package me.danny.shop.backend

import me.danny.shop.model.Shop
import org.bukkit.plugin.Plugin

internal sealed interface ShopBackend {
    fun name(): String
    fun type(): BackendType
    fun loadShop(plugin: Plugin): LoadResult
    fun saveShop(plugin: Plugin, shop: Shop)
}

internal sealed interface LoadResult {
    data class Failure(val reason: Exception) : LoadResult
    data class Success(val shop: Shop) : LoadResult
}