package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class LunarimDamageType {
    SLASH,
    PUNCTURE,
    IMPACT,
    FIRE,
    COLD,
    LIGHTNING,
    TOXIN,
    BLEEDING,
    ARCANE,
    HOLY,
    SHADOW
}

@Serializable
data class LunarimDamagePart(
    val type: LunarimDamageType,
    val amount: Int
)

@Serializable
data class LunarimProc(
    val effectId: String,
    val chance: Float,
    val strength: Float = 1f
) {
    init {
        require(chance in 0f..1f) { "chance muss zwischen 0f und 1f liegen." }
        require(strength >= 0f) { "strength darf nicht negativ sein." }
    }
}

@Serializable
data class LunarimDamageProfile(
    val parts: List<LunarimDamagePart> = emptyList(),
    val procs: List<LunarimProc> = emptyList(),
    val penetration: LunarimPenetration = LunarimPenetration()
) {
    val totalDamage: Int
        get() = parts.sumOf { it.amount.coerceAtLeast(0) }
}

data class LunarimResolvedDamagePart(
    val type: LunarimDamageType,
    val rawDamage: Int,
    val effectiveResistance: Int,
    val finalDamage: Int,
    val immune: Boolean
)

object LunarimDamageCalculator {

    fun resolve(
        profile: LunarimDamageProfile,
        resistances: LunarimResistances,
        immunities: LunarimImmunities = LunarimImmunities()
    ): List<LunarimResolvedDamagePart> {
        return profile.parts.map { part ->
            val immune = immunities.isImmuneTo(part.type)
            val resistance = resistances.valueFor(part.type)
            val penetration = profile.penetration.valueFor(part.type)
            val effectiveResistance = (resistance - penetration)
                .coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE)

            val finalDamage = if (immune) {
                0
            } else {
                (part.amount.coerceAtLeast(0) * (1f - effectiveResistance / 100f))
                    .roundToInt()
                    .coerceAtLeast(0)
            }

            LunarimResolvedDamagePart(
                type = part.type,
                rawDamage = part.amount,
                effectiveResistance = effectiveResistance,
                finalDamage = finalDamage,
                immune = immune
            )
        }
    }

    private fun LunarimResistances.valueFor(type: LunarimDamageType): Int = when (type) {
        LunarimDamageType.SLASH -> slash
        LunarimDamageType.PUNCTURE -> puncture
        LunarimDamageType.IMPACT -> impact
        LunarimDamageType.FIRE -> fire
        LunarimDamageType.COLD -> cold
        LunarimDamageType.LIGHTNING -> lightning
        LunarimDamageType.TOXIN -> toxin
        LunarimDamageType.BLEEDING -> bleeding
        LunarimDamageType.ARCANE -> arcane
        LunarimDamageType.HOLY -> holy
        LunarimDamageType.SHADOW -> shadow
    }

    private fun LunarimPenetration.valueFor(type: LunarimDamageType): Int = when (type) {
        LunarimDamageType.SLASH -> slash
        LunarimDamageType.PUNCTURE -> puncture
        LunarimDamageType.IMPACT -> impact
        LunarimDamageType.FIRE -> fire
        LunarimDamageType.COLD -> cold
        LunarimDamageType.LIGHTNING -> lightning
        LunarimDamageType.TOXIN -> toxin
        LunarimDamageType.BLEEDING -> bleeding
        LunarimDamageType.ARCANE -> arcane
        LunarimDamageType.HOLY -> holy
        LunarimDamageType.SHADOW -> shadow
    }

    private fun LunarimImmunities.isImmuneTo(type: LunarimDamageType): Boolean = when (type) {
        LunarimDamageType.SLASH -> slash
        LunarimDamageType.PUNCTURE -> puncture
        LunarimDamageType.IMPACT -> impact
        LunarimDamageType.FIRE -> fire
        LunarimDamageType.COLD -> cold
        LunarimDamageType.LIGHTNING -> lightning
        LunarimDamageType.TOXIN -> toxin
        LunarimDamageType.BLEEDING -> bleeding
        LunarimDamageType.ARCANE -> arcane
        LunarimDamageType.HOLY -> holy
        LunarimDamageType.SHADOW -> shadow
    }
}
