package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
data class LunarimAttributes(
    val strength: Int = 1,
    val perception: Int = 1,
    val endurance: Int = 1,
    val charisma: Int = 1,
    val intelligence: Int = 1,
    val agility: Int = 1,
    val luck: Int = 1
)
