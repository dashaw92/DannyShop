package me.danny.shop.inv.shop.items

/**
 * Controls what type of items are included in the item page
 */
internal enum class FilterType {
    /**
     * Everything is displayed
     */
    All,

    /**
     * Only raw materials
     */
    Materials,

    /**
     * Only custom items
     */
    Items,

    /**
     * Only commands
     */
    Commands,

    /**
     * Only experience items
     */
    Experience
}