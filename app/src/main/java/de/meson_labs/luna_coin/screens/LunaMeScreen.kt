// screens/LunaMeScreen.kt
package de.meson_labs.luna_coin.screens

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.NotEnoughCoinsDialog
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
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
    onChildChanged: (Child) -> Unit,
    onIncreaseIntelligence: () -> Unit = {},
    onIncreaseStrength: () -> Unit = {},
    onIncreaseAgility: () -> Unit = {}
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

    val savedProfileItem = selectedChild?.profileImageItem ?: selectedChild?.equippedItem

    var previewItem by remember {
        mutableStateOf(savedProfileItem)
    }

    var selectedArea by remember {
        mutableStateOf(LunaMeArea.INVENTORY)
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

    var skillToConfirm by remember {
        mutableStateOf<LunaSkillConfirm?>(null)
    }

    LaunchedEffect(profileFeedback) {
        if (profileFeedback != null) {
            delay(2200)
            profileFeedback = null
        }
    }

    LaunchedEffect(selectedChild?.id, savedProfileItem) {
        if (itemToBuy == null) {
            previewItem = savedProfileItem
        }
    }

    val previewDefinition = previewItem?.let {
        LunaItemCatalog.getDefinition(it)
    }

    val lunaImage = previewDefinition?.lunaImageRes ?: R.drawable.luna_dog
    val equippedItem = savedProfileItem

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

            Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 14.dp))

            LunaMeAreaSwitch(
                selectedArea = selectedArea,
                isPhone = isPhone,
                onSelectedAreaChanged = { selectedArea = it }
            )

            Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

            when (selectedArea) {
                LunaMeArea.INVENTORY -> {
                    if (isPhone) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
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

                                    profileFeedback?.let { message ->
                                        Spacer(modifier = Modifier.height(4.dp))

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

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(14.dp)
                                ) {
                                    InventoryGrid(
                                        items = LunaItemCatalog.allItems,
                                        unlockedItems = unlockedItems,
                                        equippedItem = equippedItem,
                                        previewItem = previewItem,
                                        isAdmin = isAdmin,
                                        selectedChild = selectedChild,
                                        isCompact = true,
                                        onUnlockedItemClick = { clickedItem ->
                                            previewItem = clickedItem
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
                                    .width(430.dp)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                InventoryGrid(
                                    items = LunaItemCatalog.allItems,
                                    unlockedItems = unlockedItems,
                                    equippedItem = equippedItem,
                                    previewItem = previewItem,
                                    isAdmin = isAdmin,
                                    selectedChild = selectedChild,
                                    isCompact = false,
                                    onUnlockedItemClick = { clickedItem ->
                                        previewItem = clickedItem
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

                LunaMeArea.SKILLS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        SkillsPanel(
                            selectedChild = selectedChild,
                            isPhone = isPhone,
                            onIncreaseIntelligence = {
                                skillToConfirm = LunaSkillConfirm(
                                    title = "Intelligenz",
                                    onConfirm = onIncreaseIntelligence
                                )
                            },
                            onIncreaseStrength = {
                                skillToConfirm = LunaSkillConfirm(
                                    title = "Stärke",
                                    onConfirm = onIncreaseStrength
                                )
                            },
                            onIncreaseAgility = {
                                skillToConfirm = LunaSkillConfirm(
                                    title = "Geschicklichkeit",
                                    onConfirm = onIncreaseAgility
                                )
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
                        previewItem = savedProfileItem
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
                                    previewItem = savedProfileItem
                                }
                            ) {
                                Text("Abbrechen")
                            }

                            Button(
                                onClick = {
                                    selectedChild?.let { child ->
                                        if (definition.item in child.inventory) {
                                            itemToBuy = null
                                            previewItem = savedProfileItem
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

    skillToConfirm?.let { skill ->
        ConfirmationDialog(
            title = "Skillpunkt vergeben?",
            message = "Möchtest du wirklich einen Skillpunkt für ${skill.title} ausgeben? Diese Aktion kann nicht rückgängig gemacht werden.",
            confirmText = "Ja, vergeben",
            dismissText = "Abbrechen",
            onConfirm = {
                skill.onConfirm()
                skillToConfirm = null
            },
            onDismiss = {
                skillToConfirm = null
            }
        )
    }

    if (showNotEnoughCoinsDialog) {
        NotEnoughCoinsDialog(
            onDismiss = {
                showNotEnoughCoinsDialog = false
            }
        )
    }
}

private data class LunaSkillConfirm(
    val title: String,
    val onConfirm: () -> Unit
)


@Composable
private fun LunaMeAreaSwitch(
    selectedArea: LunaMeArea,
    isPhone: Boolean,
    onSelectedAreaChanged: (LunaMeArea) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LunaMeAreaButton(
            text = "🎒 Inventar",
            selected = selectedArea == LunaMeArea.INVENTORY,
            modifier = Modifier.weight(1f),
            isPhone = isPhone,
            onClick = { onSelectedAreaChanged(LunaMeArea.INVENTORY) }
        )

        LunaMeAreaButton(
            text = "⭐ Skills",
            selected = selectedArea == LunaMeArea.SKILLS,
            modifier = Modifier.weight(1f),
            isPhone = isPhone,
            onClick = { onSelectedAreaChanged(LunaMeArea.SKILLS) }
        )
    }
}

@Composable
private fun LunaMeAreaButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    isPhone: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(if (isPhone) 48.dp else 42.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(if (isPhone) 48.dp else 42.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SkillsPanel(
    selectedChild: Child?,
    isPhone: Boolean,
    onIncreaseIntelligence: () -> Unit,
    onIncreaseStrength: () -> Unit,
    onIncreaseAgility: () -> Unit
) {
    if (selectedChild == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Bitte zuerst einen Benutzer auswählen.",
                modifier = Modifier.padding(24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    val currentLevel = selectedChild.level.coerceIn(1, MAX_LEVEL)
    val currentExperience = selectedChild.experience.coerceAtLeast(0)
    val currentLevelStartExperience = totalExperienceForLevel(currentLevel)
    val nextLevelExperience = totalExperienceForLevel((currentLevel + 1).coerceAtMost(MAX_LEVEL))
    val experienceInCurrentLevel = (currentExperience - currentLevelStartExperience).coerceAtLeast(0)
    val experienceNeededForCurrentLevel = (nextLevelExperience - currentLevelStartExperience).coerceAtLeast(1)
    val progress = if (currentLevel >= MAX_LEVEL) {
        1f
    } else {
        (experienceInCurrentLevel.toFloat() / experienceNeededForCurrentLevel.toFloat()).coerceIn(0f, 1f)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(if (isPhone) 2.dp else 3.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isPhone) 7.dp else 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedChild.name,
                        style = if (isPhone) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(if (isPhone) 2.dp else 3.dp))

                    Text(
                        text = "LEVEL $currentLevel",
                        style = if (isPhone) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(if (isPhone) 3.dp else 5.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isPhone) 8.dp else 10.dp)
                            .clip(RoundedCornerShape(99.dp))
                    )

                    Spacer(modifier = Modifier.height(if (isPhone) 2.dp else 3.dp))

                    Text(
                        text = if (currentLevel >= MAX_LEVEL) {
                            "Maximallevel erreicht"
                        } else {
                            "$experienceInCurrentLevel / $experienceNeededForCurrentLevel EP bis Level ${currentLevel + 1}"
                        },
                        style = if (isPhone) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(if (isPhone) 3.dp else 4.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Skillpunkte: ${selectedChild.availableSkillPoints}",
                            modifier = Modifier.padding(
                                horizontal = if (isPhone) 12.dp else 18.dp,
                                vertical = if (isPhone) 4.dp else 5.dp
                            ),
                            style = if (isPhone) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            SkillCard(
                title = "Intelligenz",
                emoji = "🧠",
                imageRes = R.drawable.luna_int,
                value = selectedChild.intelligence,
                availableSkillPoints = selectedChild.availableSkillPoints,
                isPhone = isPhone,
                onIncrease = onIncreaseIntelligence
            )
        }

        item {
            SkillCard(
                title = "Stärke",
                emoji = "💪",
                imageRes = R.drawable.luna_str,
                value = selectedChild.strength,
                availableSkillPoints = selectedChild.availableSkillPoints,
                isPhone = isPhone,
                onIncrease = onIncreaseStrength
            )
        }

        item {
            SkillCard(
                title = "Geschicklichkeit",
                emoji = "🏃",
                imageRes = R.drawable.luna_agil,
                value = selectedChild.agility,
                availableSkillPoints = selectedChild.availableSkillPoints,
                isPhone = isPhone,
                onIncrease = onIncreaseAgility
            )
        }
    }
}

@Composable
private fun SkillCard(
    title: String,
    emoji: String,
    imageRes: Int,
    value: Int,
    availableSkillPoints: Int,
    isPhone: Boolean,
    onIncrease: () -> Unit
) {
    val safeValue = value.coerceIn(1, MAX_SKILL_VALUE)
    val canIncrease = availableSkillPoints > 0 && safeValue < MAX_SKILL_VALUE

    val cardHeight = if (isPhone) 150.dp else 235.dp
    val imageWindowWidth = if (isPhone) 200.dp else 350.dp
    val imageSize = if (isPhone) 205.dp else 350.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isPhone) 0.dp else 4.dp,
                    vertical = 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isPhone) 1.dp else 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(imageWindowWidth)
                    .fillMaxHeight()
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = if (isPhone) 4.dp else 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$emoji $title",
                    style = if (isPhone) MaterialTheme.typography.labelLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (isPhone) 1.dp else 2.dp))

                Text(
                    text = "$safeValue / $MAX_SKILL_VALUE",
                    style = if (isPhone) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(if (isPhone) 2.dp else 4.dp))

                LinearProgressIndicator(
                    progress = { safeValue.toFloat() / MAX_SKILL_VALUE.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isPhone) 6.dp else 8.dp)
                        .clip(RoundedCornerShape(99.dp))
                )
            }

            Button(
                enabled = canIncrease,
                onClick = onIncrease,
                modifier = Modifier
                    .padding(end = if (isPhone) 2.dp else 6.dp)
                    .size(if (isPhone) 38.dp else 52.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = "Skill erhöhen",
                    modifier = Modifier.size(
                        if (isPhone) 22.dp else 28.dp
                    )
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

            val isSavedProfileItem = definition.item == equippedItem
            val isPreviewItem = definition.item == previewItem

            InventoryTile(
                definition = definition,
                unlocked = unlocked,
                selected = isSavedProfileItem && (unlocked || isAdmin),
                previewSelected = isPreviewItem && !isSavedProfileItem && (unlocked || isAdmin),
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
    previewSelected: Boolean,
    isAdmin: Boolean,
    canAfford: Boolean,
    isCompact: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected || previewSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (selected || previewSelected) {
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
                previewSelected -> "Vorschau"
                visibleAsUnlocked -> ""
                else -> "${definition.priceCoins} 🪙"
            },
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun totalExperienceForLevel(level: Int): Int {
    val safeLevel = level.coerceIn(1, MAX_LEVEL)
    return ((safeLevel - 1) * (safeLevel + 2) * 5) / 2
}

private enum class LunaMeArea {
    INVENTORY,
    SKILLS
}

private const val MAX_LEVEL = 100
private const val MAX_SKILL_VALUE = 100
