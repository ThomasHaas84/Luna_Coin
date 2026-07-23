package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
enum class LunarimSpellSchool {
    LIGHT,
    HOLY,
    ELEMENTAL,
    ARCANE,
    WARP,
    SHADOW,
    BLOOD,
    BLACK_MAGIC
}

@Serializable
enum class LunarimSpellType {
    DAMAGE,
    HEAL,
    PROTECTION,
    BUFF,
    DEBUFF,
    SUMMON,
    UTILITY
}

@Serializable
enum class LunarimSpellTarget {
    SELF,
    ALLY,
    ENEMY,
    ALL_ALLIES,
    ALL_ENEMIES,
    AREA
}

@Serializable
data class LunarimSpellEffect(
    val effectId: String,
    val chance: Float = 1f,
    val strength: Float = 1f
) {
    init {
        require(chance in 0f..1f)
        require(strength >= 0f)
    }
}

@Serializable
data class LunarimSpell(
    val id: String,
    val name: String,
    val description: String,
    val school: LunarimSpellSchool,
    val type: LunarimSpellType,
    val manaCost: Int,
    val requiredSkillLevel: Int,
    val target: LunarimSpellTarget = LunarimSpellTarget.ENEMY,
    val castTimeMillis: Long = 0L,
    val cooldownMillis: Long = 0L,
    val duration: LunarimEffectDuration? = null,
    val damage: LunarimDamageProfile? = null,
    val instantHeal: Int = 0,
    val effects: List<LunarimSpellEffect> = emptyList()
)
