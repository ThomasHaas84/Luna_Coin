package de.meson_labs.luna_coin.lunarim.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.meson_labs.luna_coin.models.Child

@Composable
internal fun LunarimShopScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?
) {
    LunarimPlaceholderScreen(
        modifier = modifier.fillMaxSize(),
        title = "Shop",
        symbol = "🛒",
        text = selectedChild?.name
            ?.takeIf { name -> name.isNotBlank() }
            ?.let { name ->
                "Hier kann $name später Gegenstände kaufen, anbieten und auf dem Familienmarkt handeln."
            }
            ?: "Hier entstehen später der Systemshop und der Familienmarkt."
    )
}
