package de.meson_labs.luna_coin.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import de.meson_labs.luna_coin.models.LunaCoinData
import kotlinx.coroutines.tasks.await

class FirestoreRepository : DataRepository {

    private val db = FirebaseFirestore.getInstance()
    private val familyId = "haas_family_demo"

    private val dataRef = db.collection("families")
        .document(familyId)
        .collection("data")
        .document("main")

    override suspend fun loadData(): LunaCoinData? {
        return try {
            val snapshot = dataRef.get().await()

            if (!snapshot.exists()) {
                println("📭 Dokument existiert noch nicht in Firestore")
                return null
            }

            // Korrigierte Version
            val data = snapshot.toObject(LunaCoinData::class.java)

            if (data != null && data.children.isNotEmpty()) {
                println("✅ Firestore Daten erfolgreich geladen (${data.children.size} Kinder)")
                return data
            } else {
                println("⚠️ Dokument existiert, aber Daten sind leer oder ungültig")
                return null
            }

        } catch (e: Exception) {
            println("❌ Fehler beim Laden aus Firestore: ${e.message}")
            // e.printStackTrace()   // bei Bedarf aktivieren
            null
        }
    }

    override suspend fun saveData(data: LunaCoinData) {
        try {
            dataRef.set(data).await()
            println("✅ Daten erfolgreich gespeichert (Coins: ${data.children.sumOf { it.coins }})")
        } catch (e: Exception) {
            println("❌ Fehler beim Speichern: ${e.message}")
            e.printStackTrace()
        }
    }
}