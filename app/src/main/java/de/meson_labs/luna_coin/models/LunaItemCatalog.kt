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
            item = LunaInventoryItem.plume_1,
            title = "Plume-Halsband",
            priceCoins = 5,
            iconRes = R.drawable.plume_1,
            lunaImageRes = R.drawable.plume_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.wurst_1,
            title = "Wurst",
            priceCoins = 5,
            iconRes = R.drawable.wurst_1,
            lunaImageRes = R.drawable.wurst_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.schmucker_1,
            title = "Schmucker",
            priceCoins = 5,
            iconRes = R.drawable.schmucker_1,
            lunaImageRes = R.drawable.schmucker_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.shit_1,
            title = "Kack-Wurst",
            priceCoins = 5,
            iconRes = R.drawable.shit_1,
            lunaImageRes = R.drawable.shit_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.jacke_1,
            title = "Lederjacke",
            priceCoins = 10,
            iconRes = R.drawable.lederjacke_1,
            lunaImageRes = R.drawable.luna_lederjacke_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.judo_1,
            title = "Judo",
            priceCoins = 10,
            iconRes = R.drawable.judo_1,
            lunaImageRes = R.drawable.judo_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.kleid_1,
            title = "Sommerkleid",
            priceCoins = 10,
            iconRes = R.drawable.kleid_1,
            lunaImageRes = R.drawable.kleid_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.talahoon_1,
            title = "Talahoon",
            priceCoins = 10,
            iconRes = R.drawable.talahoon_1,
            lunaImageRes = R.drawable.talahoon_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.engel_1,
            title = "Engel",
            priceCoins = 10,
            iconRes = R.drawable.engel_1,
            lunaImageRes = R.drawable.engel_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.hotdog_1,
            title = "Hotdog",
            priceCoins = 10,
            iconRes = R.drawable.hotdog_1,
            lunaImageRes = R.drawable.hotdog_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.deutschland_1,
            title = "Deutschland",
            priceCoins = 10,
            iconRes = R.drawable.deutschland_1,
            lunaImageRes = R.drawable.deutschland_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.niederlande_1,
            title = "Niederlande",
            priceCoins = 10,
            iconRes = R.drawable.niederlande_1,
            lunaImageRes = R.drawable.niederlande_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.portugal_1,
            title = "Portugal",
            priceCoins = 10,
            iconRes = R.drawable.portugal_1,
            lunaImageRes = R.drawable.portugal_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.hase_1,
            title = "Hase",
            priceCoins = 20,
            iconRes = R.drawable.hase_1,
            lunaImageRes = R.drawable.hase_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.rochen_1,
            title = "Rochen",
            priceCoins = 20,
            iconRes = R.drawable.rochen_1,
            lunaImageRes = R.drawable.rochen_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.esel_1,
            title = "Esel",
            priceCoins = 20,
            iconRes = R.drawable.esel_1,
            lunaImageRes = R.drawable.esel_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.fledermaus_1,
            title = "Fledermaus",
            priceCoins = 20,
            iconRes = R.drawable.fledermaus_1,
            lunaImageRes = R.drawable.fledermaus_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.huffel_1,
            title = "Hufflepuff",
            priceCoins = 20,
            iconRes = R.drawable.huffel_1,
            lunaImageRes = R.drawable.luna_huffel_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.toolshed_1,
            title = "Toolshed",
            priceCoins = 20,
            iconRes = R.drawable.toolshed_1,
            lunaImageRes = R.drawable.toolshed_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.chase_1,
            title = "Chase",
            priceCoins = 20,
            iconRes = R.drawable.chase_1,
            lunaImageRes = R.drawable.luna_chase_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.zuma_1,
            title = "Zuma",
            priceCoins = 20,
            iconRes = R.drawable.zuma_1,
            lunaImageRes = R.drawable.zuma_1
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
            item = LunaInventoryItem.unicorn_1,
            title = "Unicorn",
            priceCoins = 20,
            iconRes = R.drawable.unicorn_1,
            lunaImageRes = R.drawable.unicorn_1
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
            item = LunaInventoryItem.cowboy_2,
            title = "Cowboy 2",
            priceCoins = 20,
            iconRes = R.drawable.cowboy_2,
            lunaImageRes = R.drawable.cowboy_2
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
            item = LunaInventoryItem.captainunderpants_1,
            title = "Captain Underpants",
            priceCoins = 20,
            iconRes = R.drawable.captainunderpants_1,
            lunaImageRes = R.drawable.captainunderpants_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.cyber_1,
            title = "Cyber 1",
            priceCoins = 20,
            iconRes = R.drawable.cyber_1,
            lunaImageRes = R.drawable.cyber_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.cyber_2,
            title = "Cyber 2",
            priceCoins = 20,
            iconRes = R.drawable.cyber_2,
            lunaImageRes = R.drawable.cyber_2
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.perry_1,
            title = "Perry 1",
            priceCoins = 20,
            iconRes = R.drawable.perry_1,
            lunaImageRes = R.drawable.perry_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.perry_2,
            title = "Perry 2",
            priceCoins = 20,
            iconRes = R.drawable.perry_2,
            lunaImageRes = R.drawable.perry_2
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.flash_1,
            title = "Flash",
            priceCoins = 100,
            iconRes = R.drawable.flash_1,
            lunaImageRes = R.drawable.luna_flash_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.deadpool_1,
            title = "Deadpool",
            priceCoins = 100,
            iconRes = R.drawable.deadpool_1,
            lunaImageRes = R.drawable.deadpool_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.spiderman_1,
            title = "Spider-Man",
            priceCoins = 100,
            iconRes = R.drawable.spiderman_1,
            lunaImageRes = R.drawable.spiderman_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.krypto_1,
            title = "Krypto",
            priceCoins = 100,
            iconRes = R.drawable.krypto_1,
            lunaImageRes = R.drawable.krypto_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.iron_1,
            title = "Iron Man",
            priceCoins = 100,
            iconRes = R.drawable.iron_1,
            lunaImageRes = R.drawable.luna_iron_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.greenlantern_1,
            title = "Green Lantern",
            priceCoins = 100,
            iconRes = R.drawable.greenlantern_1,
            lunaImageRes = R.drawable.greenlantern_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.greenarrow_1,
            title = "Green Arrow",
            priceCoins = 100,
            iconRes = R.drawable.greenarrow_1,
            lunaImageRes = R.drawable.greenarrow_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.jedi_1,
            title = "Jedi",
            priceCoins = 100,
            iconRes = R.drawable.jedi_1,
            lunaImageRes = R.drawable.jedi_1
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

        LunaItemDefinition(
            item = LunaInventoryItem.ca_1,
            title = "Captain America",
            priceCoins = 100,
            iconRes = R.drawable.ca_1,
            lunaImageRes = R.drawable.ca_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.ca_2,
            title = "Captain America 2",
            priceCoins = 100,
            iconRes = R.drawable.ca_2,
            lunaImageRes = R.drawable.ca_2
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.virgil_1,
            title = "Virgil",
            priceCoins = 100,
            iconRes = R.drawable.virgil_1,
            lunaImageRes = R.drawable.virgil_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.dante_1,
            title = "Dante",
            priceCoins = 100,
            iconRes = R.drawable.dante_1,
            lunaImageRes = R.drawable.dante_1
        ),

        LunaItemDefinition(
            item = LunaInventoryItem.blade_1,
            title = "Blade",
            priceCoins = 100,
            iconRes = R.drawable.blade_1,
            lunaImageRes = R.drawable.blade_1
        ),





        )

    fun getDefinition(item: LunaInventoryItem): LunaItemDefinition {
        return allItems.first { it.item == item }
    }
}