package de.meson_labs.luna_coin.lunarim.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import de.meson_labs.luna_coin.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.Child

private const val MAP_ASPECT_RATIO = 1.5f

private data class LunarimMapLocation(
    val id: String,
    val name: String,
    val description: String,
    val normalizedX: Float,
    val normalizedY: Float,
    val icon: ImageVector
)

private val firstRegionLocations = listOf(
    LunarimMapLocation(
        id = "whispering_forest",
        name = "Der Flüsterwald",
        description = "Ein dichter Wald, in dem selbst der Wind wie eine fremde Stimme klingt.",
        normalizedX = 0.36f,
        normalizedY = 0.29f,
        icon = Icons.Default.Forest
    ),
    LunarimMapLocation(
        id = "old_watchtower",
        name = "Der alte Wachturm",
        description = "Eine verlassene Ruine, die weit über das umliegende Land blickt.",
        normalizedX = 0.65f,
        normalizedY = 0.24f,
        icon = Icons.Default.Castle
    ),
    LunarimMapLocation(
        id = "moonlake",
        name = "Der Mondsee",
        description = "Dunkles Wasser spiegelt auch bei Tag einen silbernen Schimmer.",
        normalizedX = 0.25f,
        normalizedY = 0.55f,
        icon = Icons.Default.Water
    ),
    LunarimMapLocation(
        id = "stonepass",
        name = "Der Steinpass",
        description = "Ein schmaler Pfad führt zwischen steilen und verwitterten Felsen hindurch.",
        normalizedX = 0.75f,
        normalizedY = 0.54f,
        icon = Icons.Default.Landscape
    ),
    LunarimMapLocation(
        id = "forgotten_ruins",
        name = "Die vergessenen Ruinen",
        description = "Überwachsene Mauern verbergen die Spuren einer längst vergangenen Siedlung.",
        normalizedX = 0.46f,
        normalizedY = 0.69f,
        icon = Icons.Default.LocationOn
    )
)

@Composable
internal fun LunarimMapScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?
) {
    var selectedLocationId by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedLocation = remember(selectedLocationId) {
        firstRegionLocations.firstOrNull { it.id == selectedLocationId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LunarimMapColors.backgroundTop,
                        LunarimMapColors.backgroundBottom
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MapHeader(selectedChild = selectedChild)

        RegionMapCard(
            locations = firstRegionLocations,
            selectedLocationId = selectedLocationId,
            onLocationClick = { location ->
                selectedLocationId = location.id
            }
        )

        RegionChangeButton()
    }

    selectedLocation?.let { location ->
        LocationDialog(
            location = location,
            onExplore = {
                // Die eigentliche Erkundungslogik und Zufallsereignisse
                // werden in einer späteren Phase ergänzt.
                selectedLocationId = null
            },
            onDismiss = {
                selectedLocationId = null
            }
        )
    }
}

@Composable
private fun MapHeader(
    selectedChild: Child?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = null,
            modifier = Modifier.size(31.dp),
            tint = LunarimMapColors.gold
        )

        Text(
            text = "KARTE VON LUNARIM",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = LunarimMapColors.parchment
        )

        Text(
            text = "GEBIET I · DIE GRENZLANDE",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = LunarimMapColors.goldMuted
        )

        Text(
            text = selectedChild?.name
                ?.takeIf { it.isNotBlank() }
                ?.let { "$it kann fünf Orte dieses Gebiets erkunden." }
                ?: "Fünf Orte dieses Gebiets können erkundet werden.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = LunarimMapColors.secondaryText
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun RegionMapCard(
    locations: List<LunarimMapLocation>,
    selectedLocationId: String?,
    onLocationClick: (LunarimMapLocation) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = LunarimMapColors.goldMuted.copy(alpha = 0.72f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = LunarimMapColors.mapFrame
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LunarimMapColors.cardRaised,
                            LunarimMapColors.card
                        )
                    )
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = LunarimMapColors.gold
                )

                Text(
                    text = "DIE GRENZLANDE",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LunarimMapColors.parchment
                )

                Text(
                    text = "${locations.size} ORTE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LunarimMapColors.goldMuted
                )
            }

            HorizontalDivider(
                color = LunarimMapColors.goldMuted.copy(alpha = 0.36f)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(MAP_ASPECT_RATIO)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LunarimMapColors.mapFrame)
            ) {
                // Ausschließlich die PNG-Ressource wird als Kartenhintergrund verwendet.
                // Es gibt in dieser Datei keine gezeichnete Ersatzkarte mehr.
                Image(
                    painter = painterResource(id = R.drawable.grenzlande_map),
                    contentDescription = "Karte des Gebiets Die Grenzlande",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                locations.forEach { location ->
                    MapLocationMarker(
                        location = location,
                        selected = location.id == selectedLocationId,
                        modifier = Modifier.align(
                            BiasAlignment(
                                horizontalBias = location.normalizedX * 2f - 1f,
                                verticalBias = location.normalizedY * 2f - 1f
                            )
                        ),
                        onClick = {
                            onLocationClick(location)
                        }
                    )
                }
            }

            Text(
                text = "Tippe auf einen markierten Ort, um ihn auszuwählen.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = LunarimMapColors.secondaryText
            )
        }
    }
}

