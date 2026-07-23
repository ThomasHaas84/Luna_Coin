package de.meson_labs.luna_coin.lunarim.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.Child

@Composable
internal fun LunarimCharacterScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Charakter",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = selectedChild?.name?.ifBlank { "Kein Benutzer ausgewählt" }
                ?: "Kein Benutzer ausgewählt",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CharacterCard(
            title = "LunaME-Charakter",
            text = "Hier wird später der Avatar aus LunaME inklusive Level angezeigt."
        )

        CharacterCard(
            title = "Attribute",
            text = "Stärke, Wahrnehmung, Ausdauer, Charisma, Intelligenz, Beweglichkeit und Glück."
        )

        CharacterCard(
            title = "Ausrüstung",
            text = "Waffen, Rüstung, Schmuck sowie alle angelegten Gegenstände."
        )

        CharacterCard(
            title = "Resistenzen",
            text = "Physische, elementare und magische Resistenzen werden hier berechnet."
        )

        CharacterCard(
            title = "Aktive Effekte",
            text = "Buffs, Debuffs, Krankheiten, Flüche und Segnungen."
        )

        CharacterCard(
            title = "Lunarim-Skills",
            text = "Alle Kampf-, Handwerks-, Überlebens- und Magiefähigkeiten."
        )
    }
}

@Composable
private fun CharacterCard(
    title: String,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
