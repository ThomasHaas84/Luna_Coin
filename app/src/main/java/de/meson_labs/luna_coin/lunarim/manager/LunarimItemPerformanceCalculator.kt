package de.meson_labs.luna_coin.lunarim.manager

import de.meson_labs.luna_coin.lunarim.models.*
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Zentrale Berechnung für Waffen, Rüstungen und Schilde.
 */
object LunarimItemPerformanceCalculator {

    private const val MINIMUM_USABLE_DAMAGE_FACTOR = 0.10f
    private const val MINIMUM_REQUIREMENT_FACTOR = 0.25f
    private const val MINIMUM_HIT_CHANCE_PERCENT = 5
    private const val MAXIMUM_HIT_CHANCE_PERCENT = 95
    private const val PENALTY_PER_MISSING_REQUIREMENT_POINT = 0.06f
    private const val AP_PENALTY_PER_MISSING_REQUIREMENT_POINT = 0.10f
    private const val HIT_PENALTY_PER_MISSING_REQUIREMENT_POINT = 4

    data class RequirementResult(
        val requiredStrength: Int,
        val actualStrength: Int,
        val missingStrength: Int,
        val requiredAgility: Int,
        val actualAgility: Int,
        val missingAgility: Int,
        val totalMissingPoints: Int,
        val requirementsMet: Boolean,
        val skillFactor: Float,
        val actionPointMultiplier: Float,
        val hitChancePenaltyPercent: Int
    )

    data class WeaponPerformance(
        val usable: Boolean,
        val broken: Boolean,
        val conditionPercent: Int,
        val conditionFactor: Float,
        val requirements: RequirementResult,
        val totalEffectivenessFactor: Float,
        val baseDamageProfile: LunarimDamageProfile,
        val effectiveDamageProfile: LunarimDamageProfile,
        val baseActionPointCost: Int,
        val effectiveActionPointCost: Int,
        val baseHitChancePercent: Int,
        val effectiveHitChancePercent: Int,
        val speed: Int,
        val range: LunarimWeaponRange,
        val balance: Int,
        val criticalChancePercent: Int,
        val criticalDamageMultiplier: Float,
        val blockBreakerPercent: Int,
        val armorBreaker: Int,
        val durabilityLossPerUse: Int
    ) {
        val baseTotalDamage: Int
            get() = baseDamageProfile.totalDamage

        val effectiveTotalDamage: Int
            get() = effectiveDamageProfile.totalDamage
    }

    data class AttackRollResult(
        val usable: Boolean,
        val hit: Boolean,
        val critical: Boolean,
        val hitRoll: Int,
        val criticalRoll: Int?,
        val hitChancePercent: Int,
        val criticalChancePercent: Int,
        val normalDamageProfile: LunarimDamageProfile,
        val resultingDamageProfile: LunarimDamageProfile,
        val actionPointCost: Int,
        val durabilityLoss: Int,
        val armorBreaker: Int,
        val blockBreakerPercent: Int
    )

    data class ArmorPerformance(
        val usable: Boolean,
        val broken: Boolean,
        val conditionPercent: Int,
        val conditionFactor: Float,
        val requirements: RequirementResult,
        val totalEffectivenessFactor: Float,
        val baseArmor: Int,
        val effectiveArmor: Int,
        val baseResistances: LunarimResistances,
        val effectiveResistances: LunarimResistances
    )

    fun calculateRequirements(
        requirements: LunarimItemRequirements,
        attributes: LunarimAttributes
    ): RequirementResult {
        val missingStrength =
            (requirements.minimumStrength - attributes.strength).coerceAtLeast(0)

        val missingAgility =
            (requirements.minimumAgility - attributes.agility).coerceAtLeast(0)

        val totalMissingPoints = missingStrength + missingAgility

        val skillFactor = (
                1f - totalMissingPoints * PENALTY_PER_MISSING_REQUIREMENT_POINT
                ).coerceIn(MINIMUM_REQUIREMENT_FACTOR, 1f)

        return RequirementResult(
            requiredStrength = requirements.minimumStrength,
            actualStrength = attributes.strength,
            missingStrength = missingStrength,
            requiredAgility = requirements.minimumAgility,
            actualAgility = attributes.agility,
            missingAgility = missingAgility,
            totalMissingPoints = totalMissingPoints,
            requirementsMet = totalMissingPoints == 0,
            skillFactor = skillFactor,
            actionPointMultiplier =
                1f + totalMissingPoints * AP_PENALTY_PER_MISSING_REQUIREMENT_POINT,
            hitChancePenaltyPercent =
                totalMissingPoints * HIT_PENALTY_PER_MISSING_REQUIREMENT_POINT
        )
    }

