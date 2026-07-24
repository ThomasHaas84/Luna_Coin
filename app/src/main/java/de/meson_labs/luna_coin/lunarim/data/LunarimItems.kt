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

    val simpleDagger = LunarimItem(
        id = "simple_dagger",
        name = "Einfacher Dolch",
        description = "Eine leichte Klinge für Reisende und angehende Abenteurer.",
        type = LunarimItemType.WEAPON,
        weightGrams = 650,
        buyPriceSilver = 150,
        sellPriceSilver = 60,
        weaponType = LunarimWeaponType.DAGGER,
        requirements = LunarimItemRequirements(
            minimumStrength = 3,
            minimumAgility = 5
        ),
        baseActionPointCost = 2,
        baseHitChancePercent = 82,
        weaponCombatStats = LunarimWeaponCombatStats(
            speed = 82,
            range = LunarimWeaponRange.MELEE,
            balance = 8,
            criticalChancePercent = 12,
            criticalDamageMultiplier = 1.70f,
            blockBreakerPercent = 2,
            armorBreaker = 2,
            durabilityLossPerUse = 1
        ),
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.SLASH, 7),
                LunarimDamagePart(LunarimDamageType.PUNCTURE, 9),
                LunarimDamagePart(LunarimDamageType.IMPACT, 1)
            ),
            procs = listOf(
                LunarimProc(LunarimEffects.BLEEDING_ID, 0.18f)
            ),
            penetration = LunarimPenetration(
                puncture = 8
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

    val clothClothes = LunarimItem(
        id = "cloth_clothes",
        name = "Stoffkleidung",
        description = "Schlichte Kleidung mit geringem Schutz vor Kälte und Schmutz.",
        type = LunarimItemType.ARMOR,
        weightGrams = 1_200,
        buyPriceSilver = 120,
        sellPriceSilver = 48,
        requirements = LunarimItemRequirements(
            minimumStrength = 1,
            minimumAgility = 1
        ),
        armor = 3,
        resistances = LunarimResistances(
            slash = 1,
            impact = 1
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

    val travelRations = LunarimItem(
        id = "travel_rations",
        name = "Reiseproviant",
        description = "Ein haltbares Mahl für lange Wege durch Lunarim.",
        type = LunarimItemType.CONSUMABLE,
        weightGrams = 400,
        buyPriceSilver = 25,
        sellPriceSilver = 10,
        stackable = true,
        maxStackSize = 20
    )

    val waterFlask = LunarimItem(
        id = "water_flask",
        name = "Wasserflasche",
        description = "Sauberes Wasser in einer einfachen Feldflasche.",
        type = LunarimItemType.CONSUMABLE,
        weightGrams = 750,
        buyPriceSilver = 15,
        sellPriceSilver = 6,
        stackable = true,
        maxStackSize = 10
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

    val minorHealingPotion = LunarimItem(
        id = "minor_healing_potion",
        name = "Einfacher Heiltrank",
        description = "Stellt später einen kleinen Teil der Gesundheit wieder her.",
        type = LunarimItemType.CONSUMABLE,
        weightGrams = 180,
        buyPriceSilver = 80,
        sellPriceSilver = 32,
        stackable = true,
        maxStackSize = 20
    )

    val healingHerbs = LunarimItem(
        id = "healing_herbs",
        name = "Heilkräuter",
        description = "Getrocknete Kräuter für Tränke und einfache Heilmittel.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.HERB,
        weightGrams = 40,
        buyPriceSilver = 35,
        sellPriceSilver = 14,
        stackable = true,
        maxStackSize = 50
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

    val firewood = LunarimItem(
        id = "firewood",
        name = "Brennholz",
        description = "Trockenes Holz für Lagerfeuer, Küche und Handwerk.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.WOOD,
        weightGrams = 1_000,
        buyPriceSilver = 20,
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

    val ironChunk = LunarimItem(
        id = "iron_chunk",
        name = "Eisenbrocken",
        description = "Ein roher Eisenklumpen für spätere Schmiedearbeiten.",
        type = LunarimItemType.RAW_MATERIAL,
        rawMaterialType = LunarimRawMaterialType.METAL,
        weightGrams = 750,
        buyPriceSilver = 45,
        sellPriceSilver = 18,
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

    val simpleRope = LunarimItem(
        id = "simple_rope",
        name = "Einfaches Seil",
        description = "Nützlich beim Erkunden, Klettern und Sichern von Lasten.",
        type = LunarimItemType.MATERIAL,
        weightGrams = 1_400,
        buyPriceSilver = 40,
        sellPriceSilver = 16,
        stackable = true,
        maxStackSize = 10
    )

    val torch = LunarimItem(
        id = "torch",
        name = "Fackel",
        description = "Erhellt später Höhlen, Ruinen und dunkle Wege.",
        type = LunarimItemType.CONSUMABLE,
        weightGrams = 600,
        buyPriceSilver = 30,
        sellPriceSilver = 12,
        stackable = true,
        maxStackSize = 10
    )

    val all = listOf(
        ironSword,
        warHammer,
        huntingBow,
        simpleDagger,
        woodenShield,
        clothClothes,
        leatherArmor,
        ironArmor,
        travelRations,
        waterFlask,
        healingPotion,
        minorHealingPotion,
        healingHerbs,
        wood,
        firewood,
        stone,
        iron,
        ironChunk,
        ironOre,
        copperOre,
        coal,
        moonHerb,
        simpleRope,
        torch
    )

    /**
     * Festes Sortiment des lokalen Händlers für die erste Shop-Phase.
     * Die Reihenfolge entspricht der Darstellung im Shop.
     */
    val localShopItems = listOf(
        travelRations,
        waterFlask,
        healingHerbs,
        minorHealingPotion,
        firewood,
        ironChunk,
        simpleRope,
        torch,
        simpleDagger,
        clothClothes
    )

    /**
     * Vorläufiger Händlerbestand. Später kann dieser Wert in einen eigenen
     * Shop-State oder nach Firestore verschoben werden, ohne den Screen zu ändern.
     */
    val localShopStockByItemId = mapOf(
        travelRations.id to 12,
        waterFlask.id to 18,
        healingHerbs.id to 9,
        minorHealingPotion.id to 5,
        firewood.id to 20,
        ironChunk.id to 10,
        simpleRope.id to 7,
        torch.id to 14,
        simpleDagger.id to 3,
        clothClothes.id to 4
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

    fun getLocalShopStock(itemId: String): Int =
        localShopStockByItemId[itemId] ?: 0
}
