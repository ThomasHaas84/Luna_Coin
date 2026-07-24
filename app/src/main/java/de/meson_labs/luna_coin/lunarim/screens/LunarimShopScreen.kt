package de.meson_labs.luna_coin.lunarim.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.lunarim.data.LunarimItems
import de.meson_labs.luna_coin.lunarim.models.LunarimItem
import de.meson_labs.luna_coin.lunarim.models.LunarimItemType
import de.meson_labs.luna_coin.models.Child

private enum class LunarimTradeArea {
    LOCAL,
    REMOTE
}

private enum class LunarimTradeAction {
    BUY,
    SELL
}

@Composable
internal fun LunarimShopScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onSaveCurrentPlayer: () -> Unit
) {
    var selectedArea by rememberSaveable { mutableStateOf(LunarimTradeArea.LOCAL) }
    var selectedAction by rememberSaveable { mutableStateOf(LunarimTradeAction.BUY) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LunarimShopColors.backgroundTop,
                        LunarimShopColors.backgroundBottom
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LunarimShopHeader(selectedChild = selectedChild)

        LunarimSelectionRow(
            leftText = "LOKAL",
            rightText = "FERNHANDEL",
            leftSelected = selectedArea == LunarimTradeArea.LOCAL,
            onLeftClick = { selectedArea = LunarimTradeArea.LOCAL },
            onRightClick = { selectedArea = LunarimTradeArea.REMOTE }
        )

        LunarimSelectionRow(
            leftText = "KAUFEN",
            rightText = "VERKAUFEN",
            leftSelected = selectedAction == LunarimTradeAction.BUY,
            onLeftClick = { selectedAction = LunarimTradeAction.BUY },
            onRightClick = { selectedAction = LunarimTradeAction.SELL }
        )

        when {
            selectedArea == LunarimTradeArea.LOCAL &&
                    selectedAction == LunarimTradeAction.BUY -> {
                LocalBuyContent(
                    modifier = Modifier.weight(1f),
                    items = LunarimItems.localShopItems,
                    onBuyClick = {
                        // Phase 1: Noch keine echte Transaktion.
                        // Sobald Kauf und Inventar eingebaut sind, wird nach
                        // erfolgreichem Kauf onSaveCurrentPlayer() aufgerufen.
                    }
                )
            }

            selectedArea == LunarimTradeArea.LOCAL &&
                    selectedAction == LunarimTradeAction.SELL -> {
                ShopPlaceholderContent(
                    modifier = Modifier.weight(1f),
                    symbol = "⚖",
                    title = "LOKAL VERKAUFEN",
                    text = "Hier werden später alle verkaufbaren Gegenstände aus deinem Lunarim-Inventar angezeigt. Der Händler zahlt einen festen Anteil des regulären Kaufpreises."
                )
            }

            selectedArea == LunarimTradeArea.REMOTE &&
                    selectedAction == LunarimTradeAction.BUY -> {
                ShopPlaceholderContent(
                    modifier = Modifier.weight(1f),
                    symbol = "🌍",
                    title = "FERNHANDEL KAUFEN",
                    text = "Hier erscheinen später Angebote anderer Lunarim-Spieler mit Verkäufer, Stückzahl und Preis in Luna Silver."
                )
            }

            else -> {
                ShopPlaceholderContent(
                    modifier = Modifier.weight(1f),
                    symbol = "📜",
                    title = "FERNHANDEL VERKAUFEN",
                    text = "Hier kannst du später eigene Gegenstände auswählen, eine Menge und einen Preis festlegen und das Angebot für andere Spieler veröffentlichen."
                )
            }
        }
    }
}

@Composable
private fun LunarimShopHeader(selectedChild: Child?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, LunarimShopColors.gold.copy(alpha = 0.78f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LunarimShopColors.stoneRaised,
                            LunarimShopColors.stone
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "HANDEL",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LunarimShopColors.parchment,
                textAlign = TextAlign.Center
            )

            Text(
                text = selectedChild?.name
                    ?.takeIf { it.isNotBlank() }
                    ?.let { "Händlerkonto von $it" }
                    ?: "Lunarim-Händler",
                style = MaterialTheme.typography.bodyMedium,
                color = LunarimShopColors.secondaryText,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 5.dp),
                color = LunarimShopColors.goldMuted.copy(alpha = 0.55f)
            )

            Text(
                text = "Alle Preise werden in Luna Silver bezahlt",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = LunarimShopColors.gold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LunarimSelectionRow(
    leftText: String,
    rightText: String,
    leftSelected: Boolean,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LunarimSelectionButton(
            modifier = Modifier.weight(1f),
            text = leftText,
            selected = leftSelected,
            onClick = onLeftClick
        )

        LunarimSelectionButton(
            modifier = Modifier.weight(1f),
            text = rightText,
            selected = !leftSelected,
            onClick = onRightClick
        )
    }
}

