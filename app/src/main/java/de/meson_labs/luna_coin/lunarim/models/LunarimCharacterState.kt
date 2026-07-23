package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
data class LunarimCharacterState(
    val skills: LunarimSkills = LunarimSkills(),
    val inventory: List<LunarimItemInstance> = emptyList(),
    val equipment: LunarimEquipment = LunarimEquipment(),
    val activeEffects: List<LunarimActiveEffect> = emptyList(),
    val knownSpellIds: List<String> = emptyList(),
    val currentHealth: Int = 100,
    val currentMana: Int = 50,
    val currentActionPoints: Int = 5
)
