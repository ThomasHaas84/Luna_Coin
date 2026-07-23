package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class LunarimItemType {
    WEAPON,
    AMMUNITION,
    ARMOR,
    SHIELD,
    CONSUMABLE,
    MATERIAL,
    RAW_MATERIAL,
    QUEST_ITEM
}

@Serializable
enum class LunarimWeaponType {
    NONE,
    SWORD,
    AXE,
    HAMMER,
    SPEAR,
    DAGGER,
    BOW,
    CROSSBOW,
    STAFF,
    WAND,
    THROWING
}

@Serializable
enum class LunarimWeaponRange {
    MELEE,
    REACH,
    RANGED
}

@Serializable
enum class LunarimRawMaterialType {
    WOOD,
    STONE,
    METAL,
    ORE,
    GEM,
    HERB,
    ANIMAL_MATERIAL,
    ALCHEMY_INGREDIENT,
    OTHER
}

@Serializable
enum class LunarimItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

@Serializable
data class LunarimItemRequirements(
    val minimumStrength: Int = 1,
    val minimumAgility: Int = 1
) {
    init {
        require(minimumStrength >= 1)
        require(minimumAgility >= 1)
    }

    fun isMetBy(attributes: LunarimAttributes): Boolean =
        attributes.strength >= minimumStrength &&
                attributes.agility >= minimumAgility
}

/**
 * Zusätzliche Kampfwerte einer Waffe.
 *
 * speed:
 * 1 = sehr langsam, 100 = sehr schnell.
 *
 * balance:
 * Modifiziert die Trefferchance direkt in Prozentpunkten.
 *
 * criticalChancePercent:
 * Grundchance auf einen kritischen Treffer.
 *
 * criticalDamageMultiplier:
 * 1.50 bedeutet 150 Prozent Schaden bei einem kritischen Treffer.
 *
 * blockBreakerPercent:
 * Prozentualer Bonus gegen einen aktiven Block oder Schildschutz.
 *
 * armorBreaker:
 * Zusätzliche allgemeine Rüstungsdurchdringung.
 *
 * durabilityLossPerUse:
 * Zustandsverlust bei einem normalen Einsatz.
 */
@Serializable
data class LunarimWeaponCombatStats(
    val speed: Int = 50,
    val range: LunarimWeaponRange = LunarimWeaponRange.MELEE,
    val balance: Int = 0,
    val criticalChancePercent: Int = 5,
    val criticalDamageMultiplier: Float = 1.50f,
    val blockBreakerPercent: Int = 0,
    val armorBreaker: Int = 0,
    val durabilityLossPerUse: Int = 1
) {
    init {
        require(speed in 1..100)
        require(balance in -50..50)
        require(criticalChancePercent in 0..100)
        require(criticalDamageMultiplier >= 1f)
        require(blockBreakerPercent in 0..100)
        require(armorBreaker >= 0)
        require(durabilityLossPerUse >= 0)
    }
}

@Serializable
data class LunarimItem(
    val id: String,
    val name: String,
    val description: String,
    val type: LunarimItemType,
    val weightGrams: Int,
    val buyPriceSilver: Int,
    val sellPriceSilver: Int,
    val stackable: Boolean = false,
    val maxStackSize: Int = 1,
    val weaponType: LunarimWeaponType = LunarimWeaponType.NONE,
    val rawMaterialType: LunarimRawMaterialType? = null,
    val requirements: LunarimItemRequirements = LunarimItemRequirements(),
    val baseActionPointCost: Int = 0,
    val baseHitChancePercent: Int = 75,
    val weaponCombatStats: LunarimWeaponCombatStats? = null,
    val damage: LunarimDamageProfile? = null,
    val armor: Int = 0,
    val resistances: LunarimResistances = LunarimResistances(),
    val immunities: LunarimImmunities = LunarimImmunities(),
    val useEffectIds: List<String> = emptyList()
) {
    val usesCondition: Boolean
        get() = type == LunarimItemType.WEAPON ||
                type == LunarimItemType.ARMOR ||
                type == LunarimItemType.SHIELD

    val isRawMaterial: Boolean
        get() = type == LunarimItemType.RAW_MATERIAL

    val isWeapon: Boolean
        get() = type == LunarimItemType.WEAPON

    val isArmorOrShield: Boolean
        get() = type == LunarimItemType.ARMOR ||
                type == LunarimItemType.SHIELD
}

@Serializable
data class LunarimItemInstance(
    val instanceId: String,
    val itemId: String,
    val amount: Int = 1,
    val durability: Int = 100,
    val maxDurability: Int = 100,
    val rarity: LunarimItemRarity = LunarimItemRarity.COMMON,
    val enchantmentIds: List<String> = emptyList(),
    val runeIds: List<String> = emptyList(),
    val customName: String? = null
) {
    val conditionPercent: Int
        get() {
            if (maxDurability <= 0) return 0

            return (
                    durability.coerceIn(0, maxDurability).toFloat() /
                            maxDurability.toFloat() *
                            100f
                    )
                .roundToInt()
                .coerceIn(0, 100)
        }

    val isBroken: Boolean
        get() = conditionPercent <= 0

    val conditionFactor: Float
        get() = conditionPercent / 100f

    fun canBeUsed(item: LunarimItem): Boolean =
        !item.usesCondition || !isBroken

    fun damageCondition(
        item: LunarimItem,
        amount: Int
    ): LunarimItemInstance {
        if (!item.usesCondition || amount <= 0) return this

        return copy(
            durability = (durability - amount).coerceAtLeast(0)
        )
    }

    fun applyWeaponUse(item: LunarimItem): LunarimItemInstance {
        if (!item.isWeapon) return this

        val loss = item.weaponCombatStats?.durabilityLossPerUse ?: 1
        return damageCondition(item, loss)
    }

    fun repairCondition(
        item: LunarimItem,
        amount: Int
    ): LunarimItemInstance {
        if (!item.usesCondition || amount <= 0) return this

        val safeMaximum = maxDurability.coerceAtLeast(1)

        return copy(
            durability = (durability + amount).coerceAtMost(safeMaximum),
            maxDurability = safeMaximum
        )
    }

    fun withConditionPercent(percent: Int): LunarimItemInstance {
        val safeMaximum = maxDurability.coerceAtLeast(1)
        val safePercent = percent.coerceIn(0, 100)
        val newDurability = (
                safeMaximum.toFloat() *
                        safePercent.toFloat() / 100f
                ).roundToInt()

        return copy(
            durability = newDurability,
            maxDurability = safeMaximum
        )
    }
}
