package de.meson_labs.luna_coin.lunarim.data

import de.meson_labs.luna_coin.lunarim.models.*

object LunarimItems {

    val ironSword = LunarimItem(
        id = "iron_sword",
        name = "Eisenschwert",
        description = "Ein zuverlässiges und ausgewogenes Einhandschwert.",
        type = LunarimItemType.WEAPON,
        weightGrams = 1_800,
        buyPriceSilver = 800,
        sellPriceSilver = 280,
        weaponType = LunarimWeaponType.SWORD,
        requirements = LunarimItemRequirements(
            minimumStrength = 8,
            minimumAgility = 6
        ),
        baseActionPointCost = 3,
        baseHitChancePercent = 78,
        weaponCombatStats = LunarimWeaponCombatStats(
            speed = 62,
            range = LunarimWeaponRange.MELEE,
            balance = 5,
            criticalChancePercent = 8,
            criticalDamageMultiplier = 1.60f,
            blockBreakerPercent = 8,
            armorBreaker = 3,
            durabilityLossPerUse = 1
        ),
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
            penetration = LunarimPenetration(
                slash = 5,
                puncture = 5
            )
        )
    )

    val warHammer = LunarimItem(
        id = "war_hammer",
        name = "Kriegshammer",
        description = "Langsam und schwer, aber sehr wirksam gegen Rüstung und Schilde.",
        type = LunarimItemType.WEAPON,
        weightGrams = 4_600,
        buyPriceSilver = 1_050,
        sellPriceSilver = 360,
        weaponType = LunarimWeaponType.HAMMER,
        requirements = LunarimItemRequirements(
            minimumStrength = 16,
            minimumAgility = 7
        ),
        baseActionPointCost = 5,
        baseHitChancePercent = 68,
        weaponCombatStats = LunarimWeaponCombatStats(
            speed = 25,
            range = LunarimWeaponRange.MELEE,
            balance = -8,
            criticalChancePercent = 6,
            criticalDamageMultiplier = 2.10f,
            blockBreakerPercent = 40,
            armorBreaker = 12,
            durabilityLossPerUse = 2
        ),
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.PUNCTURE, 4),
                LunarimDamagePart(LunarimDamageType.IMPACT, 24)
            ),
            procs = listOf(
                LunarimProc(LunarimEffects.STUNNED_ID, 0.30f)
            ),
            penetration = LunarimPenetration(
                impact = 12
            )
        )
    )

    val huntingBow = LunarimItem(
        id = "hunting_bow",
        name = "Jagdbogen",
        description = "Eine präzise Fernkampfwaffe mit hoher Reichweite.",
        type = LunarimItemType.WEAPON,
        weightGrams = 1_150,
        buyPriceSilver = 900,
        sellPriceSilver = 310,
        weaponType = LunarimWeaponType.BOW,
        requirements = LunarimItemRequirements(
            minimumStrength = 6,
            minimumAgility = 13
        ),
        baseActionPointCost = 4,
        baseHitChancePercent = 74,
        weaponCombatStats = LunarimWeaponCombatStats(
            speed = 55,
            range = LunarimWeaponRange.RANGED,
            balance = 7,
            criticalChancePercent = 12,
            criticalDamageMultiplier = 1.80f,
            blockBreakerPercent = 4,
            armorBreaker = 5,
            durabilityLossPerUse = 1
        ),
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.SLASH, 5),
                LunarimDamagePart(LunarimDamageType.PUNCTURE, 16),
                LunarimDamagePart(LunarimDamageType.IMPACT, 1)
            ),
            procs = listOf(
                LunarimProc(LunarimEffects.BLEEDING_ID, 0.12f)
            ),
            penetration = LunarimPenetration(
                puncture = 18
            )
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
        requirements = LunarimItemRequirements(
            minimumStrength = 7,
            minimumAgility = 5
        ),
        armor = 12,
        resistances = LunarimResistances(
            impact = 10
        )
    )

    val leatherArmor = LunarimItem(
        id = "leather_armor",
        name = "Lederrüstung",
        description = "Eine leichte Rüstung aus gehärtetem Leder.",
        type = LunarimItemType.ARMOR,
        weightGrams = 5_500,
        buyPriceSilver = 720,
        sellPriceSilver = 250,
        requirements = LunarimItemRequirements(
            minimumStrength = 5,
            minimumAgility = 7
        ),
        armor = 15,
        resistances = LunarimResistances(
            slash = 8,
            puncture = 4
        )
    )

    val ironArmor = LunarimItem(
        id = "iron_armor",
        name = "Eisenrüstung",
        description = "Eine schwere Rüstung mit gutem Schutz gegen Hiebe.",
        type = LunarimItemType.ARMOR,
        weightGrams = 14_500,
        buyPriceSilver = 1_650,
        sellPriceSilver = 580,
        requirements = LunarimItemRequirements(
            minimumStrength = 15,
            minimumAgility = 8
        ),
        armor = 28,
        resistances = LunarimResistances(
            slash = 16,
            puncture = 10,
            impact = 5
        )
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

    val wood = LunarimItem(
        id = "raw_wood",
        name = "Holz",
        description = "Ein grundlegender Baustoff für Lager und Handwerk.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.WOOD,
        weightGrams = 1_000,
        buyPriceSilver = 18,
        sellPriceSilver = 8,
        stackable = true,
        maxStackSize = 100
    )

    val stone = LunarimItem(
        id = "raw_stone",
        name = "Stein",
        description = "Robustes Baumaterial für Befestigungen und Ausbau.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.STONE,
        weightGrams = 1_500,
        buyPriceSilver = 14,
        sellPriceSilver = 6,
        stackable = true,
        maxStackSize = 100
    )

    val iron = LunarimItem(
        id = "raw_iron",
        name = "Eisen",
        description = "Verarbeitetes Metall für Werkzeuge, Waffen und Rüstungen.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.METAL,
        weightGrams = 1_000,
        buyPriceSilver = 55,
        sellPriceSilver = 24,
        stackable = true,
        maxStackSize = 100
    )

    val ironOre = LunarimItem(
        id = "iron_ore",
        name = "Eisenerz",
        description = "Unverarbeitetes Erz, das eingeschmolzen werden kann.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.ORE,
        weightGrams = 1_250,
        buyPriceSilver = 32,
        sellPriceSilver = 14,
        stackable = true,
        maxStackSize = 100
    )

    val copperOre = LunarimItem(
        id = "copper_ore",
        name = "Kupfererz",
        description = "Ein häufiges Erz für Metallarbeiten und Legierungen.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.ORE,
        weightGrams = 1_150,
        buyPriceSilver = 26,
        sellPriceSilver = 11,
        stackable = true,
        maxStackSize = 100
    )

    val coal = LunarimItem(
        id = "coal",
        name = "Kohle",
        description = "Brennstoff für Schmiedeöfen und Werkstätten.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.OTHER,
        weightGrams = 500,
        buyPriceSilver = 20,
        sellPriceSilver = 9,
        stackable = true,
        maxStackSize = 100
    )

    val moonHerb = LunarimItem(
        id = "moon_herb",
        name = "Mondkraut",
        description = "Eine Pflanze für alchemistische Mixturen.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.ALCHEMY_INGREDIENT,
        weightGrams = 50,
        buyPriceSilver = 75,
        sellPriceSilver = 32,
        stackable = true,
        maxStackSize = 50
    )

    val all = listOf(
        ironSword,
        warHammer,
        huntingBow,
        woodenShield,
        leatherArmor,
        ironArmor,
        healingPotion,
        wood,
        stone,
        iron,
        ironOre,
        copperOre,
        coal,
        moonHerb
    )

    val weapons = all.filter { it.type == LunarimItemType.WEAPON }

    val armorItems = all.filter {
        it.type == LunarimItemType.ARMOR ||
                it.type == LunarimItemType.SHIELD
    }

    val rawMaterials = all.filter {
        it.type == LunarimItemType.RAW_MATERIAL
    }

    private val byId = all.associateBy { it.id }

    fun getById(id: String): LunarimItem? = byId[id]
}
