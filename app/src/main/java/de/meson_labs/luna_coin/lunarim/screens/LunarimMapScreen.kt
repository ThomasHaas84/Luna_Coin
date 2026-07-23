package de.meson_labs.luna_coin.lunarim.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.meson_labs.luna_coin.models.Child

@Composable
internal fun LunarimMapScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?
) {
    LunarimPlaceholderScreen(
        modifier = modifier.fillMaxSize(),
        title = "Karte",
        symbol = "🗺",
        text = selectedChild?.name
            ?.takeIf { name -> name.isNotBlank() }
            ?.let { name ->
                "Hier erkundet $name später verschiedene Gebiete und erlebt Zufallsereignisse."
            }
            ?: "Hier werden später Gebiete erkundet und Zufallsereignisse ausgelöst."
    )
}
