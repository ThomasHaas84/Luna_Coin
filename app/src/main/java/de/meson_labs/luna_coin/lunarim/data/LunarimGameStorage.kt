package de.meson_labs.luna_coin.lunarim.data

import android.content.Context
import de.meson_labs.luna_coin.lunarim.models.LunarimGameState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Lokaler Lunarim-Spielstand pro Luna-Coin-Benutzer.
 *
 * Die Speicherung ist bewusst vom restlichen Luna-Coin-Speicher getrennt,
 * damit Lunarim schrittweise erweitert werden kann. Sobald später eine
 * Firestore-Synchronisierung hinzukommt, kann diese Klasse intern ersetzt
 * werden, ohne die Screens erneut umzubauen.
 */
class LunarimGameStorage(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun hasGame(childId: String): Boolean {
        val key = gameKey(childId)
        return preferences.contains(key) && loadGame(childId)?.hasStarted == true
    }

    fun loadGame(childId: String): LunarimGameState? {
        val rawJson = preferences.getString(gameKey(childId), null)
            ?: return null

        return runCatching {
            json.decodeFromString<LunarimGameState>(rawJson)
        }.getOrNull()
    }

    fun createNewGame(childId: String): LunarimGameState {
        val gameState = LunarimGameState.newGame(childId)
        saveGame(gameState)
        return gameState
    }

    fun saveGame(gameState: LunarimGameState) {
        preferences.edit()
            .putString(
                gameKey(gameState.childId),
                json.encodeToString(gameState)
            )
            .apply()
    }

    fun deleteGame(childId: String) {
        preferences.edit()
            .remove(gameKey(childId))
            .apply()
    }

    private fun gameKey(childId: String): String =
        "$GAME_KEY_PREFIX${childId.trim()}"

    private companion object {
        const val PREFERENCES_NAME = "lunarim_game_saves"
        const val GAME_KEY_PREFIX = "lunarim_game_"
    }
}
