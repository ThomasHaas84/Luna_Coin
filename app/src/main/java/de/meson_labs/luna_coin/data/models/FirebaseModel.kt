package de.meson_labs.luna_coin.data.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.Date

@Serializable
open class FirebaseModel {

    @Transient
    open var id: String = ""

    @Transient
    open var familyId: String = ""

    @Transient
    @Contextual
    open var createdAt: Date? = null

    @Transient
    @Contextual
    open var updatedAt: Date? = null
}