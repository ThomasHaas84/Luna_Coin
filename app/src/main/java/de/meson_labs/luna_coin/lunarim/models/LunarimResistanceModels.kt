package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

const val LUNARIM_MIN_RESISTANCE = -100
const val LUNARIM_MAX_RESISTANCE = 99

@Serializable
data class LunarimResistances(
    val slash: Int = 0,
    val puncture: Int = 0,
    val impact: Int = 0,
    val fire: Int = 0,
    val cold: Int = 0,
    val lightning: Int = 0,
    val toxin: Int = 0,
    val bleeding: Int = 0,
    val arcane: Int = 0,
    val holy: Int = 0,
    val shadow: Int = 0
) {
    fun clamped(): LunarimResistances = copy(
        slash = slash.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        puncture = puncture.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        impact = impact.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        fire = fire.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        cold = cold.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        lightning = lightning.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        toxin = toxin.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        bleeding = bleeding.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        arcane = arcane.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        holy = holy.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE),
        shadow = shadow.coerceIn(LUNARIM_MIN_RESISTANCE, LUNARIM_MAX_RESISTANCE)
    )

    operator fun plus(other: LunarimResistances): LunarimResistances = LunarimResistances(
        slash = slash + other.slash,
        puncture = puncture + other.puncture,
        impact = impact + other.impact,
        fire = fire + other.fire,
        cold = cold + other.cold,
        lightning = lightning + other.lightning,
        toxin = toxin + other.toxin,
        bleeding = bleeding + other.bleeding,
        arcane = arcane + other.arcane,
        holy = holy + other.holy,
        shadow = shadow + other.shadow
    )
}

@Serializable
data class LunarimPenetration(
    val slash: Int = 0,
    val puncture: Int = 0,
    val impact: Int = 0,
    val fire: Int = 0,
    val cold: Int = 0,
    val lightning: Int = 0,
    val toxin: Int = 0,
    val bleeding: Int = 0,
    val arcane: Int = 0,
    val holy: Int = 0,
    val shadow: Int = 0
)

@Serializable
data class LunarimImmunities(
    val slash: Boolean = false,
    val puncture: Boolean = false,
    val impact: Boolean = false,
    val fire: Boolean = false,
    val cold: Boolean = false,
    val lightning: Boolean = false,
    val toxin: Boolean = false,
    val bleeding: Boolean = false,
    val arcane: Boolean = false,
    val holy: Boolean = false,
    val shadow: Boolean = false
) {
    operator fun plus(other: LunarimImmunities): LunarimImmunities = LunarimImmunities(
        slash = slash || other.slash,
        puncture = puncture || other.puncture,
        impact = impact || other.impact,
        fire = fire || other.fire,
        cold = cold || other.cold,
        lightning = lightning || other.lightning,
        toxin = toxin || other.toxin,
        bleeding = bleeding || other.bleeding,
        arcane = arcane || other.arcane,
        holy = holy || other.holy,
        shadow = shadow || other.shadow
    )
}