    fun calculateWeapon(
        item: LunarimItem,
        instance: LunarimItemInstance,
        attributes: LunarimAttributes
    ): WeaponPerformance {
        require(item.isWeapon) {
            "${item.name} ist keine Waffe."
        }

        val combatStats = item.weaponCombatStats ?: LunarimWeaponCombatStats()
        val requirements = calculateRequirements(item.requirements, attributes)
        val broken = instance.isBroken
        val usable = !broken
        val conditionFactor = instance.conditionFactor.coerceIn(0f, 1f)

        val totalEffectivenessFactor = if (broken) {
            0f
        } else {
            (conditionFactor * requirements.skillFactor)
                .coerceIn(MINIMUM_USABLE_DAMAGE_FACTOR, 1f)
        }

        val baseProfile = item.damage ?: LunarimDamageProfile()
        val effectiveProfile = scaleDamageProfile(
            profile = baseProfile,
            factor = totalEffectivenessFactor,
            usable = usable
        )

        val speedApMultiplier = speedToApMultiplier(combatStats.speed)

        val effectiveApCost = when {
            !usable -> 0
            item.baseActionPointCost <= 0 -> 0
            else -> ceil(
                item.baseActionPointCost *
                        requirements.actionPointMultiplier *
                        speedApMultiplier
            ).toInt()
        }

        val conditionPenalty =
            ((1f - conditionFactor) * 30f).roundToInt()

        val effectiveHitChance = if (!usable) {
            0
        } else {
            (
                    item.baseHitChancePercent +
                            combatStats.balance -
                            requirements.hitChancePenaltyPercent -
                            conditionPenalty
                    ).coerceIn(
                    MINIMUM_HIT_CHANCE_PERCENT,
                    MAXIMUM_HIT_CHANCE_PERCENT
                )
        }

        val effectiveCriticalChance = if (!usable) {
            0
        } else {
            (
                    combatStats.criticalChancePercent -
                            requirements.totalMissingPoints * 2 -
                            ((1f - conditionFactor) * 10f).roundToInt()
                    ).coerceIn(0, 100)
        }

        return WeaponPerformance(
            usable = usable,
            broken = broken,
            conditionPercent = instance.conditionPercent,
            conditionFactor = conditionFactor,
            requirements = requirements,
            totalEffectivenessFactor = totalEffectivenessFactor,
            baseDamageProfile = baseProfile,
            effectiveDamageProfile = effectiveProfile,
            baseActionPointCost = item.baseActionPointCost.coerceAtLeast(0),
            effectiveActionPointCost = effectiveApCost,
            baseHitChancePercent = item.baseHitChancePercent,
            effectiveHitChancePercent = effectiveHitChance,
            speed = combatStats.speed,
            range = combatStats.range,
            balance = combatStats.balance,
            criticalChancePercent = effectiveCriticalChance,
            criticalDamageMultiplier = combatStats.criticalDamageMultiplier,
            blockBreakerPercent = combatStats.blockBreakerPercent,
            armorBreaker = combatStats.armorBreaker,
            durabilityLossPerUse = combatStats.durabilityLossPerUse
        )
    }

    /**
     * Würfelt einen vollständigen Angriff.
     *
     * Die eigentliche gegnerische Rüstung und Resistenz wird anschließend
     * vom vorhandenen LunarimDamageCalculator verarbeitet.
     */
    fun rollAttack(
        performance: WeaponPerformance,
        random: Random = Random.Default
    ): AttackRollResult {
        if (!performance.usable) {
            return AttackRollResult(
                usable = false,
                hit = false,
                critical = false,
                hitRoll = 100,
                criticalRoll = null,
                hitChancePercent = 0,
                criticalChancePercent = 0,
                normalDamageProfile = performance.effectiveDamageProfile,
                resultingDamageProfile = zeroDamageProfile(
                    performance.effectiveDamageProfile
                ),
                actionPointCost = 0,
                durabilityLoss = 0,
                armorBreaker = 0,
                blockBreakerPercent = 0
            )
        }

        val hitRoll = random.nextInt(1, 101)
        val hit = hitRoll <= performance.effectiveHitChancePercent

        if (!hit) {
            return AttackRollResult(
                usable = true,
                hit = false,
                critical = false,
                hitRoll = hitRoll,
                criticalRoll = null,
                hitChancePercent = performance.effectiveHitChancePercent,
                criticalChancePercent = performance.criticalChancePercent,
                normalDamageProfile = performance.effectiveDamageProfile,
                resultingDamageProfile = zeroDamageProfile(
                    performance.effectiveDamageProfile
                ),
                actionPointCost = performance.effectiveActionPointCost,
                durabilityLoss = performance.durabilityLossPerUse,
                armorBreaker = performance.armorBreaker,
                blockBreakerPercent = performance.blockBreakerPercent
            )
        }

        val criticalRoll = random.nextInt(1, 101)
        val critical = criticalRoll <= performance.criticalChancePercent

        val resultingProfile = if (critical) {
            scaleDamageProfile(
                profile = performance.effectiveDamageProfile,
                factor = performance.criticalDamageMultiplier,
                usable = true
            )
        } else {
            performance.effectiveDamageProfile
        }

        return AttackRollResult(
            usable = true,
            hit = true,
            critical = critical,
            hitRoll = hitRoll,
            criticalRoll = criticalRoll,
            hitChancePercent = performance.effectiveHitChancePercent,
            criticalChancePercent = performance.criticalChancePercent,
            normalDamageProfile = performance.effectiveDamageProfile,
            resultingDamageProfile = resultingProfile,
            actionPointCost = performance.effectiveActionPointCost,
            durabilityLoss = performance.durabilityLossPerUse,
            armorBreaker = performance.armorBreaker,
            blockBreakerPercent = performance.blockBreakerPercent
        )
    }

