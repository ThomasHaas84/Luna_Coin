package de.meson_labs.luna_coin.lunarim

internal enum class LunarimDestination(
    val title: String,
    val symbol: String
) {
    CAMP(
        title = "Lager",
        symbol = "⛺"
    ),
    CHARACTER(
        title = "Charakter",
        symbol = "🐶"
    ),
    SHOP(
        title = "Shop",
        symbol = "🛒"
    ),
    MAP(
        title = "Karte",
        symbol = "🗺"
    )
}
