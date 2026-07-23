package de.meson_labs.luna_coin.lunarim.data

import de.meson_labs.luna_coin.lunarim.models.*

object LunarimItems {

    val ironSword = LunarimItem(
        id = "iron_sword",
        name = "Eisenschwert",
        description = "Ein zuverlässiges Einhandschwert.",
        type = LunarimItemType.WEAPON,
        weightGrams = 1_800,
        buyPriceSilver = 800,
        sellPriceSilver = 280,
        weaponType = LunarimWeaponType.SWORD,
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.SLASH, 18),
                LunarimDamagePart(LunarimDamageType.PUNCTURE, 5),
                LunarimDamagePart(LunarimDamageType.IMPACT, 2)
            ),
            procs = listOf(
                LunarimProc(LunarimEffects.BLEEDING_ID, 0.24f),
                LunarimProc(LunarimEffects.STUNNED_ID, 0.03f)
            ),
            penetration = LunarimPenetration(slash = 5, puncture = 5)
        )
    )

    val warHammer = LunarimItem(
        id = "war_hammer",
        name = "Kriegshammer",
        description = "Eine schwere Zweihandwaffe mit hohem Einschlagschaden.",
        type = LunarimItemType.WEAPON,
        weightGrams = 4_600,
        buyPriceSilver = 1_050,
        sellPriceSilver = 360,
        weaponType = LunarimWeaponType.HAMMER,
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.PUNCTURE, 4),
                LunarimDamagePart(LunarimDamageType.IMPACT, 24)
            ),
            procs = listOf(
                LunarimProc(LunarimEffects.STUNNED_ID, 0.30f)
            ),
            penetration = LunarimPenetration(impact = 12)
        )
    )

    val huntingBow = LunarimItem(
        id = "hunting_bow",
        name = "Jagdbogen",
        description = "Ein Bogen mit hohem Durchschlagschaden.",
        type = LunarimItemType.WEAPON,
        weightGrams = 1_150,
        buyPriceSilver = 900,
        sellPriceSilver = 310,
        weaponType = LunarimWeaponType.BOW,
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.SLASH, 5),
                LunarimDamagePart(LunarimDamageType.PUNCTURE, 16),
                LunarimDamagePart(LunarimDamageType.IMPACT, 1)
            ),
            procs = listOf(
                LunarimProc(LunarimEffects.BLEEDING_ID, 0.12f)
            ),
            penetration = LunarimPenetration(puncture = 18)
        )
    )

    val woodenShield = LunarimItem(
        id = "wooden_shield",
        name = "Holzschild",
        description = "Ein einfacher Schild für die Nebenhand.",
        type = LunarimItemType.SHIELD,
        weightGrams = 2_800,
        buyPriceSilver = 450,
        sellPriceSilver = 150,
        armor = 12,
        resistances = LunarimResistances(impact = 10)
    )

    val healingPotion = LunarimItem(
        id = "healing_potion",
        name = "Heiltrank",
        description = "Stellt durch einen Effekt Lebenspunkte wieder her.",
        type = LunarimItemType.CONSUMABLE,
        weightGrams = 250,
        buyPriceSilver = 180,
        sellPriceSilver = 60,
        stackable = true,
        maxStackSize = 20
    )

    val all = listOf(
        ironSword,
        warHammer,
        huntingBow,
        woodenShield,
        healingPotion
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): LunarimItem? = byId[id]
}
