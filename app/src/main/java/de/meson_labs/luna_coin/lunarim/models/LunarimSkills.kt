package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
data class LunarimCombatSkills(
    val closeCombat: Int = 1,
    val oneHandedWeapon: Int = 1,
    val twoHandedWeapon: Int = 1,
    val shieldStance: Int = 1,
    val longRangedWeapon: Int = 1,
    val throwing: Int = 1
)

@Serializable
data class LunarimPracticalSkills(
    val medicine: Int = 1,
    val alchemy: Int = 1,
    val cooking: Int = 1,
    val crafting: Int = 1,
    val hunting: Int = 1,
    val fishing: Int = 1,
    val leadership: Int = 1,
    val blacksmithing: Int = 1,
    val mining: Int = 1,
    val lockPicking: Int = 1,
    val sneaking: Int = 1,
    val pickpocketing: Int = 1
)

@Serializable
data class LunarimMagicSkills(
    val light: Int = 1,
    val holy: Int = 1,
    val elemental: Int = 1,
    val arcane: Int = 1,
    val warp: Int = 1,
    val shadow: Int = 1,
    val blood: Int = 1,
    val blackMagic: Int = 1
)

@Serializable
data class LunarimSkills(
    val combat: LunarimCombatSkills = LunarimCombatSkills(),
    val practical: LunarimPracticalSkills = LunarimPracticalSkills(),
    val magic: LunarimMagicSkills = LunarimMagicSkills()
)
