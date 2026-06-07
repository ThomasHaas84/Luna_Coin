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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.models.Child

private enum class LunaItem {
    Sunglasses
}

private data class InventorySlot(
    val item: LunaItem? = null
)

@Composable
fun LunaMeScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onLogout: () -> Unit
) {
    var equippedItem by remember {
        mutableStateOf<LunaItem?>(null)
    }

    val inventorySlots = remember {
        List(20) { index ->
            when (index) {
                0 -> InventorySlot(LunaItem.Sunglasses)
                else -> InventorySlot()
            }
        }
    }

    val lunaImage = when (equippedItem) {
        LunaItem.Sunglasses -> R.drawable.luna_sunglasses1
        null -> R.drawable.luna_dog
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = lunaImage),
                    contentDescription = "Luna",
                    modifier = Modifier
                        .size(470.dp)
                        .offset(x = (-40).dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.width(260.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Inventar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                InventoryGrid(
                    slots = inventorySlots,
                    equippedItem = equippedItem,
                    onItemClick = { clickedItem ->
                        equippedItem = if (equippedItem == clickedItem) {
                            null
                        } else {
                            clickedItem
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun InventoryGrid(
    slots: List<InventorySlot>,
    equippedItem: LunaItem?,
    onItemClick: (LunaItem) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        slots.chunked(4).forEach { rowSlots ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSlots.forEach { slot ->
                    InventoryTile(
                        slot = slot,
                        equipped = slot.item != null && slot.item == equippedItem,
                        onClick = {
                            slot.item?.let { item ->
                                onItemClick(item)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryTile(
    slot: InventorySlot,
    equipped: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (equipped) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (equipped) 3.dp else 1.dp

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = slot.item != null) {
                onClick()
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        when (slot.item) {
            LunaItem.Sunglasses -> {
                Image(
                    painter = painterResource(id = R.drawable.sunglasses1),
                    contentDescription = "Sonnenbrille",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            null -> {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}