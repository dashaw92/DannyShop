package me.danny.shop.inv

object LoreList {
    fun <T> makeList(options: List<ListEntry<T>>, selected: T): Array<out String> {
        val lore = mutableListOf<String>()
        var description: List<String> = listOf()
        options.map { (option, desc) ->
            if (selected == option) {
                description = desc
                "&2• &n$option"
            } else "&7• $option"
        }.forEach(lore::add)
        lore += ""
        description.map { "&e$it" }.forEach(lore::add)
        return lore.toTypedArray()
    }

    data class ListEntry<T>(val option: T, val description: List<String>)

    internal infix fun <A> A.toEntry(description: List<String>): ListEntry<A> = ListEntry(this, description)
}