@Composable
private fun LunarimSelectionButton(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier.height(46.dp),
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                LunarimShopColors.gold
            } else {
                LunarimShopColors.goldMuted.copy(alpha = 0.45f)
            }
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) {
                LunarimShopColors.selectedStone
            } else {
                LunarimShopColors.stone.copy(alpha = 0.92f)
            },
            contentColor = if (selected) {
                LunarimShopColors.parchment
            } else {
                LunarimShopColors.secondaryText
            }
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun LocalBuyContent(
    modifier: Modifier,
    items: List<LunarimItem>,
    onBuyClick: (LunarimItem) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "WAREN DES HÄNDLERS",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 1.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = LunarimShopColors.gold,
                textAlign = TextAlign.Center
            )
        }

        items(
            items = items,
            key = { it.id }
        ) { item ->
            LunarimShopItemCard(
                item = item,
                onBuyClick = { onBuyClick(item) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Phase 1: Die Schaltflächen verändern noch kein Inventar und kein Luna-Silver-Guthaben.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = LunarimShopColors.secondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LunarimShopItemCard(
    item: LunarimItem,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, LunarimShopColors.goldMuted.copy(alpha = 0.58f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            LunarimShopColors.stoneRaised,
                            LunarimShopColors.stone
                        )
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(
                            color = LunarimShopColors.iconStone,
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.shopSymbol(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LunarimShopColors.parchment,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = item.shopCategory().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LunarimShopColors.gold
                    )

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LunarimShopColors.secondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = LunarimShopColors.goldMuted.copy(alpha = 0.32f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Vorrat: ${LunarimItems.getLocalShopStock(item.id)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LunarimShopColors.secondaryText
                    )
                    Text(
                        text = "${item.buyPriceSilver} Luna Silver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LunarimShopColors.silverText
                    )
                }

                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LunarimShopColors.gold,
                        contentColor = LunarimShopColors.buttonText
                    )
                ) {
                    Text(
                        text = "KAUFEN",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopPlaceholderContent(
    modifier: Modifier,
    symbol: String,
    title: String,
    text: String
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, LunarimShopColors.goldMuted.copy(alpha = 0.62f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                LunarimShopColors.stoneRaised,
                                LunarimShopColors.stone
                            )
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.displaySmall
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LunarimShopColors.parchment,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    color = LunarimShopColors.goldMuted.copy(alpha = 0.48f)
                )

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LunarimShopColors.secondaryText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "WIRD IN EINER SPÄTEREN PHASE AKTIVIERT",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LunarimShopColors.gold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun LunarimItem.shopCategory(): String =
    when (type) {
        LunarimItemType.WEAPON -> "Waffe"
        LunarimItemType.AMMUNITION -> "Munition"
        LunarimItemType.ARMOR -> "Rüstung"
        LunarimItemType.SHIELD -> "Schild"
        LunarimItemType.CONSUMABLE -> {
            when (id) {
                "travel_rations", "water_flask" -> "Nahrung"
                "healing_potion", "minor_healing_potion" -> "Trank"
                "torch" -> "Werkzeug"
                else -> "Verbrauchsgegenstand"
            }
        }
        LunarimItemType.MATERIAL -> "Werkzeug"
        LunarimItemType.RAW_MATERIAL -> {
            when (id) {
                "healing_herbs", "moon_herb" -> "Zutat"
                else -> "Rohstoff"
            }
        }
        LunarimItemType.QUEST_ITEM -> "Questgegenstand"
    }

private fun LunarimItem.shopSymbol(): String =
    when (id) {
        "travel_rations" -> "🍞"
        "water_flask" -> "💧"
        "healing_herbs", "moon_herb" -> "🌿"
        "healing_potion", "minor_healing_potion" -> "🧪"
        "raw_wood", "firewood" -> "🪵"
        "raw_iron", "iron_chunk", "iron_ore", "copper_ore" -> "⛏"
        "simple_rope" -> "🪢"
        "torch" -> "🔥"
        "simple_dagger" -> "🗡"
        "cloth_clothes" -> "🥋"
        else -> when (type) {
            LunarimItemType.WEAPON -> "⚔"
            LunarimItemType.AMMUNITION -> "➶"
            LunarimItemType.ARMOR -> "🛡"
            LunarimItemType.SHIELD -> "🛡"
            LunarimItemType.CONSUMABLE -> "◈"
            LunarimItemType.MATERIAL -> "⚒"
            LunarimItemType.RAW_MATERIAL -> "◆"
            LunarimItemType.QUEST_ITEM -> "✦"
        }
    }

private object LunarimShopColors {
    val backgroundTop = Color(0xFF111315)
    val backgroundBottom = Color(0xFF090A0C)
    val stone = Color(0xFF191C1F)
    val stoneRaised = Color(0xFF252A2E)
    val selectedStone = Color(0xFF30363A)
    val iconStone = Color(0xFF111417)
    val gold = Color(0xFFD1AC62)
    val goldMuted = Color(0xFF9F824C)
    val parchment = Color(0xFFE8E0CF)
    val secondaryText = Color(0xFFB8B1A4)
    val silverText = Color(0xFFD9DEE2)
    val buttonText = Color(0xFF1A1712)
}
