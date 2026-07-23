package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
data class LunarimCampState(
    val campLevel: Int = 1,
    val storageLevel: Int = 1,
    val kitchenLevel: Int = 1,
    val workshopLevel: Int = 1
)

@Serializable
data class LunarimGameState(
    val childId: String,
    val hasStarted: Boolean = false,
    val camp: LunarimCampState = LunarimCampState(),
    val character: LunarimCharacterState = LunarimCharacterState(),
    val currentLocationId: String = "camp",
    val lastOpenedDestination: String = "camp"
) {
    companion object {
        fun newGame(childId: String): LunarimGameState =
            LunarimGameState(childId = childId, hasStarted = true)

        fun resetForChild(childId: String): LunarimGameState =
            LunarimGameState(childId = childId, hasStarted = false)
    }
}
