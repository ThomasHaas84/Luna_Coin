package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
data class LunarimCampState(
    /*
     * Ein neues Lager beginnt auf Stufe 0.
     * Die Bilddatei wird passend dazu als lvl_0.jpg geladen.
     */
    val campLevel: Int = 0,
    val storageLevel: Int = 0,
    val kitchenLevel: Int = 0,
    val workshopLevel: Int = 0
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
        fun newGame(childId: String): LunarimGameState = LunarimGameState(
            childId = childId,
            hasStarted = true
        )

        fun resetForChild(childId: String): LunarimGameState = LunarimGameState(
            childId = childId,
            hasStarted = false
        )
    }
}
