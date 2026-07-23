package de.meson_labs.luna_coin.lunarim.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import de.meson_labs.luna_coin.lunarim.models.LunarimGameState
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Firestore-Speicher für Lunarim.
 *
 * Firestore-Struktur:
 *
 * families/{familyId}/lunarimSaves/{childId}
 *
 * Der vollständige LunarimGameState wird als JSON gespeichert. Dadurch bleiben
 * neue Felder durch Kotlin Serialization abwärtskompatibel und Firestore muss
 * die verschachtelten Lunarim-Modelle nicht selbst umwandeln.
 */
class LunarimCloudStorage(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val familyId: String = DEFAULT_FAMILY_ID
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val savesCollection
        get() = firestore
            .collection("families")
            .document(familyId)
            .collection(COLLECTION_LUNARIM_SAVES)

    /**
     * Lädt den Spielstand eines Benutzers aus Firestore.
     *
     * Gibt null zurück, wenn noch kein Cloud-Spielstand existiert.
     * Firestore-Ausnahmen werden nicht verschluckt, damit der Aufrufer
     * bewusst auf das lokale Backup zurückfallen kann.
     */
    suspend fun load(childId: String): LunarimGameState? {
        if (childId.isBlank()) return null

        val snapshot = savesCollection
            .document(childId)
            .get()
            .await()

        if (!snapshot.exists()) return null

        val storedJson = snapshot.getString(FIELD_GAME_STATE_JSON)
            ?: return null

        return json.decodeFromString<LunarimGameState>(storedJson)
    }

    /**
     * Speichert einen vollständigen Spielstand in Firestore.
     */
    suspend fun save(gameState: LunarimGameState) {
        require(gameState.childId.isNotBlank()) {
            "Für einen Lunarim-Spielstand wird eine gültige Child-ID benötigt."
        }

        savesCollection
            .document(gameState.childId)
            .set(gameState.toFirestoreMap())
            .await()
    }

    /**
     * Löscht den Cloud-Spielstand eines Benutzers.
     */
    suspend fun delete(childId: String) {
        if (childId.isBlank()) return

        savesCollection
            .document(childId)
            .delete()
            .await()
    }

    /**
     * Speichert Käufer und Verkäufer atomar in einer Firestore-Transaktion.
     *
     * Diese Methode wird später vom Lunarim-Shop nach einer erfolgreich
     * berechneten Spieler-zu-Spieler-Transaktion aufgerufen. Entweder werden
     * beide Spielstände gespeichert oder keiner von beiden.
     */
    suspend fun savePlayerTrade(
        buyerState: LunarimGameState,
        sellerState: LunarimGameState
    ) {
        require(buyerState.childId.isNotBlank()) {
            "Der Käufer benötigt eine gültige Child-ID."
        }
        require(sellerState.childId.isNotBlank()) {
            "Der Verkäufer benötigt eine gültige Child-ID."
        }
        require(buyerState.childId != sellerState.childId) {
            "Käufer und Verkäufer müssen unterschiedliche Benutzer sein."
        }

        val buyerRef = savesCollection.document(buyerState.childId)
        val sellerRef = savesCollection.document(sellerState.childId)

        firestore.runTransaction { transaction ->
            transaction.set(
                buyerRef,
                buyerState.toFirestoreMap()
            )
            transaction.set(
                sellerRef,
                sellerState.toFirestoreMap()
            )
        }.await()
    }

    private fun LunarimGameState.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            FIELD_CHILD_ID to childId,
            FIELD_HAS_STARTED to hasStarted,
            FIELD_GAME_STATE_JSON to json.encodeToString(this),
            FIELD_UPDATED_AT to Timestamp.now(),
            FIELD_SAVE_VERSION to CURRENT_SAVE_VERSION
        )
    }

    private companion object {
        const val DEFAULT_FAMILY_ID = "haas_family_demo"
        const val COLLECTION_LUNARIM_SAVES = "lunarimSaves"

        const val FIELD_CHILD_ID = "childId"
        const val FIELD_HAS_STARTED = "hasStarted"
        const val FIELD_GAME_STATE_JSON = "gameStateJson"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val FIELD_SAVE_VERSION = "saveVersion"

        const val CURRENT_SAVE_VERSION = 1
    }
}
