package de.meson_labs.luna_coin.lunarim.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LunarimCharacterColors.backgroundTop,
                        LunarimCharacterColors.backgroundBottom
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = 18.dp
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 860.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CharacterScreenHeader()

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
                title = "AUSRÜSTUNG",
                text = "Hier werden angelegte Waffen, Rüstungen, Schilde, Schmuck und ihr Zustand angezeigt."
            )

            CharacterInfoCard(
                title = "RESISTENZEN",
                text = "Physische, elementare und magische Widerstände werden aus Attributen, Ausrüstung und aktiven Effekten berechnet."
            )

            CharacterInfoCard(
                title = "AKTIVE EFFEKTE",
                text = if (gameState?.character?.activeEffects.isNullOrEmpty()) {
                    "Aktuell sind keine positiven oder negativen Effekte aktiv."
                } else {
                    "${gameState?.character?.activeEffects?.size ?: 0} aktive Effekte"
                }
            )

            CharacterInfoCard(
                title = "LUNARIM-SKILLS",
                text = "Kampf-, Handwerks-, Überlebens- und Magiefähigkeiten werden hier ergänzt."
            )
        }
    }
}

@Composable
private fun CharacterScreenHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 2.dp,
                bottom = 4.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "CHARAKTER",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = LunarimCharacterColors.parchment
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.72f),
            thickness = 1.dp,
            color = LunarimCharacterColors.runeGold.copy(alpha = 0.66f)
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

    LunarimCharacterCard(
        accentColor = LunarimCharacterColors.runeGold
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                fontWeight = FontWeight.Bold,
                color = LunarimCharacterColors.parchment
            )

            Text(
                text = "LUNARIM-CHARAKTER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LunarimCharacterColors.runeGold
            )
        }
    }
}

@Composable
private fun CharacterImage(
    @DrawableRes avatarRes: Int?,
    characterName: String
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        /*
         * Das bisherige Seitenverhältnis war auf Tablets zu flach.
         * Der Bildbereich erhält deshalb dort deutlich mehr Höhe.
         * ContentScale.Fit stellt sicher, dass oben und unten nichts
         * abgeschnitten wird.
         */
        val imageHeight = if (maxWidth >= 600.dp) {
            470.dp
        } else {
            330.dp
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(LunarimCharacterColors.imageBackground),
            contentAlignment = Alignment.Center
        ) {
            if (avatarRes != null) {
                Image(
                    painter = painterResource(avatarRes),
                    contentDescription = "Charakterbild von $characterName",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Kein Charakterbild",
                    modifier = Modifier.size(112.dp),
                    tint = LunarimCharacterColors.secondaryText
                )
            }
        }
    }
}

@Composable
private fun CharacterLevelCard(
    selectedChild: Child?
) {
    CharacterSectionCard(
        title = "LEVEL UND FORTSCHRITT"
    ) {
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

@Composable
private fun CharacterSkillsCard(
    selectedChild: Child?
) {
    CharacterSectionCard(
        title = "ATTRIBUTE"
    ) {
        CharacterValueRow("Stärke", selectedChild?.strength ?: 1)
        CharacterValueRow("Wahrnehmung", selectedChild?.perception ?: 1)
        CharacterValueRow("Ausdauer", selectedChild?.endurance ?: 1)
        CharacterValueRow("Charisma", selectedChild?.charisma ?: 1)
        CharacterValueRow("Intelligenz", selectedChild?.intelligence ?: 1)
        CharacterValueRow("Beweglichkeit", selectedChild?.agility ?: 1)
        CharacterValueRow("Glück", selectedChild?.luck ?: 1)
    }
}

@Composable
private fun LunarimStatusCard(
    gameState: LunarimGameState?
) {
    val character = gameState?.character

    CharacterSectionCard(
        title = "AKTUELLER ZUSTAND"
    ) {
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

@Composable
private fun CharacterSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    LunarimCharacterCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LunarimCharacterColors.parchment
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = LunarimCharacterColors.runeGoldMuted.copy(alpha = 0.58f)
            )

            content()
        }
    }
}

@Composable
private fun CharacterInfoCard(
    title: String,
    text: String
) {
    LunarimCharacterCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LunarimCharacterColors.parchment
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = LunarimCharacterColors.runeGoldMuted.copy(alpha = 0.58f)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = LunarimCharacterColors.secondaryText
            )
        }
    }
}

@Composable
private fun LunarimCharacterCard(
    accentColor: Color = LunarimCharacterColors.runeGoldMuted,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.72f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = LunarimCharacterColors.darkStone
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LunarimCharacterColors.darkStoneRaised,
                            LunarimCharacterColors.darkStone
                        )
                    )
                )
        ) {
            content()
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
            style = MaterialTheme.typography.bodyLarge,
            color = LunarimCharacterColors.secondaryText
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = LunarimCharacterColors.runeGold
        )
    }
}

private object LunarimCharacterColors {
    val backgroundTop = Color(0xFF0B0C0E)
    val backgroundBottom = Color(0xFF151719)
    val darkStone = Color(0xFF17191C)
    val darkStoneRaised = Color(0xFF24282C)
    val imageBackground = Color(0xFF090A0B)
    val runeGold = Color(0xFFD1AC62)
    val runeGoldMuted = Color(0xFF9F824C)
    val parchment = Color(0xFFE8E0CF)
    val secondaryText = Color(0xFFB8B1A4)
}
