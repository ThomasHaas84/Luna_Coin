package de.meson_labs.luna_coin.lunarim.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.lunarim.models.LunarimGameState
import de.meson_labs.luna_coin.models.Child

@Composable
internal fun LunarimCampScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    gameState: LunarimGameState?,
    onUpgradeCamp: () -> Unit
) {
    val campLevel = gameState?.camp?.campLevel ?: 0
    val ownerName = selectedChild?.name
        ?.takeIf { it.isNotBlank() }
        ?: "Unbekannt"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LunarimCampColors.backgroundTop,
                        LunarimCampColors.backgroundBottom
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 14.dp,
                vertical = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CampLevelHeader(
            campLevel = campLevel,
            ownerName = ownerName
        )

        CampImageCard(
            campLevel = campLevel
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp),
            enabled = gameState?.hasStarted == true,
            onClick = onUpgradeCamp,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LunarimCampColors.gold,
                contentColor = LunarimCampColors.buttonText,
                disabledContainerColor = LunarimCampColors.gold.copy(alpha = 0.28f),
                disabledContentColor = LunarimCampColors.parchment.copy(alpha = 0.40f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            Column(
                modifier = Modifier.padding(start = 10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "LAGER AUSBAUEN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Auf Lagerstufe ${campLevel + 1} erweitern",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        CampSectionCard(
            icon = Icons.Default.Inventory2,
            title = "LAGERINVENTAR"
        ) {
            EmptyCampSection(
                text = "Das Lagerinventar ist noch leer.",
                hint = "Gesammelte Rohstoffe und eingelagerte Gegenstände erscheinen später hier."
            )
        }

        CampSectionCard(
            icon = Icons.Default.HomeWork,
            title = "AKTIVE EINRICHTUNGEN"
        ) {
            EmptyCampSection(
                text = "Noch keine Einrichtungen aktiv.",
                hint = "Lagerfeuer, Werkbank, Küche und weitere Einrichtungen werden hier angezeigt."
            )
        }

        CampSectionCard(
            icon = Icons.Default.AutoAwesome,
            title = "AKTIVE BUFFS"
        ) {
            EmptyCampSection(
                text = "Momentan sind keine Lager-Buffs aktiv.",
                hint = "Boni aus Einrichtungen, Nahrung und Lagerausbauten erscheinen später hier."
            )
        }
    }
}

@Composable
private fun CampLevelHeader(
    campLevel: Int,
    ownerName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "LAGERSTUFE",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = LunarimCampColors.goldMuted
        )

        Text(
            text = "LVL $campLevel",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = LunarimCampColors.parchment
        )

        Text(
            text = "Lager von $ownerName",
            style = MaterialTheme.typography.bodyMedium,
            color = LunarimCampColors.secondaryText
        )

        Text(
            text = "Bewohner: 1",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = LunarimCampColors.gold
        )
    }
}

@Composable
private fun CampImageCard(
    campLevel: Int
) {
    val context = LocalContext.current

    /*
     * Lädt automatisch lvl_0.jpg, lvl_1.jpg, lvl_2.jpg usw.
     * Die Dateien gehören nach:
     * app/src/main/res/drawable/
     */
    val campImageResId = remember(campLevel, context.packageName) {
        context.resources.getIdentifier(
            "lvl_$campLevel",
            "drawable",
            context.packageName
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = LunarimCampColors.goldMuted.copy(alpha = 0.72f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = LunarimCampColors.card
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        if (campImageResId != 0) {
            Image(
                painter = painterResource(campImageResId),
                contentDescription = "Lager auf Stufe $campLevel",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 210.dp,
                        max = 390.dp
                    ),
                /*
                 * Fit verhindert auf Tablets das Abschneiden des oberen und
                 * unteren Bildbereichs. Das komplette Lager bleibt sichtbar.
                 */
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 230.dp)
                    .background(LunarimCampColors.cardRaised)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Das Lagerbild lvl_$campLevel.jpg wurde nicht gefunden.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LunarimCampColors.secondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CampSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = LunarimCampColors.goldMuted.copy(alpha = 0.48f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = LunarimCampColors.card
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LunarimCampColors.cardRaised,
                            LunarimCampColors.card
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LunarimCampColors.gold,
                    modifier = Modifier.size(25.dp)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LunarimCampColors.parchment
                )
            }

            HorizontalDivider(
                color = LunarimCampColors.goldMuted.copy(alpha = 0.38f)
            )

            content()
        }
    }
}

@Composable
private fun EmptyCampSection(
    text: String,
    hint: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = LunarimCampColors.parchment
        )

        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = LunarimCampColors.secondaryText
        )
    }
}

private object LunarimCampColors {
    val backgroundTop = Color(0xFF121518)
    val backgroundBottom = Color(0xFF090B0D)
    val card = Color(0xFF171A1D)
    val cardRaised = Color(0xFF22262A)
    val gold = Color(0xFFD1AC62)
    val goldMuted = Color(0xFF9F824C)
    val parchment = Color(0xFFE8E0CF)
    val secondaryText = Color(0xFFB8B1A4)
    val buttonText = Color(0xFF1A1712)
}