@Composable
private fun MapLocationMarker(
    location: LunarimMapLocation,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    /*
     * Das Kartenbild enthält die sichtbaren Ortsmarkierungen bereits.
     * Diese transparente Fläche liegt exakt darüber und skaliert gemeinsam
     * mit der Karte. Auf Tablets wird nur der Trefferbereich etwas größer.
     */
    BoxWithConstraints(modifier = modifier) {
        val markerSize = if (maxWidth > 600.dp) 68.dp else 54.dp

        Box(
            modifier = Modifier
                .size(markerSize)
                .clip(CircleShape)
                .background(
                    color = if (selected) {
                        LunarimMapColors.gold.copy(alpha = 0.18f)
                    } else {
                        Color.Transparent
                    }
                )
                .clickable(
                    onClickLabel = "${location.name} auswählen",
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = location.icon,
                    contentDescription = location.name,
                    modifier = Modifier.size(markerSize * 0.52f),
                    tint = LunarimMapColors.gold
                )
            }
        }
    }
}

@Composable
private fun RegionChangeButton() {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        enabled = false,
        onClick = {},
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = LunarimMapColors.goldMuted.copy(alpha = 0.34f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LunarimMapColors.card.copy(alpha = 0.78f),
            contentColor = LunarimMapColors.parchment,
            disabledContainerColor = LunarimMapColors.card.copy(alpha = 0.58f),
            disabledContentColor = LunarimMapColors.parchment.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChangeCircle,
            contentDescription = null,
            modifier = Modifier.size(27.dp)
        )

        Column(
            modifier = Modifier.padding(start = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "GEBIET WECHSELN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Weitere Gebiete werden später freigeschaltet.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LocationDialog(
    location: LunarimMapLocation,
    onExplore: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        containerColor = LunarimMapColors.card,
        tonalElevation = 12.dp,
        icon = {
            Icon(
                imageVector = location.icon,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = LunarimMapColors.gold
            )
        },
        title = {
            Text(
                text = location.name.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = LunarimMapColors.parchment
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(
                    color = LunarimMapColors.goldMuted.copy(alpha = 0.42f)
                )

                Text(
                    text = location.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = LunarimMapColors.secondaryText
                )

                Text(
                    text = "Möchtest du diesen Ort erkunden?",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = LunarimMapColors.parchment
                )
            }
        },
        confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onExplore,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LunarimMapColors.gold,
                    contentColor = LunarimMapColors.buttonText
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null
                )
                Text(
                    text = "ERKUNDEN",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss,
                border = BorderStroke(
                    width = 1.dp,
                    color = LunarimMapColors.goldMuted.copy(alpha = 0.62f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LunarimMapColors.parchment
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
                Text(
                    text = "ABBRECHEN",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    )
}

private object LunarimMapColors {
    val backgroundTop = Color(0xFF121518)
    val backgroundBottom = Color(0xFF090B0D)

    val card = Color(0xFF171A1D)
    val cardRaised = Color(0xFF22262A)
    val mapFrame = Color(0xFF111315)

    val gold = Color(0xFFD1AC62)
    val goldMuted = Color(0xFF9F824C)
    val parchment = Color(0xFFE8E0CF)
    val secondaryText = Color(0xFFB8B1A4)
    val buttonText = Color(0xFF1A1712)

}
