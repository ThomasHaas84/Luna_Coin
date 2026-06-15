package de.meson_labs.luna_coin.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

open class FirebaseModel {
    open var id: String = ""                    // ← open hinzugefügt
    open var familyId: String = ""              // ← open hinzugefügt

    @ServerTimestamp
    open var createdAt: Date? = null            // ← open hinzugefügt

    @ServerTimestamp
    open var updatedAt: Date? = null            // ← open hinzugefügt
}