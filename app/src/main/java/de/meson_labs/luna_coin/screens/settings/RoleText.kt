package de.meson_labs.luna_coin.screens.settings

import de.meson_labs.luna_coin.models.UserRole

fun roleText(role: UserRole): String {
    return when (role) {
        UserRole.CHILD -> "Kind"
        UserRole.PARENT -> "Eltern"
        UserRole.ADMIN -> "Admin"
    }
}