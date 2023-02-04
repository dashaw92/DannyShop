package me.danny.shop.inv

internal class LoreField(
    private val header: String = "&6┌┤&4 DannyShop &6├──",
    private val footer: String = "&6└───────────",
    private val perFieldPrefix: String = "&6| "
) {
    private var fields: MutableList<String> = mutableListOf()

    fun add(field: String) {
        fields += field
    }

    fun addAll(listFields: Collection<String>) {
        fields.addAll(listFields)
    }

    fun build(): Array<out String> {
        val display: MutableList<String> = fields.map { field -> "$perFieldPrefix $field" }.toMutableList()
        display.add(0, header)
        display.add(footer)
        return display.toTypedArray()
    }
}