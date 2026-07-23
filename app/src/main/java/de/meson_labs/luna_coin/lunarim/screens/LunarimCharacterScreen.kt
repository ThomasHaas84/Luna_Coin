package de.meson_labs.luna_coin.lunarim.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.lunarim.models.LunarimGameState
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaItemCatalog

@Composable
internal fun LunarimCharacterScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    gameState: LunarimGameState? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CharacterAvatarCard(
            selectedChild = selectedChild
        )

        CharacterLevelCard(
            selectedChild = selectedChild
        )

        CharacterSkillsCard(
            selectedChild = selectedChild
        )

        LunarimStatusCard(
            gameState = gameState
        )

        CharacterInfoCard(
            title = "Ausrüstung",
            text = "Hier werden angelegte Waffen, Rüstungen, Schilde, Schmuck und ihr Zustand angezeigt."
        )

        CharacterInfoCard(
            title = "Resistenzen",
            text = "Physische, elementare und magische Widerstände werden aus Attributen, Ausrüstung und aktiven Effekten berechnet."
        )

        CharacterInfoCard(
            title = "Aktive Effekte",
            text = if (gameState?.character?.activeEffects.isNullOrEmpty()) {
                "Aktuell sind keine positiven oder negativen Effekte aktiv."
            } else {
                "${gameState?.character?.activeEffects?.size ?: 0} aktive Effekte"
            }
        )

        CharacterInfoCard(
            title = "Lunarim-Skills",
            text = "Kampf-, Handwerks-, Überlebens- und Magiefähigkeiten werden hier ergänzt."
        )
    }
}

@Composable
private fun CharacterAvatarCard(
    selectedChild: Child?
) {
    val avatarRes = selectedChild
        ?.profileImageItem
        ?.let { item ->
            runCatching {
                LunaItemCatalog.getDefinition(item).lunaImageRes
            }.getOrNull()
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CharacterImage(
                avatarRes = avatarRes,
                characterName = selectedChild?.name.orEmpty()
            )

            Text(
                text = selectedChild?.name
                    ?.ifBlank { "Kein Benutzer ausgewählt" }
                    ?: "Kein Benutzer ausgewählt",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Lunarim-Charakter",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CharacterImage(
    @DrawableRes avatarRes: Int?,
    characterName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .aspectRatio(1.25f)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarRes != null) {
            Image(
                painter = painterResource(avatarRes),
                contentDescription = "Charakterbild von $characterName",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Kein Charakterbild",
                modifier = Modifier.size(112.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CharacterLevelCard(
    selectedChild: Child?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Level und Fortschritt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            CharacterValueRow(
                label = "Level",
                value = "${selectedChild?.level ?: 1}"
            )

            CharacterValueRow(
                label = "Erfahrung",
                value = "${selectedChild?.experience ?: 0} EP"
            )

            CharacterValueRow(
                label = "Freie Skillpunkte",
                value = "${selectedChild?.availableSkillPoints ?: 0}"
            )
        }
    }
}

@Composable
private fun CharacterSkillsCard(
    selectedChild: Child?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Attribute",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            CharacterValueRow("Stärke", selectedChild?.strength ?: 1)
            CharacterValueRow("Wahrnehmung", selectedChild?.perception ?: 1)
            CharacterValueRow("Ausdauer", selectedChild?.endurance ?: 1)
            CharacterValueRow("Charisma", selectedChild?.charisma ?: 1)
            CharacterValueRow("Intelligenz", selectedChild?.intelligence ?: 1)
            CharacterValueRow("Beweglichkeit", selectedChild?.agility ?: 1)
            CharacterValueRow("Glück", selectedChild?.luck ?: 1)
        }
    }
}

@Composable
private fun LunarimStatusCard(
    gameState: LunarimGameState?
) {
    val character = gameState?.character

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Aktueller Zustand",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            CharacterValueRow(
                label = "Lebenspunkte",
                value = "${character?.currentHealth ?: 100}"
            )

            CharacterValueRow(
                label = "Mana",
                value = "${character?.currentMana ?: 50}"
            )

            CharacterValueRow(
                label = "Aktionspunkte",
                value = "${character?.currentActionPoints ?: 5}"
            )

            CharacterValueRow(
                label = "Inventarplätze",
                value = "${character?.inventory?.size ?: 0}"
            )
        }
    }
}

@Composable
private fun CharacterInfoCard(
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

            HorizontalDivider()

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CharacterValueRow(
    label: String,
    value: Int
) {
    CharacterValueRow(
        label = label,
        value = value.toString()
    )
}

@Composable
private fun CharacterValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
