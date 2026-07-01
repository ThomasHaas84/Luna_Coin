// screens/LunaMeScreen.kt
package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.NotEnoughCoinsDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaInventoryItem
import de.meson_labs.luna_coin.models.LunaItemCatalog
import de.meson_labs.luna_coin.models.LunaItemDefinition
import de.meson_labs.luna_coin.models.UserRole
import kotlinx.coroutines.delay

@Composable
fun LunaMeScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    selectedChild: Child?,
    onBuyItem: (String) -> Unit,
    onLogout: () -> Unit,
    onChildChanged: (Child) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout
    val isPhonePortrait = isPhone && configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val screenPadding = if (isPhone) {
        14.dp
    } else {
        24.dp
    }

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

    var profileFeedback by remember {
        mutableStateOf<String?>(null)
    }

    var showNotEnoughCoinsDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(profileFeedback) {
        if (profileFeedback != null) {
            delay(2200)
            profileFeedback = null
        }
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
                itemToBuy == null &&
                (
                        previewItem == null ||
                                previewItem in unlockedItems ||
                                isAdmin
                        )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isPhone) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(screenPadding)
            ) {
                LunaScreenHeader(
                    title = "LunaME",
                    selectedChild = selectedChild,
                    onLogout = onLogout
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isPhonePortrait) 260.dp else 170.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = lunaImage),
                                contentDescription = "Luna",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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

                                    profileFeedback = "Profilbild wurde aktualisiert"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Als Profilbild speichern",
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier.height(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            profileFeedback?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Inventar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        InventoryGrid(
                            items = LunaItemCatalog.allItems,
                            unlockedItems = unlockedItems,
                            equippedItem = equippedItem,
                            previewItem = previewItem,
                            isAdmin = isAdmin,
                            selectedChild = selectedChild,
                            isCompact = true,
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(screenPadding)
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
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = lunaImage),
                                contentDescription = "Luna",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .size(800.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

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

                                    profileFeedback = "Profilbild wurde aktualisiert"
                                }
                            }
                        ) {
                            Text("Als Profilbild speichern")
                        }

                        Box(
                            modifier = Modifier.height(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            profileFeedback?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .width(390.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Inventar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        InventoryGrid(
                            items = LunaItemCatalog.allItems,
                            unlockedItems = unlockedItems,
                            equippedItem = equippedItem,
                            previewItem = previewItem,
                            isAdmin = isAdmin,
                            selectedChild = selectedChild,
                            isCompact = false,
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

        itemToBuy?.let { definition ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        itemToBuy = null
                        previewItem = selectedChild?.equippedItem
                    }
                    .padding(
                        start = if (isPhone) 16.dp else 0.dp,
                        end = if (isPhone) 16.dp else 40.dp,
                        bottom = if (isPhonePortrait) 24.dp else 0.dp
                    ),
                contentAlignment = when {
                    isPhonePortrait -> Alignment.BottomCenter
                    isPhone -> Alignment.Center
                    else -> Alignment.CenterEnd
                }
            ) {
                Card(
                    modifier = if (isPhone) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.width(330.dp)
                    }
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {},
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(if (isPhonePortrait) 18.dp else 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Item kaufen?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(if (isPhonePortrait) 10.dp else 16.dp))

                        Image(
                            painter = painterResource(id = definition.iconRes),
                            contentDescription = definition.title,
                            modifier = Modifier.size(if (isPhonePortrait) 70.dp else 90.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(if (isPhonePortrait) 10.dp else 16.dp))

                        Text(
                            text = "Möchtest du „${definition.title}“ wirklich für ${definition.priceCoins} Coins kaufen?",
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(if (isPhonePortrait) 16.dp else 24.dp))

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
                                        if (definition.item in child.inventory) {
                                            itemToBuy = null
                                            previewItem = selectedChild?.equippedItem
                                            return@Button
                                        }

                                        if (child.coins >= definition.priceCoins) {
                                            previewItem = definition.item
                                            onBuyItem(definition.item.name)
                                            itemToBuy = null
                                        } else {
                                            showNotEnoughCoinsDialog = true
                                        }
                                    }
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

    if (showNotEnoughCoinsDialog) {
        NotEnoughCoinsDialog(
            onDismiss = {
                showNotEnoughCoinsDialog = false
            }
        )
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
    isCompact: Boolean,
    onUnlockedItemClick: (LunaInventoryItem) -> Unit,
    onBuyRequest: (LunaItemDefinition) -> Unit
) {
    LazyVerticalGrid(
        columns = if (isCompact) {
            GridCells.Adaptive(minSize = 78.dp)
        } else {
            GridCells.Fixed(4)
        },
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
    ) {
        items(items) { definition ->
            val unlocked = definition.item in unlockedItems

            val selected =
                definition.item == equippedItem

            InventoryTile(
                definition = definition,
                unlocked = unlocked,
                selected = selected && (unlocked || isAdmin),
                isAdmin = isAdmin,
                canAfford = selectedChild != null &&
                        selectedChild.coins >= definition.priceCoins,
                isCompact = isCompact,
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
    isCompact: Boolean,
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
            .size(
                width = if (isCompact) 78.dp else 104.dp,
                height = if (isCompact) 104.dp else 138.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                onClick()
            }
            .padding(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompact) 58.dp else 84.dp)
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
        }

        Spacer(modifier = Modifier.height(8.dp))

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