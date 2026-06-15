package de.meson_labs.luna_coin

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Firebase initialisieren
        FirebaseApp.initializeApp(this)

        // Firestore Offline-Persistenz aktivieren
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}