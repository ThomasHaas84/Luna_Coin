package de.meson_labs.luna_coin.lunarim.data

import android.content.Context
import de.meson_labs.luna_coin.lunarim.models.LunarimGameState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Lokaler Lunarim-Spielstand pro Luna-Coin-Benutzer.
 *
 * Jeder Benutzer erhält über seine Child-ID einen eigenen Eintrag.
 */
class LunarimGameStorage(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(childId: String): LunarimGameState? {
        if (childId.isBlank()) return null

        val storedJson = preferences.getString(
            saveKey(childId),
            null
        ) ?: return null

        return runCatching {
            json.decodeFromString<LunarimGameState>(storedJson)
        }.getOrNull()
    }

    fun hasStartedGame(childId: String): Boolean =
        load(childId)?.hasStarted == true

    fun createNewGame(childId: String): LunarimGameState {
        require(childId.isNotBlank()) {
            "Für einen Lunarim-Spielstand wird eine gültige Child-ID benötigt."
        }

        return LunarimGameState
            .newGame(childId)
            .also(::save)
    }

    fun save(gameState: LunarimGameState) {
        if (gameState.childId.isBlank()) return

        preferences.edit()
            .putString(
                saveKey(gameState.childId),
                json.encodeToString(gameState)
            )
            .commit()
    }

    fun delete(childId: String) {
        if (childId.isBlank()) return

        preferences.edit()
            .remove(saveKey(childId))
            .commit()
    }

    private fun saveKey(childId: String): String =
        "$SAVE_KEY_PREFIX$childId"

    private companion object {
        const val PREFERENCES_NAME = "lunarim_game_saves"
        const val SAVE_KEY_PREFIX = "lunarim_save_"
    }
}
