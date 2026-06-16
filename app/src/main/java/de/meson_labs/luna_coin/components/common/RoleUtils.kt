// components/common/RoleExtensions.kt
package de.meson_labs.luna_coin.components.common

import de.meson_labs.luna_coin.models.UserRole

fun UserRole.toDisplayText(): String {
    return when (this) {
        UserRole.CHILD -> "Kind"
        UserRole.PARENT -> "Eltern"
        UserRole.ADMIN -> "Admin"
    }
}