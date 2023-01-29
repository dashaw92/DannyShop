package me.danny.shop.inv

object LoreList {
    /**
     * Function for generating lists that represent
     * options.
     *
     * ```
     * enum class State { None, Infinite, Timed }
     *
     * LoreList.makeList(listOf(
     *      State.None toEntry listOf("Players do not have to wait", "to purchase this item again"),
     *      State.Infinite toEntry listOf("Players can only purchase this", "item one time."),
     *      State.Timed toEntry listOf("Players have to wait to purchase", "this item again.")
     *  ), State.None)
     * ```
     * Becomes:
     * ```
     * [
     *   "• None",
     *   "• Infinite",
     *   "• Timed",
     *   "",
     *   "Players do not have to wait",
     *   "to purchase this item again"
     * ]
     * ```
     * with the selected option highlighted with coloring.
     *
     * Designed specifically with enum classes in mind, but can be used with raw Strings as well.
     */
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

    /**
     * Represents a list option with the associated description.
     * The description will be displayed if the option is selected.
     */
    data class ListEntry<T>(val option: T, val description: List<String>)

    /**
     * Convenience for constructing [ListEntry] objects
     */
    infix fun <A> A.toEntry(description: List<String>): ListEntry<A> = ListEntry(this, description)
}