    /**
     * Reduziert einen gegnerischen Blockwert durch den Blockbrecher der Waffe.
     */
    fun applyBlockBreaker(
        baseBlockValue: Int,
        blockBreakerPercent: Int
    ): Int {
        val reduction = (
                baseBlockValue.coerceAtLeast(0) *
                        blockBreakerPercent.coerceIn(0, 100) /
                        100f
                ).roundToInt()

        return (baseBlockValue - reduction).coerceAtLeast(0)
    }

    /**
     * Reduziert einen allgemeinen Rüstungswert durch den Rüstungsbrecher.
     */
    fun applyArmorBreaker(
        baseArmor: Int,
        armorBreaker: Int
    ): Int =
        (baseArmor - armorBreaker).coerceAtLeast(0)

    fun calculateArmor(
        item: LunarimItem,
        instance: LunarimItemInstance,
        attributes: LunarimAttributes
    ): ArmorPerformance {
        require(item.isArmorOrShield) {
            "${item.name} ist weder Rüstung noch Schild."
        }

        val requirements = calculateRequirements(item.requirements, attributes)
        val broken = instance.isBroken
        val usable = !broken
        val conditionFactor = instance.conditionFactor.coerceIn(0f, 1f)

        val totalEffectivenessFactor = if (broken) {
            0f
        } else {
            (conditionFactor * requirements.skillFactor).coerceIn(0f, 1f)
        }

        return ArmorPerformance(
            usable = usable,
            broken = broken,
            conditionPercent = instance.conditionPercent,
            conditionFactor = conditionFactor,
            requirements = requirements,
            totalEffectivenessFactor = totalEffectivenessFactor,
            baseArmor = item.armor,
            effectiveArmor = scaleValue(item.armor, totalEffectivenessFactor),
            baseResistances = item.resistances,
            effectiveResistances = scaleResistances(
                item.resistances,
                totalEffectivenessFactor
            )
        )
    }

    private fun speedToApMultiplier(speed: Int): Float =
        when {
            speed >= 85 -> 0.75f
            speed >= 70 -> 0.85f
            speed >= 55 -> 0.95f
            speed >= 40 -> 1.00f
            speed >= 25 -> 1.15f
            else -> 1.30f
        }

    private fun scaleDamageProfile(
        profile: LunarimDamageProfile,
        factor: Float,
        usable: Boolean
    ): LunarimDamageProfile {
        if (!usable || factor <= 0f) return zeroDamageProfile(profile)

        return profile.copy(
            parts = profile.parts.map { part ->
                part.copy(
                    amount = scaleValue(
                        value = part.amount,
                        factor = factor,
                        keepMinimumOne = part.amount > 0
                    )
                )
            },
            procs = profile.procs.map { proc ->
                proc.copy(
                    chance = (proc.chance * factor).coerceIn(0f, 1f)
                )
            }
        )
    }

    private fun zeroDamageProfile(
        profile: LunarimDamageProfile
    ): LunarimDamageProfile =
        profile.copy(
            parts = profile.parts.map { it.copy(amount = 0) },
            procs = emptyList()
        )

    private fun scaleResistances(
        resistances: LunarimResistances,
        factor: Float
    ): LunarimResistances =
        LunarimResistances(
            slash = scaleValue(resistances.slash, factor),
            puncture = scaleValue(resistances.puncture, factor),
            impact = scaleValue(resistances.impact, factor),
            fire = scaleValue(resistances.fire, factor),
            cold = scaleValue(resistances.cold, factor),
            lightning = scaleValue(resistances.lightning, factor),
            toxin = scaleValue(resistances.toxin, factor),
            bleeding = scaleValue(resistances.bleeding, factor),
            arcane = scaleValue(resistances.arcane, factor),
            holy = scaleValue(resistances.holy, factor),
            shadow = scaleValue(resistances.shadow, factor)
        )

    private fun scaleValue(
        value: Int,
        factor: Float,
        keepMinimumOne: Boolean = false
    ): Int {
        if (value == 0 || factor <= 0f) return 0

        val scaled = (value * factor).roundToInt()

        return if (keepMinimumOne && value > 0) {
            scaled.coerceAtLeast(1)
        } else {
            scaled
        }
    }
}
