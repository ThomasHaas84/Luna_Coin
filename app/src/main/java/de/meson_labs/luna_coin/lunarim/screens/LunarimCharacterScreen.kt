package de.meson_labs.luna_coin.lunarim.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.meson_labs.luna_coin.models.Child

@Composable
internal fun LunarimCharacterScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?
) {
    LunarimPlaceholderScreen(
        modifier = modifier.fillMaxSize(),
        title = "Charakter",
        symbol = "🐶",
        text = selectedChild?.name
            ?.takeIf { name -> name.isNotBlank() }
            ?.let { name ->
                "Hier erscheinen später der LunaME-Charakter von $name, das Level, die Hauptskills und die Lunarim-Unterskills."
            }
            ?: "Hier erscheinen später LunaME, Level, Hauptskills und Lunarim-Unterskills."
    )
}
