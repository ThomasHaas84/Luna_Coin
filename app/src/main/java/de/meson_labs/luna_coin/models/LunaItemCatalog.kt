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
            priceCoins = 5,
            iconRes = R.drawable.sunglasses1,
            lunaImageRes = R.drawable.luna_sunglasses1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.SUNGLASSES_2,
            title = "Sonnenbrille 2",
            priceCoins = 5,
            iconRes = R.drawable.sunglasses_2,
            lunaImageRes = R.drawable.luna_sunglasses_2
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.halstuch_1,
            title = "Halstuch (rot)",
            priceCoins = 5,
            iconRes = R.drawable.halstuch_1,
            lunaImageRes = R.drawable.luna_halstuch_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.kappe_1,
            title = "Kappe (rot)",
            priceCoins = 5,
            iconRes = R.drawable.kappe_1,
            lunaImageRes = R.drawable.luna_kappe_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.jacke_1,
            title = "Lederjacke",
            priceCoins = 10,
            iconRes = R.drawable.lederjacke_1,
            lunaImageRes = R.drawable.luna_lederjacke_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.huffel_1,
            title = "Hufflepuff",
            priceCoins = 20,
            iconRes = R.drawable.huffel_1,
            lunaImageRes = R.drawable.luna_huffel_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.chase_1,
            title = "Chase",
            priceCoins = 20,
            iconRes = R.drawable.chase_1,
            lunaImageRes = R.drawable.luna_chase_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.marshall_1,
            title = "Marshall",
            priceCoins = 20,
            iconRes = R.drawable.marshall_1,
            lunaImageRes = R.drawable.luna_marshall_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.ballett_1,
            title = "Ballett",
            priceCoins = 20,
            iconRes = R.drawable.ballett_1,
            lunaImageRes = R.drawable.luna_ballett_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.lunacraft_1,
            title = "Lunacraft",
            priceCoins = 20,
            iconRes = R.drawable.lunacraft_1,
            lunaImageRes = R.drawable.luna_lunacraft_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.pirat_1,
            title = "Pirat",
            priceCoins = 20,
            iconRes = R.drawable.pirat_1,
            lunaImageRes = R.drawable.luna_pirat_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.cowboy_1,
            title = "Cowboy",
            priceCoins = 20,
            iconRes = R.drawable.cowboy_1,
            lunaImageRes = R.drawable.luna_cowboy_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.knight_1,
            title = "Ritter",
            priceCoins = 20,
            iconRes = R.drawable.knight_1,
            lunaImageRes = R.drawable.luna_knight_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.diabetis_1,
            title = "Captain Diabetis",
            priceCoins = 20,
            iconRes = R.drawable.diabetis_1,
            lunaImageRes = R.drawable.luna_diabetis_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.flash_1,
            title = "Flash",
            priceCoins = 100,
            iconRes = R.drawable.flash_1,
            lunaImageRes = R.drawable.luna_flash_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.iron_1,
            title = "Iron Man",
            priceCoins = 100,
            iconRes = R.drawable.iron_1,
            lunaImageRes = R.drawable.luna_iron_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.gandalf_1,
            title = "Gandalf",
            priceCoins = 100,
            iconRes = R.drawable.gandalf_1,
            lunaImageRes = R.drawable.luna_gandalf_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.slytherin,
            title = "Slytherin",
            priceCoins = 100,
            iconRes = R.drawable.harry_potter,
            lunaImageRes = R.drawable.slyth_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.ravenclaw,
            title = "Ravenclaw",
            priceCoins = 100,
            iconRes = R.drawable.harry_potter,
            lunaImageRes = R.drawable.raven_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.griffindor,
            title = "Griffindor",
            priceCoins = 100,
            iconRes = R.drawable.harry_potter,
            lunaImageRes = R.drawable.griff_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.warhammer_1,
            title = "Kettenschwert",
            priceCoins = 100,
            iconRes = R.drawable.warhammer_1,
            lunaImageRes = R.drawable.luna_warhammer_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.tau_1,
            title = "Tau",
            priceCoins = 100,
            iconRes = R.drawable.tau_1,
            lunaImageRes = R.drawable.luna_tau_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.ork_1,
            title = "Ork",
            priceCoins = 100,
            iconRes = R.drawable.ork_1,
            lunaImageRes = R.drawable.luna_ork_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.dark_knight_1,
            title = "Dark Knight",
            priceCoins = 100,
            iconRes = R.drawable.dark_knight_1,
            lunaImageRes = R.drawable.luna_dark_knight_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.batman_1,
            title = "Batman",
            priceCoins = 100,
            iconRes = R.drawable.batman_1,
            lunaImageRes = R.drawable.luna_batman_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.gow_1,
            title = "Kratos",
            priceCoins = 100,
            iconRes = R.drawable.kratos_1,
            lunaImageRes = R.drawable.kratos_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.gow_2,
            title = "Kratos 2",
            priceCoins = 100,
            iconRes = R.drawable.kratos_2,
            lunaImageRes = R.drawable.kratos_2
        ),





        )

    fun getDefinition(item: LunaInventoryItem): LunaItemDefinition {
        return allItems.first { it.item == item }
    }
}