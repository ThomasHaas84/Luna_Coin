package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaInventoryItem
import de.meson_labs.luna_coin.models.LunaItemCatalog
import de.meson_labs.luna_coin.models.LunaItemDefinition
import de.meson_labs.luna_coin.models.UserRole

@Composable
fun LunaMeScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onLogout: () -> Unit,
    onChildChanged: (Child) -> Unit
) {
    val isAdmin = selectedChild?.role == UserRole.ADMIN

    val unlockedItems = if (isAdmin) {
        LunaItemCatalog.allItems.map { it.item }
    } else {
        selectedChild?.inventory ?: emptyList()
    }

    var previewItem by remember {
        mutableStateOf(selectedChild?.equippedItem)
    }

    var itemToBuy by remember {
        mutableStateOf<LunaItemDefinition?>(null)
    }

    LaunchedEffect(selectedChild?.id, selectedChild?.equippedItem) {
        if (itemToBuy == null) {
            previewItem = selectedChild?.equippedItem
        }
    }

    val previewDefinition = previewItem?.let {
        LunaItemCatalog.getDefinition(it)
    }

    val lunaImage = previewDefinition?.lunaImageRes ?: R.drawable.luna_dog
    val equippedItem = selectedChild?.equippedItem

    val profileButtonEnabled =
        selectedChild != null &&
                (
                        previewItem == null ||
                                previewItem in unlockedItems ||
                                isAdmin
                        )

    itemToBuy?.let { definition ->
        Dialog(
            onDismissRequest = {
                itemToBuy = null
                previewItem = selectedChild?.equippedItem
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 40.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Card(
                    modifier = Modifier.width(330.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Item kaufen?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            painter = painterResource(id = definition.iconRes),
                            contentDescription = definition.title,
                            modifier = Modifier.size(90.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Möchtest du „${definition.title}“ wirklich für ${definition.priceCoins} Coins kaufen?",
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    itemToBuy = null
                                    previewItem = selectedChild?.equippedItem
                                }
                            ) {
                                Text("Abbrechen")
                            }

                            Button(
                                onClick = {
                                    selectedChild?.let { child ->
                                        if (
                                            child.coins >= definition.priceCoins &&
                                            definition.item !in child.inventory
                                        ) {
                                            val newInventory =
                                                child.inventory + definition.item

                                            previewItem = definition.item

                                            onChildChanged(
                                                child.copy(
                                                    coins = child.coins - definition.priceCoins,
                                                    inventory = newInventory,
                                                    equippedItem = definition.item
                                                )
                                            )
                                        }
                                    }

                                    itemToBuy = null
                                }
                            ) {
                                Text("Kaufen")
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        LunaScreenHeader(
            title = "LunaME",
            selectedChild = selectedChild,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Image(
                    painter = painterResource(id = lunaImage),
                    contentDescription = "Luna",
                    modifier = Modifier.size(360.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    enabled = profileButtonEnabled,
                    onClick = {
                        selectedChild?.let { child ->
                            onChildChanged(
                                child.copy(
                                    profileImageItem = previewItem,
                                    hasProfileImage = true
                                )
                            )
                        }
                    }
                ) {
                    Text("Als Profilbild speichern")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Inventar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Coins: ${selectedChild?.coins ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                InventoryGrid(
                    items = LunaItemCatalog.allItems,
                    unlockedItems = unlockedItems,
                    equippedItem = equippedItem,
                    previewItem = previewItem,
                    isAdmin = isAdmin,
                    selectedChild = selectedChild,
                    onUnlockedItemClick = { clickedItem ->
                        selectedChild?.let { child ->
                            if (child.equippedItem == clickedItem) {
                                previewItem = null

                                onChildChanged(
                                    child.copy(
                                        equippedItem = null
                                    )
                                )
                            } else {
                                previewItem = clickedItem

                                onChildChanged(
                                    child.copy(
                                        equippedItem = clickedItem
                                    )
                                )
                            }
                        }
                    },
                    onBuyRequest = { definition ->
                        previewItem = definition.item
                        itemToBuy = definition
                    }
                )
            }
        }
    }
}

@Composable
private fun InventoryGrid(
    items: List<LunaItemDefinition>,
    unlockedItems: List<LunaInventoryItem>,
    equippedItem: LunaInventoryItem?,
    previewItem: LunaInventoryItem?,
    isAdmin: Boolean,
    selectedChild: Child?,
    onUnlockedItemClick: (LunaInventoryItem) -> Unit,
    onBuyRequest: (LunaItemDefinition) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { definition ->
            val unlocked = definition.item in unlockedItems
            val selected =
                definition.item == equippedItem ||
                        definition.item == previewItem

            InventoryTile(
                definition = definition,
                unlocked = unlocked,
                selected = selected && (unlocked || isAdmin),
                isAdmin = isAdmin,
                canAfford = selectedChild != null &&
                        selectedChild.coins >= definition.priceCoins,
                onClick = {
                    if (unlocked || isAdmin) {
                        onUnlockedItemClick(definition.item)
                    } else {
                        onBuyRequest(definition)
                    }
                }
            )
        }
    }
}

@Composable
private fun InventoryTile(
    definition: LunaItemDefinition,
    unlocked: Boolean,
    selected: Boolean,
    isAdmin: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (selected) {
        3.dp
    } else {
        1.dp
    }

    val visibleAsUnlocked = unlocked || isAdmin

    Column(
        modifier = Modifier
            .size(width = 66.dp, height = 92.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                onClick()
            }
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .alpha(
                    if (visibleAsUnlocked || canAfford) {
                        1f
                    } else {
                        0.35f
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = definition.iconRes),
                contentDescription = definition.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            if (!visibleAsUnlocked) {
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when {
                selected -> "Angelegt"
                visibleAsUnlocked -> ""
                else -> "${definition.priceCoins} 🪙"
            },
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}