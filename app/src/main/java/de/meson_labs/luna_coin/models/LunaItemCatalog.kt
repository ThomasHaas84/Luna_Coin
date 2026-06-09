package de.meson_labs.luna_coin.models

import androidx.annotation.DrawableRes
import de.meson_labs.luna_coin.R

data class LunaItemDefinition(
    val item: LunaInventoryItem,
    val title: String,
    val priceCoins: Int,
    @DrawableRes val iconRes: Int,
    @DrawableRes val lunaImageRes: Int
)

object LunaItemCatalog {

    val allItems = listOf(
        LunaItemDefinition(
            item = LunaInventoryItem.SUNGLASSES_1,
            title = "Sonnenbrille",
            priceCoins = 10,
            iconRes = R.drawable.sunglasses1,
            lunaImageRes = R.drawable.luna_sunglasses1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.SUNGLASSES_2,
            title = "Sonnenbrille 2",
            priceCoins = 10,
            iconRes = R.drawable.sunglasses_2,
            lunaImageRes = R.drawable.luna_sunglasses_2
        ),


    )

    fun getDefinition(item: LunaInventoryItem): LunaItemDefinition {
        return allItems.first { it.item == item }
    }
}