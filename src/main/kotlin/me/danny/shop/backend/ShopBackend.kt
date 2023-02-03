package me.danny.shop.backend

import me.danny.shop.model.*
import org.bukkit.plugin.*

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