package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
enum class LunarimEquipmentSlot {
    MAIN_HAND,
    OFF_HAND,
    HEAD,
    CHEST,
    HANDS,
    LEGS,
    FEET,
    NECKLACE,
    LEFT_RING,
    RIGHT_RING
}

@Serializable
data class LunarimEquipment(
    val equippedInstanceIds: Map<LunarimEquipmentSlot, String> = emptyMap()
) {
    fun get(slot: LunarimEquipmentSlot): String? = equippedInstanceIds[slot]

    fun equip(slot: LunarimEquipmentSlot, instanceId: String): LunarimEquipment =
        copy(equippedInstanceIds = equippedInstanceIds + (slot to instanceId))

    fun unequip(slot: LunarimEquipmentSlot): LunarimEquipment =
        copy(equippedInstanceIds = equippedInstanceIds - slot)
}
