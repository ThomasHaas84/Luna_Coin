package de.meson_labs.luna_coin.viewmodel

enum class CelebrationType {
    HIGHSCORE,
    LEVEL_UP
}

data class CelebrationEvent(
    val id: String,
    val type: CelebrationType,
    val title: String,
    val subtitle: String,
    val details: String,
    val footer: String
)