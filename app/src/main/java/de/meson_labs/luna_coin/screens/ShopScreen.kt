package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.ShopItem

@Composable
fun ShopScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    selectedChild: Child?,
    onBuyItem: (String) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Shop",
                        style = MaterialTheme.typography.displaySmall
                    )

                    Text(
                        text = "${selectedChild?.name ?: ""} · ${selectedChild?.coins ?: 0} Luna Coins",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onLogout
                ) {
                    Text("Benutzer wechseln")
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }

        if (data.shopItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Noch keine Shop-Artikel vorhanden.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(data.shopItems) { item ->
                ShopItemCard(
                    item = item,
                    currentCoins = selectedChild?.coins ?: 0,
                    onBuyItem = onBuyItem
                )
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    currentCoins: Int,
    onBuyItem: (String) -> Unit
) {
    val canBuy = currentCoins >= item.priceCoins

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 6.dp
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )

                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "${item.priceCoins} Luna Coins",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    onBuyItem(item.id)
                },
                enabled = canBuy
            ) {
                Text("Kaufen")
            }
        }
    }
}