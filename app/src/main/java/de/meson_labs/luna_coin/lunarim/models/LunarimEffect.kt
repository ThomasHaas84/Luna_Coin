package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
enum class LunarimEffectCategory {
    BUFF,
    DEBUFF,
    DISEASE,
    CURSE,
    BLESSING,
    FIRE,
    COLD,
    LIGHTNING,
    POISON,
    BLEEDING,
    STUN,
    ARCANE,
    HOLY,
    SHADOW,
    HEALING
}

@Serializable
enum class LunarimEffectTickType {
    NONE,
    DAMAGE,
    HEALING
}

@Serializable
enum class LunarimDisease {
    VAMPIRISM,
    SYPHILIS,
    HERPES,
    LEPROSY
}

@Serializable
data class LunarimEffectDuration(
    val outsideCombatDurationMillis: Long? = null,
    val combatDurationTurns: Int? = null
) {
    init {
        require(outsideCombatDurationMillis == null || outsideCombatDurationMillis >= 0L)
        require(combatDurationTurns == null || combatDurationTurns >= 0)
    }
}

@Serializable
data class LunarimEffectModifiers(
    val maxHealthFlat: Int = 0,
    val maxManaFlat: Int = 0,
    val maxActionPointsFlat: Int = 0,
    val actionPointsPerTurnFlat: Int = 0,
    val carryCapacityGramsFlat: Int = 0,
    val armorFlat: Int = 0,
    val armorPercent: Int = 0,
    val dodgePercent: Int = 0,
    val criticalChancePercent: Int = 0,
    val physicalDamagePercent: Int = 0,
    val rangedDamagePercent: Int = 0,
    val magicDamagePercent: Int = 0,
    val resistances: LunarimResistances = LunarimResistances(),
    val immunities: LunarimImmunities = LunarimImmunities()
)

@Serializable
data class LunarimEffect(
    val id: String,
    val name: String,
    val description: String,
    val category: LunarimEffectCategory,
    val disease: LunarimDisease? = null,
    val defaultDuration: LunarimEffectDuration = LunarimEffectDuration(),
    val stackable: Boolean = false,
    val maxStacks: Int = 1,
    val tickType: LunarimEffectTickType = LunarimEffectTickType.NONE,
    val tickAmount: Int = 0,
    val modifiers: LunarimEffectModifiers = LunarimEffectModifiers()
) {
    init {
        require(id.isNotBlank())
        require(maxStacks >= 1)
    }
}

@Serializable
data class LunarimActiveEffect(
    val effectId: String,
    val appliedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long? = null,
    val remainingCombatTurns: Int? = null,
    val stacks: Int = 1,
    val strength: Float = 1f
) {
    fun isExpiredOutsideCombat(nowEpochMillis: Long): Boolean =
        expiresAtEpochMillis?.let { nowEpochMillis >= it } ?: false

    fun isExpiredInCombat(): Boolean =
        remainingCombatTurns?.let { it <= 0 } ?: false
}
